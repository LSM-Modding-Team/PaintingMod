package net.nicomar2009.paintingmod.registry;

import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nicomar2009.paintingmod.PaintingMod;
import net.nicomar2009.paintingmod.block.*;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PaintingMod.MOD_ID);
    public static final DeferredBlock<SchoolPaintableBlock> SCHOOL_PAINTABLE_BLOCK = BLOCKS.registerBlock(
            "school_paintable_block", SchoolPaintableBlock::new,
            p -> p.strength(1.8F, 6F).sound(SoundType.STONE).requiresCorrectToolForDrops());
    public static final DeferredBlock<SchoolPaintableCustomBlock> SCHOOL_PAINTABLE_CUSTOM_BLOCK = BLOCKS.registerBlock(
            "school_paintable_custom_block", SchoolPaintableCustomBlock::new,
            p -> p.strength(1.8F, 6F).sound(SoundType.STONE).requiresCorrectToolForDrops().dynamicShape().noOcclusion());
    public static final DeferredBlock<SchoolMiniCubePaintableBlock> SCHOOL_PAINTABLE_MINI_CUBE_BLOCK = BLOCKS.registerBlock(
            "school_paintable_mini_cube_block", SchoolMiniCubePaintableBlock::new,
            p -> p.strength(1.8F, 6F).sound(SoundType.STONE).requiresCorrectToolForDrops().dynamicShape().noOcclusion());
    private ModBlocks() {}
}
