package net.nicomar2009.paintingmod.client;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

/** One meshing result, split by cull direction, with no references to mutable entities. */
final class PaintPart implements BlockStateModelPart {
    private final EnumMap<Direction, List<BakedQuad>> faces = new EnumMap<>(Direction.class);
    private final List<BakedQuad> unculled = new ArrayList<>();
    private final Material.Baked particle;
    private int flags;
    PaintPart(Material.Baked particle) { this.particle = particle; }
    void add(Direction cull, BakedQuad quad) {
        if (cull == null) unculled.add(quad);
        else faces.computeIfAbsent(cull, ignored -> new ArrayList<>()).add(quad);
        flags |= quad.materialInfo().flags();
    }
    @Override public List<BakedQuad> getQuads(Direction cull) { return cull == null ? unculled : faces.getOrDefault(cull, List.of()); }
    @Override public boolean useAmbientOcclusion() { return true; }
    @Override public Material.Baked particleMaterial() { return particle; }
    @Override public int materialFlags() { return flags; }
}
