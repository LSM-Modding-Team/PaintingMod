package net.nicomar2009.paintingmod.registry;

import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nicomar2009.paintingmod.PaintingMod;
import net.nicomar2009.paintingmod.block.*;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PaintingMod.MOD_ID);
    public static final DeferredBlock<PaintableBlock> PAINTABLE_BLOCK = BLOCKS.registerBlock(
            "paintable_block", PaintableBlock::new,
            p -> p.strength(1.8F, 6F).sound(SoundType.STONE).requiresCorrectToolForDrops().lightLevel(state -> 0));
    public static final DeferredBlock<PaintableCustomBlock> PAINTABLE_CUSTOM_BLOCK = BLOCKS.registerBlock(
            "paintable_custom_block", PaintableCustomBlock::new,
            p -> p.strength(1.8F, 6F).sound(SoundType.STONE).requiresCorrectToolForDrops().lightLevel(state -> 0).dynamicShape().noOcclusion());
    public static final DeferredBlock<MiniCubePaintableBlock> PAINTABLE_MINI_CUBE_BLOCK = BLOCKS.registerBlock(
            "paintable_mini_cube_block", MiniCubePaintableBlock::new,
            p -> p.strength(1.8F, 6F).sound(SoundType.STONE).requiresCorrectToolForDrops().lightLevel(state -> 0).dynamicShape().noOcclusion());
    private ModBlocks() {}
}
