package net.nicomar2009.paintingmod.api;

import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** World-space placement of a piece-local UV basis. Coordinates are in blocks, not pixels.
 * U/V need not have right-handed winding: mirrors must preserve the original material face.
 */
public record PaintFrame(Vec3 origin, Direction u, Direction v, Direction normal) {
    public PaintFrame {
        Objects.requireNonNull(origin); Objects.requireNonNull(u); Objects.requireNonNull(v); Objects.requireNonNull(normal);
        if (!Double.isFinite(origin.x) || !Double.isFinite(origin.y) || !Double.isFinite(origin.z)
                || u.getAxis()==v.getAxis() || u.getAxis()==normal.getAxis() || v.getAxis()==normal.getAxis())
            throw new IllegalArgumentException("Invalid paint frame");
    }
    public static Vec3 vector(Direction direction) {
        return new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }
    public Vec3 point(double localU, double localV) {
        return origin.add(vector(u).scale(localU)).add(vector(v).scale(localV));
    }
    public double localU(Vec3 point) { return point.subtract(origin).dot(vector(u)); }
    public double localV(Vec3 point) { return point.subtract(origin).dot(vector(v)); }
    public double distanceToPlane(Vec3 point) { return point.subtract(origin).dot(vector(normal)); }

    /** Same placement convention as Core doors: mirror local X, then (x,z) -> (-z,x).
     * Never rewrite cell indices on rotation/reflection. Pass the CLOSED, piece-local frame.
     */
    public PaintFrame placed(Vec3 worldOrigin, int quarterTurns, boolean mirrored) {
        if (quarterTurns<0 || quarterTurns>3) throw new IllegalArgumentException("Invalid rotation");
        return new PaintFrame(worldOrigin.add(transform(origin,quarterTurns,mirrored)),
                transform(u,quarterTurns,mirrored),transform(v,quarterTurns,mirrored),transform(normal,quarterTurns,mirrored));
    }
    private static Vec3 transform(Vec3 point,int turns,boolean mirrored) {
        double x=mirrored?-point.x:point.x, z=point.z;
        return switch(turns) {
            case 0 -> new Vec3(x,point.y,z); case 1 -> new Vec3(-z,point.y,x);
            case 2 -> new Vec3(-x,point.y,-z); default -> new Vec3(z,point.y,-x);
        };
    }
    private static Direction transform(Direction direction,int turns,boolean mirrored) {
        Vec3 transformed=transform(vector(direction),turns,mirrored);
        // Compare components rather than signed zero in Vec3.equals.
        for (Direction candidate:Direction.values())
            if (candidate.getStepX()==transformed.x && candidate.getStepY()==transformed.y && candidate.getStepZ()==transformed.z) return candidate;
        throw new IllegalArgumentException("Invalid basis");
    }
}
