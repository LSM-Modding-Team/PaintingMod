package net.nicomar2009.paintingmod.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.nicomar2009.paintingmod.paint.CopiedPaintPattern;
import net.nicomar2009.paintingmod.paint.Paint;

/** Surface-only clipboard, distinct from the legacy six-face/occupancy clipboard. No geometry is pasted.
 * Compatibility requires the owner key, dimensions and exact allowed mask, independent of placement.
 */
public final class SurfacePaintPattern {
    private static final String KEY="paintingmod_surface_copy";
    private final Identifier compatibility;
    private final int width,height;
    private final int[] indices,colors;
    private SurfacePaintPattern(Identifier compatibility,int width,int height,int[] indices,int[] colors) {
        this.compatibility=compatibility; this.width=width; this.height=height;
        this.indices=indices.clone(); this.colors=colors.clone();
    }
    public static Set<PaintCell> cells(PaintSurface surface) {
        return surface.mask().stream().mapToObj(index->new PaintCell(surface.id(),index)).collect(Collectors.toUnmodifiableSet());
    }
    /** Caller authorizes READ over cells(surface) before capturing server data. */
    public static SurfacePaintPattern capture(PaintTarget target,PaintSurface surface) {
        int[] indices=surface.mask().stream().toArray(); int[] colors=new int[indices.length];
        for (int i=0;i<indices.length;i++) colors[i]=target.data().paint(new PaintCell(surface.id(),indices[i])).id();
        return new SurfacePaintPattern(surface.compatibility(),surface.width(),surface.height(),indices,colors);
    }
    public boolean compatible(PaintSurface surface) {
        return compatibility.equals(surface.compatibility()) && width==surface.width() && height==surface.height()
                && Arrays.equals(indices,surface.mask().stream().toArray());
    }
    public Map<PaintCell,Paint> changes(PaintSurface destination) {
        if (!compatible(destination)) throw new IllegalArgumentException("Incompatible surface clipboard");
        var result=new HashMap<PaintCell,Paint>();
        for (int i=0;i<indices.length;i++) result.put(new PaintCell(destination.id(),indices[i]),Paint.fromId(colors[i]));
        return Map.copyOf(result);
    }
    private static CompoundTag tag(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag(); }
    public static boolean hasCopy(ItemStack stack) { return tag(stack).contains(KEY); }
    public static void clear(ItemStack stack) {
        var tag=tag(stack); tag.remove(KEY);
        if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA); else stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }
    public void write(ItemStack stack) {
        CopiedPaintPattern.clear(stack);
        var tag=tag(stack); var copy=new CompoundTag();
        copy.putInt("version",1); copy.putInt("palette",Paint.PALETTE_VERSION); copy.putString("compatibility",compatibility.toString());
        copy.putInt("width",width); copy.putInt("height",height); copy.putIntArray("indices",indices); copy.putIntArray("colors",colors);
        tag.put(KEY,copy); stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }
    /** Malformed or unknown formats return null; callers must distinguish that from !hasCopy. */
    public static SurfacePaintPattern read(ItemStack stack) {
        var tag=tag(stack).getCompoundOrEmpty(KEY);
        int width=tag.getIntOr("width",0),height=tag.getIntOr("height",0);
        if (tag.getIntOr("version",-1)!=1 || tag.getIntOr("palette",-1)!=Paint.PALETTE_VERSION
                || width<1 || width>64 || height<1 || height>64) return null;
        int[] indices=tag.getIntArray("indices").orElse(null),colors=tag.getIntArray("colors").orElse(null);
        if (indices==null || colors==null || indices.length==0 || indices.length>4096 || indices.length!=colors.length) return null;
        for (int i=0;i<indices.length;i++) {
            if (indices[i]<0 || indices[i]>=width*height || i>0 && indices[i]<=indices[i-1]
                    || !Paint.validId(colors[i]) || colors[i]!=0 && !Paint.fromId(colors[i]).enabled()) return null;
        }
        try { return new SurfacePaintPattern(Identifier.parse(tag.getStringOr("compatibility","")),width,height,indices,colors); }
        catch (IllegalArgumentException error) { return null; }
    }
}
