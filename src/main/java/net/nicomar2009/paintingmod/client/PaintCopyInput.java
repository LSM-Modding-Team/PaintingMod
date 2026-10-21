package net.nicomar2009.paintingmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.nicomar2009.paintingmod.PaintingMod;
import net.nicomar2009.paintingmod.block.PaintableBlockEntity;
import net.nicomar2009.paintingmod.network.PaintCopyPayload;
import net.nicomar2009.paintingmod.tool.PaintItem;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = PaintingMod.MOD_ID, value = Dist.CLIENT)
public final class PaintCopyInput {
    @SubscribeEvent public static void copy(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!event.isUseItem() || minecraft.player == null || minecraft.level == null
                || !(minecraft.hitResult instanceof BlockHitResult hit)
                || !(minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof PaintableBlockEntity block)
                || !block.canPaintAt(hit.getDirection(),hit.getLocation())) return;
        var window = minecraft.getWindow();
        if (!InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                && !InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL)) return;
        InteractionHand hand;
        if (minecraft.player.getMainHandItem().getItem() instanceof PaintItem) hand = InteractionHand.MAIN_HAND;
        else if (minecraft.player.getOffhandItem().getItem() instanceof PaintItem) hand = InteractionHand.OFF_HAND;
        else return;
        // Intercept before useItemOn sends its normal server packet; cancel both hands.
        event.setCanceled(true); event.setSwingHand(false);
        if (event.getHand() == hand)
            ClientPacketDistributor.sendToServer(new PaintCopyPayload(hit.getBlockPos(), hand == InteractionHand.OFF_HAND));
    }
}
