package net.nicomar2009.paintingmod.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.nicomar2009.paintingmod.paint.Paint;

/** Stable material state; face selection and placement rotation happen in the shared renderer. */
final class PaintAppearance {
    private PaintAppearance() {}
    static BlockState state(Paint paint) {
        // A paint is a surface, not a placed vanilla block. Neighbors must not change its axis
        // or choose another variant: this also keeps copied item previews faithful to the block.
        return BuiltInRegistries.BLOCK.getValue(Identifier.parse(paint.blockId())).defaultBlockState();
    }
}
