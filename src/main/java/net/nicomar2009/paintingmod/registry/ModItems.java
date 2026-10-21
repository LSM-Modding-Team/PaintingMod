package net.nicomar2009.paintingmod.registry;

import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nicomar2009.paintingmod.PaintingMod;
import net.nicomar2009.paintingmod.tool.*;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PaintingMod.MOD_ID);
    public static final DeferredItem<BlockItem> PAINTABLE_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.PAINTABLE_BLOCK);
    public static final DeferredItem<BlockItem> PAINTABLE_CUSTOM_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.PAINTABLE_CUSTOM_BLOCK);
    public static final DeferredItem<BlockItem> PAINTABLE_MINI_CUBE_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.PAINTABLE_MINI_CUBE_BLOCK);
    public static final DeferredItem<SandpaperItem> SCHOOL_SANDPAPER = ITEMS.registerItem("school_sandpaper", SandpaperItem::new);

    public static final DeferredItem<HammerItem> SCHOOL_HAMMER = ITEMS.registerItem("school_hammer", HammerItem::new);

    public static final DeferredItem<PaintCleanerItem> PAINT_CLEANER = ITEMS.registerItem("paint_cleaner", PaintCleanerItem::new);

    private ModItems() {}
}
