package net.nicomar2009.paintingmod.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Hit is recomputed by the server for tools. Provider selection must ray-test its actual current geometry,
 * reject occlusion/ambiguous boundaries and return only an explicitly paintable nearest surface.
 * Do not trust a client-supplied surface ID or the carrier block's full-cube face.
 */
public record PaintQuery(Level level,Player player,ItemStack tool,BlockHitResult hit) {
    public Vec3 from() { return player.getEyePosition(); }
    public Vec3 to() { return from().add(player.getViewVector(1).scale(player.blockInteractionRange())); }
}
