package net.nicomar2009.paintingmod.geometry;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Six proper cube rotations: local +Y points away from the supporting face. */
public final class PaintOrientation {
    private PaintOrientation() {}
    public static Vec3 rotate(Vec3 point, Direction up) {
        double x = point.x, y = point.y, z = point.z;
        return switch (up) {
            case UP -> point;
            case DOWN -> new Vec3(x, 1-y, 1-z);
            case EAST -> new Vec3(y, 1-x, z);
            case WEST -> new Vec3(1-y, x, z);
            case SOUTH -> new Vec3(x, 1-z, y);
            case NORTH -> new Vec3(x, z, 1-y);
        };
    }
    private static Direction inverse(Direction up) {
        return switch (up) {
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
            case SOUTH -> Direction.NORTH;
            case NORTH -> Direction.SOUTH;
            default -> up;
        };
    }
    public static Direction face(Direction face, Direction from, Direction to) {
        var point = new Vec3(.5 + face.getStepX()*.5, .5 + face.getStepY()*.5, .5 + face.getStepZ()*.5);
        point = rotate(rotate(point, inverse(from)), to);
        for (Direction candidate : Direction.values())
            if (point.x == .5 + candidate.getStepX()*.5 && point.y == .5 + candidate.getStepY()*.5
                    && point.z == .5 + candidate.getStepZ()*.5) return candidate;
        throw new IllegalStateException("Invalid cube rotation");
    }
    public static int cube(int cube, int divisions, Direction from, Direction to) {
        var point = new Vec3((cube%divisions+.5)/divisions, (cube/divisions%divisions+.5)/divisions,
                (cube/(divisions*divisions)+.5)/divisions);
        point = rotate(rotate(point, inverse(from)), to);
        return (int)(point.x*divisions) + divisions*(int)(point.y*divisions)
                + divisions*divisions*(int)(point.z*divisions);
    }
    public static int square(Direction face, int cube, int divisions) {
        int x = cube%divisions, y = cube/divisions%divisions, z = cube/(divisions*divisions);
        return switch (face.getAxis()) {
            case X -> z + divisions*y;
            case Y -> x + divisions*z;
            case Z -> x + divisions*y;
        };
    }
}
