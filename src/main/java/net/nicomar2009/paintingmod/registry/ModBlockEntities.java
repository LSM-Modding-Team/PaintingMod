package net.nicomar2009.paintingmod.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nicomar2009.paintingmod.PaintingMod;
import net.nicomar2009.paintingmod.block.*;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PaintingMod.MOD_ID);
    public static final Supplier<BlockEntityType<PaintableBlockEntity>> PAINTABLE = BLOCK_ENTITIES.register(
            "paintable_block", () -> new BlockEntityType<>(PaintableBlockEntity::new, ModBlocks.PAINTABLE_BLOCK.get()));
    public static final Supplier<BlockEntityType<PaintableCustomBlockEntity>> PAINTABLE_CUSTOM = BLOCK_ENTITIES.register(
            "paintable_custom_block", () -> new BlockEntityType<>(PaintableCustomBlockEntity::new, ModBlocks.PAINTABLE_CUSTOM_BLOCK.get()));
    public static final Supplier<BlockEntityType<MiniCubePaintEntity>> MINI_CUBE_PAINT = BLOCK_ENTITIES.register(
            "mini_cube_paint", () -> new BlockEntityType<>(MiniCubePaintEntity::new, ModBlocks.PAINTABLE_MINI_CUBE_BLOCK.get()));
    private ModBlockEntities() {}
}
