package net.nicomar2009.paintingmod.block;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.nicomar2009.paintingmod.paint.SchoolPaint;
import net.nicomar2009.paintingmod.registry.ModBlockEntities;

public class SchoolPaintableBlockEntity extends BlockEntity {
    private final int[] paints = new int[6];
    public SchoolPaintableBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.SCHOOL_PAINTABLE.get(), pos, state);
    }
    protected SchoolPaintableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    public SchoolPaint paint(Direction face) { return SchoolPaint.fromId(paints[face.ordinal()]); }
    public boolean canPaintAt(Direction face,Vec3 hit) {return true;}
    public SchoolPaint paintAt(Direction face, Vec3 hit) { return paint(face); }
    public void paintAt(Direction face, Vec3 hit, SchoolPaint color) { paintFace(face, color); }
    public int[] snapshot() { return paints.clone(); }
    public boolean painted() { for (int paint : paints) if (paint != 0) return true; return false; }
    public void paintFace(Direction face, SchoolPaint paint) {
        int[] copy = snapshot(); copy[face.ordinal()] = paint.ordinal(); apply(copy);
    }
    /** Placement hook only. Loading and update packets must never call this method. */
    public void initializePlaced() {
        int[] colors = snapshot();
        Arrays.fill(colors, SchoolPaint.DEFAULT.ordinal());
        apply(colors);
    }
    public void clean() { apply(new int[6]); }
    public void apply(int[] colors) {
        if (colors.length == 24 || colors.length == 96) {
            int[] wholeFaces = new int[6];
            for (int face = 0; face < 6; face++) wholeFaces[face] = colors[face * (colors.length/6)];
            apply(wholeFaces); return;
        }
        if (colors.length != 6) throw new IllegalArgumentException("Expected six face colors");
        int[] validated = Arrays.stream(colors).map(id -> SchoolPaint.fromId(id).ordinal()).toArray();
        if (Arrays.equals(paints, validated)) return;
        System.arraycopy(validated, 0, paints, 0, 6);
        changed();
    }
    protected void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (Direction face : Direction.values()) output.putInt("paint_" + face.getSerializedName(), paints[face.ordinal()]);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (Direction face : Direction.values())
            paints[face.ordinal()] = SchoolPaint.fromId(input.getIntOr("paint_" + face.getSerializedName(), 0)).ordinal();
        invalidateClientGeometry();
    }
    protected void invalidateClientGeometry() {
        // A BE data packet arrives after the block update; invalidate client geometry again.
        if (level != null && level.isClientSide())
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
