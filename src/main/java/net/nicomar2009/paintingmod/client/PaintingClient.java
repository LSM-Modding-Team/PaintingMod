package net.nicomar2009.paintingmod.client;

import com.supermartijn642.fusion.api.model.predicates.FusionModelPredicateRegistry;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.nicomar2009.paintingmod.PaintingMod;

/** Register before Fusion freezes its model registry; never loaded on dedicated servers. */
@Mod(value = PaintingMod.MOD_ID, dist = Dist.CLIENT)
public final class PaintingClient {
    public PaintingClient() {
        FusionModelPredicateRegistry.registerModelPredicate(
                Identifier.fromNamespaceAndPath(PaintingMod.MOD_ID, "painted_surface"), PaintedSurfacePredicate.SERIALIZER);
    }
}
