package net.nicomar2009.paintingmod.tool;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.nicomar2009.paintingmod.block.PaintableBlockEntity;
import net.nicomar2009.paintingmod.paint.CopiedPaintPattern;
import net.nicomar2009.paintingmod.paint.Paint;

/** Reusable paint; a copied six-face palette is applied once, then normal painting resumes. */
public class PaintItem extends Item {
    private final Paint paint;
    public PaintItem(Paint paint, Properties properties) { super(properties.stacksTo(1)); this.paint = paint; }
    public static void copyTo(ItemStack stack, PaintableBlockEntity source) {
        RectanglePaintTool.clear(stack);
        CopiedPaintPattern.capture(source).write(stack);
    }
    @Override public InteractionResult use(net.minecraft.world.level.Level level,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand) {
        return RectanglePaintTool.cancel(level,player,player.getItemInHand(hand));
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel(); var pos = context.getClickedPos(); var player = context.getPlayer();
        if (!(level.getBlockEntity(pos) instanceof PaintableBlockEntity block)) return InteractionResult.PASS;
        if (!block.canPaintAt(context.getClickedFace(),context.getClickLocation())) return InteractionResult.PASS;
        if (player == null || player.isSpectator() || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) return InteractionResult.FAIL;
        InteractionResult rectangle=RectanglePaintTool.use(context,paint);
        if(rectangle!=null)return rectangle;
        if (!level.isClientSide()) {
            ItemStack stack = context.getItemInHand();
            CopiedPaintPattern pattern = CopiedPaintPattern.read(stack);
            if (pattern == null && CopiedPaintPattern.hasCopy(stack)) {
                player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.paintingmod.paint_copy_invalid"));
                return InteractionResult.FAIL;
            }
            if (pattern == null) block.paintAt(context.getClickedFace(), context.getClickLocation(), paint);
            else {
                pattern.apply(block);
                CopiedPaintPattern.clear(stack);
                player.containerMenu.broadcastChanges();
            }
        }
        return InteractionResult.SUCCESS;
    }
}
