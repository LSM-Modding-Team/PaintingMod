package net.nicomar2009.paintingmod.api;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** Optional immutable controller value for the ONE paint authority. The consumer owns its durable storage
 * and transport; PaintingMod has no parallel global store. Carrier blocks keep links, not independent layers.
 * Save this under an owner paint-only key in the controller's existing persistence and update packet,
 * or store it once per structure UUID in the consumer's dimension store. Choose one of these, not both.
 */
public record PaintLayer(UUID owner,long revision,PaintData data) {
    public PaintLayer {
        Objects.requireNonNull(owner); Objects.requireNonNull(data);
        if (revision<0 || revision>=Long.MAX_VALUE-1) throw new IllegalArgumentException("Invalid paint revision");
    }
    /** Initially ORIGINAL. Set Paint.DEFAULT explicitly for cells whose placement finish should be default paint. */
    public static PaintLayer fresh(UUID owner) { return new PaintLayer(owner,0,PaintData.EMPTY); }
    public PaintTarget target(String geometryKey,BlockPos authority,Set<BlockPos> members,List<PaintSurface> surfaces) {
        return new PaintTarget(owner.toString(),revision,geometryKey,authority,members,surfaces,data);
    }
    public boolean matches(PaintTarget expected) {
        return owner.toString().equals(expected.identity()) && revision==expected.revision() && data.equals(expected.data());
    }
    /** Pure preparation, not a commit. Provider must compare live geometryKey, membership and permissions,
     * persist next successfully, then publish/synchronize it. Never publish this value before persistence succeeds.
     */
    public PaintLayer next(PaintTarget expected,PaintData replacement) {
        if (!matches(expected)) throw new IllegalStateException("Stale paint snapshot");
        replacement.validate(expected.surfaces()); return new PaintLayer(owner,revision+1,replacement);
    }
    public CompoundTag save(List<PaintSurface> surfaces) {
        CompoundTag tag=new CompoundTag(); tag.putInt("version",1); tag.putString("owner",owner.toString());
        tag.putLong("revision",revision); tag.put("data",data.save(surfaces)); return tag;
    }
    /** Use the same codec for disk and synchronized snapshots; bind it to the controller's actual structure UUID.
     * The consumer rejects stale/out-of-order revisions and unavailable/removed owners before installing updates.
     */
    public static PaintLayer load(CompoundTag tag,UUID expectedOwner,List<PaintSurface> surfaces) {
        if (tag.getIntOr("version",-1)!=1 || !tag.getStringOr("owner","").equals(expectedOwner.toString()))
            throw new IllegalArgumentException("Wrong paint owner/format");
        return new PaintLayer(expectedOwner,tag.getLongOr("revision",-1),PaintData.load(tag.getCompoundOrEmpty("data"),surfaces));
    }
}
