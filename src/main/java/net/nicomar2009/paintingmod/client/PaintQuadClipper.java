package net.nicomar2009.paintingmod.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.nicomar2009.paintingmod.geometry.PaintOrientation;
import org.joml.Vector3fc;

/** Crop the original face UVs to a 16px, 8px or 4px square, then move it to the cut plane. */
final class PaintQuadClipper {
    private record Vertex(float u, float v, float textureU, float textureV) {
        Vertex between(Vertex other, float t) {
            return new Vertex(u + (other.u-u)*t, v + (other.v-v)*t,
                    textureU + (other.textureU-textureU)*t, textureV + (other.textureV-textureV)*t);
        }
    }
    private PaintQuadClipper() {}
    static BakedQuad clip(BakedQuad source, Direction face, int cube, int divisions, int color, boolean ao, Direction up) {
        if (source.direction() != face) return null;
        int x = cube % divisions, y = cube / divisions % divisions, z = cube / (divisions * divisions);
        float size = 1F / divisions;
        int uCell = face.getAxis() == Direction.Axis.X ? z : x;
        int vCell = face.getAxis() == Direction.Axis.Y ? z : y;
        int normalCell = switch (face.getAxis()) { case X -> x; case Y -> y; case Z -> z; };
        float plane = (normalCell + (face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0)) * size;
        List<Vertex> vertices = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            Vector3fc p = source.position(i);
            float normal = switch (face.getAxis()) { case X -> p.x(); case Y -> p.y(); case Z -> p.z(); };
            float boundary = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0;
            if (Math.abs(normal - boundary) > 0.00001F) return null;
            vertices.add(new Vertex(face.getAxis() == Direction.Axis.X ? p.z() : p.x(),
                    face.getAxis() == Direction.Axis.Y ? p.z() : p.y(),
                    UVPair.unpackU(source.packedUV(i)), UVPair.unpackV(source.packedUV(i))));
        }
        vertices = cut(vertices, true, uCell * size, true);
        vertices = cut(vertices, true, (uCell+1) * size, false);
        vertices = cut(vertices, false, vCell * size, true);
        vertices = cut(vertices, false, (vCell+1) * size, false);
        // Catalog geometry is axis-aligned. A nondegenerate clipped rectangle has four corners.
        var corners = new ArrayList<Vertex>(4);
        for (Vertex vertex : vertices) if (corners.isEmpty() || distance(corners.getLast(), vertex) > 1e-10F) corners.add(vertex);
        if (corners.size() > 1 && distance(corners.getFirst(), corners.getLast()) < 1e-10F) corners.removeLast();
        if (corners.size() != 4 || distance(corners.get(0), corners.get(1)) < 1e-10F) return null;
        var material = source.materialInfo();
        var builder = new QuadBakingVertexConsumer();
        Direction worldFace = PaintOrientation.face(face, Direction.UP, up);
        builder.setDirection(worldFace);
        builder.setSprite(material.sprite(), material.layer(), material.itemRenderType());
        builder.setTintIndex(-1);
        builder.setShade(material.shade());
        builder.setAmbientOcclusion(ao);
        builder.setLightEmission(0);
        for (Vertex v : corners) {
            Vec3 point = switch (face.getAxis()) {
                case X -> new Vec3(plane, v.v, v.u);
                case Y -> new Vec3(v.u, plane, v.v);
                case Z -> new Vec3(v.u, v.v, plane);
            };
            point = PaintOrientation.rotate(point, up);
            builder.addVertex((float) point.x, (float) point.y, (float) point.z);
            builder.setUv(v.textureU, v.textureV);
            builder.setColor(color);
            builder.setNormal(worldFace.getStepX(), worldFace.getStepY(), worldFace.getStepZ());
        }
        return builder.bakeQuad();
    }
    private static float distance(Vertex a, Vertex b) { return (a.u-b.u)*(a.u-b.u) + (a.v-b.v)*(a.v-b.v); }
    private static List<Vertex> cut(List<Vertex> input, boolean u, float edge, boolean lower) {
        var output = new ArrayList<Vertex>();
        if (input.isEmpty()) return output;
        Vertex previous = input.getLast();
        float p = u ? previous.u : previous.v;
        for (Vertex current : input) {
            float c = u ? current.u : current.v;
            boolean inside = lower ? c >= edge : c <= edge, wasInside = lower ? p >= edge : p <= edge;
            if (inside != wasInside) output.add(previous.between(current, (edge-p)/(c-p)));
            if (inside) output.add(current);
            previous = current; p = c;
        }
        return output;
    }
}
