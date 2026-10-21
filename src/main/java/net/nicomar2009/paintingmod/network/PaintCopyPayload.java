package net.nicomar2009.paintingmod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nicomar2009.paintingmod.PaintingMod;
import net.nicomar2009.paintingmod.block.PaintableBlockEntity;
import net.nicomar2009.paintingmod.tool.PaintItem;
import net.nicomar2009.paintingmod.tool.SurfacePaintTools;

/** The client sends a target, never trusted colors; the server copies its own block data. */
public record PaintCopyPayload(BlockPos pos, boolean offHand) implements CustomPacketPayload {
    public static final Type<PaintCopyPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PaintingMod.MOD_ID, "copy_paint"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PaintCopyPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PaintCopyPayload::pos, ByteBufCodecs.BOOL, PaintCopyPayload::offHand, PaintCopyPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, CODEC, PaintCopyPayload::handle);
    }
    private static void handle(PaintCopyPayload request, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
            var level = player.level();
            if (!level.hasChunkAt(request.pos) || !player.isWithinBlockInteractionRange(request.pos, 0)) return;
            var stack = player.getItemInHand(request.offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!(stack.getItem() instanceof PaintItem)) return;
            var hit = player.pick(player.blockInteractionRange(), 1, false);
            if (!(hit instanceof BlockHitResult blockHit) || !blockHit.getBlockPos().equals(request.pos)
                    || !level.mayInteract(player, request.pos) || !player.mayUseItemAt(request.pos, blockHit.getDirection(), stack)) return;
            if (SurfacePaintTools.registered(level,request.pos)) {
                if (SurfacePaintTools.copy(player,stack,blockHit))
                    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.paintingmod.paint_copied"));
                return;
            }
            if (level.getBlockEntity(request.pos) instanceof PaintableBlockEntity block
                    && block.canPaintAt(blockHit.getDirection(),blockHit.getLocation())) {
                PaintItem.copyTo(stack, block); player.containerMenu.broadcastChanges();
                player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.paintingmod.paint_copied"));
            }
        });
    }
}
