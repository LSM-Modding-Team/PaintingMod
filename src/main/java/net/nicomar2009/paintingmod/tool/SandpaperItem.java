package net.nicomar2009.paintingmod.tool;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.nicomar2009.paintingmod.block.PaintableBlockEntity;
import net.nicomar2009.paintingmod.paint.Paint;
import net.nicomar2009.paintingmod.registry.ModPaintItems;

public class SandpaperItem extends Item {
    public SandpaperItem(Properties properties) { super(properties.stacksTo(1)); }
    @Override public InteractionResult use(net.minecraft.world.level.Level level,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand) {
        return RectanglePaintTool.cancel(level,player,player.getItemInHand(hand));
    }
    public static void refund(net.minecraft.world.entity.player.Player player, Paint paint) {
        var item = ModPaintItems.refundItem(paint);
        if (item == null) return;
        ItemStack recovered = new ItemStack(item);
        if (!player.getInventory().add(recovered)) player.drop(recovered, false);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level=context.getLevel();var pos=context.getClickedPos();var player=context.getPlayer();
        InteractionResult external=SurfacePaintTools.use(context,null,net.nicomar2009.paintingmod.api.PaintProvider.Action.SAND);
        if(external!=null)return external;
        if(SurfacePaintTools.hasRectangle(context.getItemInHand())) return InteractionResult.FAIL;
        if(!(level.getBlockEntity(pos) instanceof PaintableBlockEntity block)) return InteractionResult.PASS;
        if(!block.canPaintAt(context.getClickedFace(),context.getClickLocation())) return InteractionResult.PASS;
        if(player==null||player.isSpectator()||!level.mayInteract(player,pos)
                ||!player.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand())) return InteractionResult.FAIL;
        InteractionResult rectangle=RectanglePaintTool.use(context,null);
        if(rectangle!=null)return rectangle;
        Paint color=block.paintAt(context.getClickedFace(),context.getClickLocation());
        if(color==Paint.UNPAINTED) return InteractionResult.FAIL;
        if(!level.isClientSide()) {
            block.paintAt(context.getClickedFace(),context.getClickLocation(),Paint.UNPAINTED);
            refund(player, color);
        }
        return InteractionResult.SUCCESS;
    }
}
