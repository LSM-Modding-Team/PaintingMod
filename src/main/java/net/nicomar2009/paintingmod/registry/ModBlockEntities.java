package net.nicomar2009.paintingmod.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nicomar2009.paintingmod.PaintingMod;
import net.nicomar2009.paintingmod.block.*;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PaintingMod.MOD_ID);
    public static final Supplier<BlockEntityType<SchoolPaintableBlockEntity>> SCHOOL_PAINTABLE = BLOCK_ENTITIES.register(
            "school_paintable_block", () -> new BlockEntityType<>(SchoolPaintableBlockEntity::new, ModBlocks.SCHOOL_PAINTABLE_BLOCK.get()));
    public static final Supplier<BlockEntityType<SchoolPaintableCustomBlockEntity>> SCHOOL_PAINTABLE_CUSTOM = BLOCK_ENTITIES.register(
            "school_paintable_custom_block", () -> new BlockEntityType<>(SchoolPaintableCustomBlockEntity::new, ModBlocks.SCHOOL_PAINTABLE_CUSTOM_BLOCK.get()));
    public static final Supplier<BlockEntityType<SchoolMiniCubePaintEntity>> SCHOOL_MINI_CUBE_PAINT = BLOCK_ENTITIES.register(
            "school_mini_cube_paint", () -> new BlockEntityType<>(SchoolMiniCubePaintEntity::new, ModBlocks.SCHOOL_PAINTABLE_MINI_CUBE_BLOCK.get()));
    private ModBlockEntities() {}
}
