package net.nicomar2009.paintingmod.tool;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.nicomar2009.paintingmod.block.SchoolPaintableBlockEntity;
import net.nicomar2009.paintingmod.paint.SchoolPaint;
import net.nicomar2009.paintingmod.registry.ModItems;

public class SchoolSandpaperItem extends Item {
    public SchoolSandpaperItem(Properties properties) { super(properties.stacksTo(1)); }
    @Override public InteractionResult use(net.minecraft.world.level.Level level,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand) {
        return RectanglePaintTool.cancel(level,player,player.getItemInHand(hand));
    }
    public static void refund(net.minecraft.world.entity.player.Player player,SchoolPaint color) {
        if (!color.refundable()) return;
        Item item=switch(color) {
            case LIGHT -> ModItems.LIGHT_SCHOOL.get(); case DARK -> ModItems.DARK_SCHOOL.get();
            case STREET -> ModItems.STREET_SCHOOL.get(); case WHITE -> ModItems.CLASSROOM_WHITE.get();
            case FLOOR -> ModItems.CLASSROOM_FLOOR_PAINT.get(); case YELLOW -> ModItems.YELLOW_SCHOOL_PAINT.get();
            case PLAYGROUND_WHITE -> ModItems.WHITE_PLAYGROUND_FLOOR_PAINT.get();
            case PLAYGROUND_GRAY -> ModItems.GRAY_PLAYGROUND_FLOOR_PAINT.get();
            case PLAYGROUND_GREEN -> ModItems.GREEN_PLAYGROUND_FLOOR_PAINT.get();
            case PLAYGROUND_ORANGE -> ModItems.ORANGE_PLAYGROUND_FLOOR_PAINT.get();
            case DARK_TILED -> ModItems.DARK_TILED_FLOOR_PAINT.get();
            case RED_STREET -> ModItems.RED_STREET_WALL_PAINT.get();
            case LABORATORY_TILE -> ModItems.SCHOOL_LABORATORY_TILE_PAINT.get();
            case LABORATORY_TABLE_TILE -> ModItems.LABORATORY_TABLE_TILE_PAINT.get();
            case AUDITORIUM_PARQUET -> ModItems.AUDITORIUM_PARQUET_PAINT.get();
            default -> throw new IllegalArgumentException("Unpainted faces cannot yield paint");
        };
        ItemStack recovered=new ItemStack(item);
        if(!player.getInventory().add(recovered))player.drop(recovered,false);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level=context.getLevel();var pos=context.getClickedPos();var player=context.getPlayer();
        if(!(level.getBlockEntity(pos) instanceof SchoolPaintableBlockEntity block)) return InteractionResult.PASS;
        if(!block.canPaintAt(context.getClickedFace(),context.getClickLocation())) return InteractionResult.PASS;
        if(player==null||player.isSpectator()||!level.mayInteract(player,pos)
                ||!player.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand())) return InteractionResult.FAIL;
        InteractionResult rectangle=RectanglePaintTool.use(context,null);
        if(rectangle!=null)return rectangle;
        SchoolPaint color=block.paintAt(context.getClickedFace(),context.getClickLocation());
        if(color==SchoolPaint.UNPAINTED) return InteractionResult.FAIL;
        if(!level.isClientSide()) {
            block.paintAt(context.getClickedFace(),context.getClickLocation(),SchoolPaint.UNPAINTED);
            if (color.refundable()) refund(player,color);
        }
        return InteractionResult.SUCCESS;
    }
}
