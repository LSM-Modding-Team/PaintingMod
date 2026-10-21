package net.nicomar2009.paintingmod.tool;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.nicomar2009.paintingmod.block.PaintableBlockEntity;

public class PaintCleanerItem extends Item {
    public PaintCleanerItem(Properties properties) { super(properties.stacksTo(1)); }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel(); var pos = context.getClickedPos(); var player = context.getPlayer();
        InteractionResult external=SurfacePaintTools.use(context,null,net.nicomar2009.paintingmod.api.PaintProvider.Action.CLEAN);
        if(external!=null)return external;
        if (!(level.getBlockEntity(pos) instanceof PaintableBlockEntity block)) return InteractionResult.PASS;
        if (!block.canPaintAt(context.getClickedFace(),context.getClickLocation())) return InteractionResult.PASS;
        if (player == null || player.isSpectator() || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) return InteractionResult.FAIL;
        if (!level.isClientSide()) block.clean();
        return InteractionResult.SUCCESS;
    }
}
