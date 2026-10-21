package net.nicomar2009.paintingmod.tool;

import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.nicomar2009.paintingmod.block.FinePaintBlockEntity;
import net.nicomar2009.paintingmod.block.PaintableBlockEntity;
import net.nicomar2009.paintingmod.block.PaintableCustomBlockEntity;
import net.nicomar2009.paintingmod.paint.Paint;

/** Server-authoritative coplanar rectangles, one palette/update per changed block. */
public final class RectanglePaintTool {
    private static final String KEY="paintingmod_paint_rectangle";
    private static final int MAX_BLOCKS=256;
    private record End(int plane,int u0,int u1,int v0,int v1) {}
    private record Change(PaintableBlockEntity block,int[] colors) {}
    private RectanglePaintTool() {}
    private static CompoundTag tag(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag(); }
    private static void write(ItemStack stack,CompoundTag tag) {
        if(tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }
    public static void clear(ItemStack stack) { CompoundTag tag=tag(stack);tag.remove(KEY);write(stack,tag); }
    public static InteractionResult cancel(Level level,Player player,ItemStack stack) {
        if(!player.isShiftKeyDown() || !tag(stack).contains(KEY)) return InteractionResult.PASS;
        if(!level.isClientSide()) { clear(stack);player.containerMenu.broadcastChanges();player.sendOverlayMessage(Component.translatable("message.paintingmod.rectangle.cancelled")); }
        return InteractionResult.SUCCESS;
    }
    private static int axis(Direction face) { return switch(face.getAxis()) { case X->0;case Y->1;case Z->2; }; }
    private static int uAxis(int axis) { return axis==0 ? 2 : 0; }
    private static int vAxis(int axis) { return axis==1 ? 2 : 1; }
    private static End end(UseOnContext context,int divisions) {
        BlockPos p=context.getClickedPos();int[] pos={p.getX(),p.getY(),p.getZ()};
        var hit=context.getClickLocation();double[] point={hit.x,hit.y,hit.z};
        for(int i=0;i<3;i++) if(!Double.isFinite(point[i]) || point[i]<pos[i]-1e-6 || point[i]>pos[i]+1+1e-6)return null;
        int axis=axis(context.getClickedFace()),u=uAxis(axis),v=vAxis(axis);
        double normal=point[axis]-pos[axis];
        if(Math.abs(normal-Math.round(normal*divisions)/(double)divisions)>1e-6)return null;
        if(divisions==1 && Math.abs(normal-(context.getClickedFace().getAxisDirection()==Direction.AxisDirection.POSITIVE?1:0))>1e-6)return null;
        int size=4/divisions,a=pos[u]*4,b=pos[v]*4;
        a+=Math.clamp((int)Math.floor((point[u]-pos[u])*divisions),0,divisions-1)*size;
        b+=Math.clamp((int)Math.floor((point[v]-pos[v])*divisions),0,divisions-1)*size;
        return new End((int)Math.round(point[axis]*4),a,a+size-1,b,b+size-1);
    }
    private static boolean exposed(Level level,BlockPos pos,Direction face,PaintableCustomBlockEntity block,int[] halves) {
        int cube=halves[0]+2*halves[1]+4*halves[2];if(!block.hasCube(cube))return false;
        int axis=axis(face),step=face.getAxisDirection()==Direction.AxisDirection.POSITIVE?1:-1;
        int next=halves[axis]+step;
        if(next>=0 && next<2) return !block.hasCube(cube+step*(1<<axis));
        BlockPos neighbor=pos.relative(face);if(!level.hasChunkAt(neighbor))return false;
        if(level.getBlockEntity(neighbor) instanceof PaintableCustomBlockEntity other) {
            int outside=cube+(step>0?-1:1)*(1<<axis);return !other.hasCube(outside);
        }
        return !level.getBlockState(neighbor).isFaceSturdy(level,neighbor,face.getOpposite());
    }
    private static InteractionResult fail(Player player,String key) {
        player.sendOverlayMessage(Component.translatable("message.paintingmod.rectangle."+key));return InteractionResult.FAIL;
    }
    /** null means ordinary one-face use; null paint means sanding and recovering colors. */
    public static InteractionResult use(UseOnContext context,Paint paint) {
        var player=context.getPlayer();var level=context.getLevel();var stack=context.getItemInHand();
        CompoundTag data=tag(stack);boolean pending=data.contains(KEY);
        if(!pending && (player==null || !player.isShiftKeyDown())) return null;
        if(player==null || player.isSpectator() || !player.mayBuild())return InteractionResult.FAIL;
        if(level.isClientSide())return InteractionResult.SUCCESS;
        var current=level.getBlockEntity(context.getClickedPos());
        int divisions=current instanceof FinePaintBlockEntity fine?16/fine.miniSize():current instanceof PaintableCustomBlockEntity?2:1;
        End end=end(context,divisions);Direction face=context.getClickedFace();
        if(end==null)return fail(player,"target");
        String dimension=level.dimension().identifier().toString();
        if(!pending) {
            CompoundTag anchor=new CompoundTag();anchor.putString("owner",player.getUUID().toString());anchor.putString("dimension",dimension);
            anchor.putString("face",face.getSerializedName());anchor.putInt("plane",end.plane);
            anchor.putInt("grid",4);
            anchor.putInt("u0",end.u0);anchor.putInt("u1",end.u1);anchor.putInt("v0",end.v0);anchor.putInt("v1",end.v1);
            data.put(KEY,anchor);write(stack,data);player.containerMenu.broadcastChanges();
            player.sendOverlayMessage(Component.translatable("message.paintingmod.rectangle.start"));return InteractionResult.SUCCESS;
        }
        CompoundTag anchor=data.getCompoundOrEmpty(KEY);
        if(anchor.getIntOr("grid",2)==2) {
            for(String key:new String[]{"plane","u0","v0"})anchor.putInt(key,anchor.getIntOr(key,0)*2);
            for(String key:new String[]{"u1","v1"})anchor.putInt(key,anchor.getIntOr(key,0)*2+1);
            anchor.putInt("grid",4);
        }
        if(!anchor.getStringOr("owner","").equals(player.getUUID().toString()) || !anchor.getStringOr("dimension","").equals(dimension)
                || !anchor.getStringOr("face","").equals(face.getSerializedName()) || anchor.getIntOr("plane",Integer.MIN_VALUE)!=end.plane)
            return fail(player,"plane");
        int u0=Math.min(anchor.getIntOr("u0",end.u0),end.u0),u1=Math.max(anchor.getIntOr("u1",end.u1),end.u1);
        int v0=Math.min(anchor.getIntOr("v0",end.v0),end.v0),v1=Math.max(anchor.getIntOr("v1",end.v1),end.v1);
        int bu0=Math.floorDiv(u0,4),bu1=Math.floorDiv(u1,4),bv0=Math.floorDiv(v0,4),bv1=Math.floorDiv(v1,4);
        if(((long)bu1-bu0+1)*((long)bv1-bv0+1)>MAX_BLOCKS)return fail(player,"large");
        int axis=axis(face),u=uAxis(axis),v=vAxis(axis),step=face.getAxisDirection()==Direction.AxisDirection.POSITIVE?1:-1;
        int normal=Math.floorDiv(end.plane-(step>0?1:0),4),normalQuarter=Math.floorMod(end.plane-(step>0?1:0),4),normalHalf=normalQuarter/2;
        var changes=new ArrayList<Change>();int count=0;
        for(int a=bu0;a<=bu1;a++) for(int b=bv0;b<=bv1;b++) {
            int[] xyz=new int[3];xyz[axis]=normal;xyz[u]=a;xyz[v]=b;BlockPos pos=new BlockPos(xyz[0],xyz[1],xyz[2]);
            if(!level.hasChunkAt(pos))return fail(player,"unloaded");
            if(!(level.getBlockEntity(pos) instanceof PaintableBlockEntity block))continue;
            if(!level.mayInteract(player,pos) || !player.mayUseItemAt(pos,face,stack))return fail(player,"denied");
            int[] colors=block.snapshot(),before=colors.clone();
            if(block instanceof FinePaintBlockEntity fine) {
                int size=fine.miniSize()/4;
                if(end.plane%size!=0)continue;
                for(int qa=0;qa<4;qa+=size)for(int qb=0;qb<4;qb+=size) {
                    if(a*4+qa+size-1<u0||a*4+qa>u1||b*4+qb+size-1<v0||b*4+qb>v1)continue;
                    double[] point={pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5};
                    point[axis]=end.plane*.25;point[u]=a+(qa+size*.5)*.25;point[v]=b+(qb+size*.5)*.25;
                    int index=fine.exposedSquare(face,new net.minecraft.world.phys.Vec3(point[0],point[1],point[2]),level);
                    if(index<0)continue;
                    int next=paint==null?0:paint.ordinal();
                    boolean changed=false;
                    for(int square:fine.squareGroup(index)) {
                        if(colors[square]==next)continue;
                        colors[square]=next;changed=true;
                    }
                    if(changed) {count++;}
                }
            } else if(block instanceof PaintableCustomBlockEntity small) {
                if(end.plane%2!=0)continue;
                for(int qa=0;qa<2;qa++) for(int qb=0;qb<2;qb++) {
                    if(a*4+qa*2+1<u0 || a*4+qa*2>u1 || b*4+qb*2+1<v0 || b*4+qb*2>v1)continue;
                    int[] halves=new int[3];halves[axis]=normalHalf;halves[u]=qa;halves[v]=qb;
                    if(!exposed(level,pos,face,small,halves))continue;
                    int index=face.ordinal()*4+PaintableCustomBlockEntity.quadrant(face,halves[0],halves[1],halves[2]);
                    int next=paint==null?0:paint.ordinal();if(colors[index]==next)continue;
                    colors[index]=next;count++;
                }
            } else {
                boolean outer=end.plane==normal*4+(step>0?4:0);
                if(!outer)continue;
                BlockPos neighbor=pos.relative(face);
                if(outer&&(!level.hasChunkAt(neighbor) || level.getBlockState(neighbor).isFaceSturdy(level,neighbor,face.getOpposite())))continue;
                int index=face.ordinal(),next=paint==null?0:paint.ordinal();
                if(colors[index]!=next) { colors[index]=next;count++; }
            }
            if(!Arrays.equals(before,colors))changes.add(new Change(block,colors));
        }
        for(Change change:changes)change.block.apply(change.colors);
        clear(stack);player.containerMenu.broadcastChanges();
        player.sendOverlayMessage(Component.translatable("message.paintingmod.rectangle.done",count));return InteractionResult.SUCCESS;
    }
}
