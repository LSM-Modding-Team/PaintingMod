package net.nicomar2009.paintingmod.tool;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.nicomar2009.paintingmod.api.*;
import net.nicomar2009.paintingmod.paint.CopiedPaintPattern;
import net.nicomar2009.paintingmod.paint.Paint;

/** Explicit providers use this path; the original three block implementations keep their old path/formats. */
public final class SurfacePaintTools {
    private static final String RECTANGLE="paintingmod_surface_rectangle";
    private static final int MAX_RECTANGLE_CELLS=4096;
    private SurfacePaintTools() {}
    public static boolean registered(Level level,BlockPos pos) {
        return level.hasChunkAt(pos) && PaintProviders.find(level.getBlockState(pos).getBlock()).isPresent();
    }
    private static CompoundTag tag(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag(); }
    private static void write(ItemStack stack,CompoundTag tag) {
        if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA); else stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }
    public static boolean hasRectangle(ItemStack stack) { return tag(stack).contains(RECTANGLE); }
    public static void clearRectangle(ItemStack stack) { var tag=tag(stack); tag.remove(RECTANGLE); write(stack,tag); }
    /** null leaves the legacy cancellation path unchanged. */
    public static InteractionResult cancel(Level level,Player player,ItemStack stack) {
        if (!player.isShiftKeyDown() || !hasRectangle(stack)) return null;
        if (!level.isClientSide()) {
            clearRectangle(stack); player.containerMenu.broadcastChanges();
            player.sendOverlayMessage(Component.translatable("message.paintingmod.rectangle.cancelled"));
        }
        return InteractionResult.SUCCESS;
    }
    /** Server use never trusts the packet's face/location to choose a provider cell. */
    private static PaintQuery query(UseOnContext context) {
        var player=context.getPlayer();
        if (player==null) return null;
        if (!context.getLevel().isClientSide()) {
            var picked=player.pick(player.blockInteractionRange(),1,false);
            if (!(picked instanceof BlockHitResult hit) || !hit.getBlockPos().equals(context.getClickedPos())) return null;
            return new PaintQuery(context.getLevel(),player,context.getItemInHand(),hit);
        }
        return new PaintQuery(context.getLevel(),player,context.getItemInHand(),
                new BlockHitResult(context.getClickLocation(),context.getClickedFace(),context.getClickedPos(),false));
    }
    private static InteractionResult fail(Player player,String key) {
        if (player!=null) player.sendOverlayMessage(Component.translatable(key));
        return InteractionResult.FAIL;
    }
    /** null means the block has no registered provider. CLEAN erases the complete resolved paint target. */
    public static InteractionResult use(UseOnContext context,Paint paint,PaintProvider.Action action) {
        if (!registered(context.getLevel(),context.getClickedPos())) return null;
        var player=context.getPlayer(); var stack=context.getItemInHand();
        if (player==null || player.isSpectator() || !player.mayBuild()) return InteractionResult.FAIL;
        PaintQuery query=query(context);
        if (query==null) return InteractionResult.FAIL;
        var picked=PaintOperations.pick(query);
        // An excluded/occluded region does not fall through to legacy painting or door use.
        if (picked.isEmpty()) return InteractionResult.FAIL;
        var selection=picked.get(); var target=selection.target();
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        if (action!=PaintProvider.Action.CLEAN) {
            if (RectanglePaintTool.pending(stack)) return fail(player,"message.paintingmod.rectangle.target");
            InteractionResult rectangle=rectangle(query,selection,paint,action);
            if (rectangle!=null) return rectangle;
        }
        Map<PaintCell,Paint> changes;
        if (action==PaintProvider.Action.APPLY && SurfacePaintPattern.hasCopy(stack)) {
            var pattern=SurfacePaintPattern.read(stack); var surface=target.surface(selection.cell().surface());
            if (pattern==null || !pattern.compatible(surface)) return fail(player,"message.paintingmod.paint_copy_invalid");
            changes=pattern.changes(surface); action=PaintProvider.Action.PASTE;
        } else if (action==PaintProvider.Action.APPLY && CopiedPaintPattern.hasCopy(stack)) {
            // The six-face clipboard cannot be reinterpreted as a door surface or an occupancy operation.
            return fail(player,"message.paintingmod.paint_copy_invalid");
        } else if (action==PaintProvider.Action.CLEAN) {
            changes=new HashMap<>();
            for (var surface:target.surfaces()) for (var cell:SurfacePaintPattern.cells(surface)) changes.put(cell,Paint.UNPAINTED);
        } else {
            if (action==PaintProvider.Action.SAND && target.data().paint(selection.cell())==Paint.UNPAINTED) return InteractionResult.FAIL;
            changes=Map.of(selection.cell(),action==PaintProvider.Action.SAND?Paint.UNPAINTED:paint);
        }
        if (!PaintOperations.change(query,selection,action,changes)) return InteractionResult.FAIL;
        if (action==PaintProvider.Action.SAND) refund(player,target.data(),changes);
        if (action==PaintProvider.Action.PASTE) { SurfacePaintPattern.clear(stack); player.containerMenu.broadcastChanges(); }
        return InteractionResult.SUCCESS;
    }
    /** Called only after the common copy packet handler has verified range, tool and vanilla ray. */
    public static boolean copy(ServerPlayer player,ItemStack stack,BlockHitResult hit) {
        var query=new PaintQuery(player.level(),player,stack,hit); var picked=PaintOperations.pick(query);
        if (picked.isEmpty()) return false;
        var selection=picked.get(); var surface=selection.target().surface(selection.cell().surface());
        if (!PaintOperations.authorize(query,selection,PaintProvider.Action.READ,SurfacePaintPattern.cells(surface))) return false;
        RectanglePaintTool.clear(stack);
        SurfacePaintPattern.capture(selection.target(),surface).write(stack);
        player.containerMenu.broadcastChanges(); return true;
    }
    private static void refund(Player player,PaintData before,Map<PaintCell,Paint> changes) {
        for (var entry:changes.entrySet()) {
            Paint previous=before.paint(entry.getKey());
            if (previous!=entry.getValue() && previous.refundable()) SandpaperItem.refund(player,previous);
        }
    }
    private static void anchor(PaintQuery query,PaintOperations.Selection selection,Paint paint,PaintProvider.Action action) {
        var anchor=new CompoundTag();
        anchor.putString("owner",query.player().getUUID().toString());
        anchor.putString("dimension",query.level().dimension().identifier().toString());
        anchor.putString("provider",selection.registration().id().toString());
        anchor.putString("target",selection.target().identity()); anchor.putLong("revision",selection.target().revision());
        anchor.putString("geometry",selection.target().geometryKey());
        anchor.putString("surface",selection.cell().surface()); anchor.putInt("cell",selection.cell().index());
        anchor.putString("action",action.name()); anchor.putInt("paint",paint==null?0:paint.id());
        var data=tag(query.tool()); data.put(RECTANGLE,anchor); write(query.tool(),data);
    }
    /** Bounded rectangles within one authoritative target. Compatible coplanar patches may share a rectangle;
     * never discover unrelated structures, load chunks, or cross the built-in block/clipboard formats.
     */
    private static InteractionResult rectangle(PaintQuery query,PaintOperations.Selection selected,Paint paint,PaintProvider.Action action) {
        var stack=query.tool(); var player=query.player(); boolean pending=hasRectangle(stack);
        if (!pending && !player.isShiftKeyDown()) return null;
        if (action!=PaintProvider.Action.APPLY && action!=PaintProvider.Action.SAND) return InteractionResult.FAIL;
        if (!PaintOperations.authorize(query,selected,action,java.util.Set.of(selected.cell()))) return InteractionResult.FAIL;
        if (!pending) {
            anchor(query,selected,paint,action); player.containerMenu.broadcastChanges();
            player.sendOverlayMessage(Component.translatable("message.paintingmod.rectangle.start")); return InteractionResult.SUCCESS;
        }
        var anchor=tag(stack).getCompoundOrEmpty(RECTANGLE); var target=selected.target();
        if (!anchor.getStringOr("owner","").equals(player.getUUID().toString())
                || !anchor.getStringOr("dimension","").equals(query.level().dimension().identifier().toString())
                || !anchor.getStringOr("provider","").equals(selected.registration().id().toString())
                || !anchor.getStringOr("target","").equals(target.identity())
                || anchor.getLongOr("revision",-1)!=target.revision()
                || !anchor.getStringOr("geometry","").equals(target.geometryKey())
                || !anchor.getStringOr("action","").equals(action.name()) || anchor.getIntOr("paint",-1)!=(paint==null?0:paint.id()))
            return fail(player,"message.paintingmod.rectangle.target");
        PaintSurface first=target.surfaces().stream().filter(s->s.id().equals(anchor.getStringOr("surface",""))).findFirst().orElse(null);
        int firstIndex=anchor.getIntOr("cell",-1);
        if (first==null || !first.allows(firstIndex)) return fail(player,"message.paintingmod.rectangle.target");
        PaintSurface last=target.surface(selected.cell().surface());
        int[] offset=offset(first,last);
        if (offset==null) return fail(player,"message.paintingmod.rectangle.plane");
        int a=firstIndex%first.width(),b=firstIndex/first.width();
        int c=offset[0]+selected.cell().index()%last.width(),d=offset[1]+selected.cell().index()/last.width();
        int u0=Math.min(a,c),u1=Math.max(a,c),v0=Math.min(b,d),v1=Math.max(b,d);
        if (((long)u1-u0+1)*((long)v1-v0+1)>MAX_RECTANGLE_CELLS) return fail(player,"message.paintingmod.rectangle.large");
        var changes=new HashMap<PaintCell,Paint>(); var positions=new java.util.HashSet<Long>();
        for (var surface:target.surfaces()) {
            int[] delta=offset(first,surface); if (delta==null) continue;
            for (int index:surface.mask().stream().toArray()) {
                int u=delta[0]+index%surface.width(),v=delta[1]+index/surface.width();
                if (u<u0 || u>u1 || v<v0 || v>v1) continue;
                // Overlapping paint patches are ambiguous; never paint/refund twice at one position.
                if (!positions.add(((long)u<<32) ^ (v&0xffffffffL))) return fail(player,"message.paintingmod.rectangle.target");
                changes.put(new PaintCell(surface.id(),index),action==PaintProvider.Action.SAND?Paint.UNPAINTED:paint);
            }
        }
        if (changes.isEmpty() || !PaintOperations.change(query,selected,action,changes)) return fail(player,"message.paintingmod.rectangle.denied");
        if (action==PaintProvider.Action.SAND) refund(player,target.data(),changes);
        long count=changes.entrySet().stream().filter(e->target.data().paint(e.getKey())!=e.getValue()).count();
        clearRectangle(stack); player.containerMenu.broadcastChanges();
        player.sendOverlayMessage(Component.translatable("message.paintingmod.rectangle.done",count)); return InteractionResult.SUCCESS;
    }
    /** Return aligned integer 8px offsets; different orientation, material group or plane is incompatible. */
    private static int[] offset(PaintSurface root,PaintSurface other) {
        var a=root.frame(); var b=other.frame();
        if (!root.compatibility().equals(other.compatibility()) || a.u()!=b.u() || a.v()!=b.v() || a.normal()!=b.normal()
                || Math.abs(a.distanceToPlane(b.origin()))>1e-6) return null;
        double u=a.localU(b.origin())/PaintSurface.CELL_SIZE,v=a.localV(b.origin())/PaintSurface.CELL_SIZE;
        if (Math.abs(u-Math.rint(u))>1e-6 || Math.abs(v-Math.rint(v))>1e-6 || Math.abs(u)>4096 || Math.abs(v)>4096) return null;
        return new int[]{(int)Math.rint(u),(int)Math.rint(v)};
    }
}
