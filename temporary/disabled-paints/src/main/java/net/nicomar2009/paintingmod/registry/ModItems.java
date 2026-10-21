package net.nicomar2009.paintingmod.registry;

import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nicomar2009.paintingmod.PaintingMod;
import net.nicomar2009.paintingmod.paint.SchoolPaint;
import net.nicomar2009.paintingmod.tool.*;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PaintingMod.MOD_ID);
    public static final DeferredItem<BlockItem> SCHOOL_PAINTABLE_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.SCHOOL_PAINTABLE_BLOCK);
    public static final DeferredItem<BlockItem> SCHOOL_PAINTABLE_CUSTOM_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.SCHOOL_PAINTABLE_CUSTOM_BLOCK);
    public static final DeferredItem<BlockItem> SCHOOL_PAINTABLE_MINI_CUBE_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.SCHOOL_PAINTABLE_MINI_CUBE_BLOCK);
    public static final DeferredItem<SchoolSandpaperItem> SCHOOL_SANDPAPER = ITEMS.registerItem("school_sandpaper", SchoolSandpaperItem::new);

    public static final DeferredItem<SchoolHammerItem> SCHOOL_HAMMER = ITEMS.registerItem("school_hammer", SchoolHammerItem::new);

    public static final DeferredItem<SchoolPaintItem> LIGHT_SCHOOL = ITEMS.registerItem("light_school", props -> new SchoolPaintItem(SchoolPaint.LIGHT, props));

    public static final DeferredItem<SchoolPaintItem> DARK_SCHOOL = ITEMS.registerItem("dark_school", props -> new SchoolPaintItem(SchoolPaint.DARK, props));

    public static final DeferredItem<SchoolPaintItem> STREET_SCHOOL = ITEMS.registerItem("street_school", props -> new SchoolPaintItem(SchoolPaint.STREET, props));

    public static final DeferredItem<SchoolPaintItem> CLASSROOM_WHITE = ITEMS.registerItem("classroom_white", props -> new SchoolPaintItem(SchoolPaint.WHITE, props));

    public static final DeferredItem<SchoolPaintItem> CLASSROOM_FLOOR_PAINT = ITEMS.registerItem("classroom_floor_paint", props -> new SchoolPaintItem(SchoolPaint.FLOOR, props));

    public static final DeferredItem<SchoolPaintItem> YELLOW_SCHOOL_PAINT = ITEMS.registerItem("yellow_school_paint", props -> new SchoolPaintItem(SchoolPaint.YELLOW, props));

    public static final DeferredItem<SchoolPaintItem> WHITE_PLAYGROUND_FLOOR_PAINT = ITEMS.registerItem("white_playground_floor_paint", props -> new SchoolPaintItem(SchoolPaint.PLAYGROUND_WHITE, props));

    public static final DeferredItem<SchoolPaintItem> GRAY_PLAYGROUND_FLOOR_PAINT = ITEMS.registerItem("gray_playground_floor_paint", props -> new SchoolPaintItem(SchoolPaint.PLAYGROUND_GRAY, props));

    public static final DeferredItem<SchoolPaintItem> GREEN_PLAYGROUND_FLOOR_PAINT = ITEMS.registerItem("green_playground_floor_paint", props -> new SchoolPaintItem(SchoolPaint.PLAYGROUND_GREEN, props));

    public static final DeferredItem<SchoolPaintItem> ORANGE_PLAYGROUND_FLOOR_PAINT = ITEMS.registerItem("orange_playground_floor_paint", props -> new SchoolPaintItem(SchoolPaint.PLAYGROUND_ORANGE, props));

    public static final DeferredItem<SchoolPaintItem> DARK_TILED_FLOOR_PAINT = ITEMS.registerItem("dark_tiled_floor_paint", props -> new SchoolPaintItem(SchoolPaint.DARK_TILED, props));

    public static final DeferredItem<SchoolPaintItem> RED_STREET_WALL_PAINT = ITEMS.registerItem("red_street_wall_paint", props -> new SchoolPaintItem(SchoolPaint.RED_STREET, props));

    public static final DeferredItem<SchoolPaintItem> SCHOOL_LABORATORY_TILE_PAINT = ITEMS.registerItem("school_laboratory_tile_paint", props -> new SchoolPaintItem(SchoolPaint.LABORATORY_TILE, props));

    public static final DeferredItem<SchoolPaintItem> LABORATORY_TABLE_TILE_PAINT = ITEMS.registerItem("laboratory_table_tile_paint", props -> new SchoolPaintItem(SchoolPaint.LABORATORY_TABLE_TILE, props));

    public static final DeferredItem<SchoolPaintItem> AUDITORIUM_PARQUET_PAINT = ITEMS.registerItem("auditorium_parquet_paint", props -> new SchoolPaintItem(SchoolPaint.AUDITORIUM_PARQUET, props));

    public static final DeferredItem<PaintCleanerItem> PAINT_CLEANER = ITEMS.registerItem("paint_cleaner", PaintCleanerItem::new);

    private ModItems() {}
}
