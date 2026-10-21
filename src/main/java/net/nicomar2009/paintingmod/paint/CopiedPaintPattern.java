package net.nicomar2009.paintingmod.paint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.nicomar2009.paintingmod.block.FinePaintBlockEntity;
import net.nicomar2009.paintingmod.block.PaintableBlock;
import net.nicomar2009.paintingmod.block.PaintableBlockEntity;
import net.nicomar2009.paintingmod.block.PaintableCustomBlockEntity;
import net.nicomar2009.paintingmod.geometry.PaintOrientation;
import net.nicomar2009.paintingmod.model.PaintModelData;

/** One immutable clipboard used by both the item preview and authoritative paste. */
public final class CopiedPaintPattern {
    public static final String KEY = "paintingmod_paint_copy";
    private final Direction up;
    private final int[] colors;
    private final long occupied;
    private final BlockPos origin;
    private CopiedPaintPattern(int[] colors, long occupied, BlockPos origin, Direction up) {
        this.up = up;
        this.colors = colors.clone(); this.occupied = occupied; this.origin = origin.immutable();
    }
    public static CopiedPaintPattern capture(PaintableBlockEntity source) {
        long occupied = source instanceof FinePaintBlockEntity fine ? fine.occupied()
                : source instanceof PaintableCustomBlockEntity custom ? custom.occupied() : 1;
        return new CopiedPaintPattern(source.snapshot(), occupied, source.getBlockPos(), source.getBlockState().getValue(PaintableBlock.PAINT_UP));
    }
    public int divisions() { return colors.length == 96 ? 4 : colors.length == 24 ? 2 : 1; }
    public BlockPos origin() { return origin; }
    public PaintModelData modelData() { return new PaintModelData(divisions(), occupied, colors, up); }
    /** Rotate the complete saved template from its original support to the new support. */
    public CopiedPaintPattern orient(Direction target) {
        if (target == up) return this;
        int d = divisions(), squares = d*d;
        int[] rotated = new int[colors.length];
        long mask = 0;
        for (int cube = 0; cube < d*d*d; cube++) {
            int next = PaintOrientation.cube(cube, d, up, target);
            if ((occupied & (1L << cube)) != 0) mask |= 1L << next;
            for (Direction face : Direction.values()) {
                Direction nextFace = PaintOrientation.face(face, up, target);
                rotated[nextFace.ordinal()*squares + PaintOrientation.square(nextFace, next, d)] =
                        colors[face.ordinal()*squares + PaintOrientation.square(face, cube, d)];
            }
        }
        return new CopiedPaintPattern(rotated, mask, origin, target);
    }
    public void apply(PaintableBlockEntity destination) {
        destination.apply(orient(destination.getBlockState().getValue(PaintableBlock.PAINT_UP)).colors.clone());
    }
    /** Full block cloning restores occupancy as well; ordinary pattern paste only applies colors. */
    public boolean restoreBlock(PaintableBlockEntity destination) {
        if (destination.snapshot().length != colors.length) return false;
        var rotated = orient(destination.getBlockState().getValue(PaintableBlock.PAINT_UP));
        if (destination instanceof FinePaintBlockEntity fine) fine.restoreOccupancy(rotated.occupied);
        else if (destination instanceof PaintableCustomBlockEntity custom) custom.restoreOccupancy((int) rotated.occupied);
        destination.apply(rotated.colors.clone());
        return true;
    }
    public void write(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag copy = new CompoundTag();
        copy.putInt("paint_palette", Paint.PALETTE_VERSION);
        copy.putIntArray("paints", colors);
        copy.putLong("occupied", occupied);
        copy.putString("up", up.getSerializedName());
        copy.putInt("origin_x", origin.getX()); copy.putInt("origin_y", origin.getY()); copy.putInt("origin_z", origin.getZ());
        data.put(KEY, copy); stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }
    public static CopiedPaintPattern read(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!data.contains(KEY)) return null;
        CompoundTag copy = data.getCompoundOrEmpty(KEY);
        int[] colors = copy.getIntArray("paints").orElse(null);
        if (colors == null) {
            // Read the original clipboard, which stored each face/quadrant as its own integer.
            int count = copy.getIntOr("subdivisions", copy.getBooleanOr("quadrants", false) ? 4 : 1);
            if (count != 1 && count != 4 && count != 16) return null;
            colors = new int[count * 6];
            for (Direction face : Direction.values()) for (int q = 0; q < count; q++) {
                String key = face.getSerializedName() + (count > 1 ? "_" + q : "");
                if (!copy.contains(key)) return null;
                colors[face.ordinal() * count + q] = copy.getIntOr(key, -1);
            }
        }
        if (colors.length != 6 && colors.length != 24 && colors.length != 96) return null;
        int palette = copy.getIntOr("paint_palette", 1);
        for (int i = 0; i < colors.length; i++) {
            int id = colors[i];
            if (id < 0 || (palette < Paint.PALETTE_VERSION ? id > 16 : !Paint.validId(id))) return null;
            colors[i] = Paint.decode(id, palette);
        }
        int d = colors.length == 96 ? 4 : colors.length == 24 ? 2 : 1;
        long full = d == 4 ? -1L : (1L << (d * d * d)) - 1;
        long occupied = copy.getLongOr("occupied", full) & full;
        if (occupied == 0) return null;
        return new CopiedPaintPattern(colors, occupied, new BlockPos(copy.getIntOr("origin_x", 0),
                copy.getIntOr("origin_y", 0), copy.getIntOr("origin_z", 0)), readUp(copy));
    }
    private static Direction readUp(CompoundTag copy) {
        String name = copy.getStringOr("up", "up");
        for (Direction face : Direction.values()) if (face.getSerializedName().equals(name)) return face;
        return Direction.UP;
    }
    public static boolean hasCopy(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY);
    }
    public static void clear(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.remove(KEY);
        if (data.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }
}
