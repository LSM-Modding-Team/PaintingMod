package net.nicomar2009.paintingmod.tool;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.nicomar2009.paintingmod.block.MiniCubePaintEntity;
import net.nicomar2009.paintingmod.block.PaintableCustomBlockEntity;

public final class HammerItem extends Item {
    public HammerItem(Properties properties) { super(properties.stacksTo(1)); }

    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var player = context.getPlayer();
        var entity = level.getBlockEntity(pos);
        if (!(entity instanceof MiniCubePaintEntity) && !(entity instanceof PaintableCustomBlockEntity))
            return InteractionResult.PASS;
        if (player == null || player.isSpectator() || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()))
            return InteractionResult.FAIL;
        if (entity instanceof MiniCubePaintEntity mini) {
            if (!mini.canPaintAt(context.getClickedFace(), context.getClickLocation())) return InteractionResult.PASS;
            if (!player.mayBuild()) return InteractionResult.FAIL;
            if (!level.isClientSide()) mini.removeCube(context.getClickedFace(), context.getClickLocation());
        } else if (entity instanceof PaintableCustomBlockEntity custom) {
            if (!custom.canPaintAt(context.getClickedFace(), context.getClickLocation())) return InteractionResult.PASS;
            if (!level.isClientSide()) custom.removeCube(context.getClickedFace(), context.getClickLocation());
        }
        return InteractionResult.SUCCESS;
    }
}
