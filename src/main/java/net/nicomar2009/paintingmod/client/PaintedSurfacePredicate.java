package net.nicomar2009.paintingmod.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.supermartijn642.fusion.api.model.predicates.ModelPredicate;
import com.supermartijn642.fusion.api.util.Serializer;
import java.util.Locale;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nicomar2009.paintingmod.block.*;
import net.nicomar2009.paintingmod.paint.Paint;

/** Select the saved paint at each exposed cell; carved planes share the original face grid. */
public record PaintedSurfacePredicate(int divisions, int cube, Direction face, int paint) implements ModelPredicate {
    public static final Serializer<PaintedSurfacePredicate> SERIALIZER = new Serializer<>() {
        @Override public PaintedSurfacePredicate deserialize(JsonObject json) {
            int divisions = json.get("divisions").getAsInt();
            int cube = json.get("cube").getAsInt(), paint = json.get("paint").getAsInt();
            if ((divisions != 1 && divisions != 2 && divisions != 4)
                    || cube < 0 || cube >= divisions * divisions * divisions || !Paint.validId(paint))
                throw new JsonParseException("Invalid painted surface");
            return new PaintedSurfacePredicate(divisions, cube,
                    Direction.valueOf(json.get("face").getAsString().toUpperCase(Locale.ROOT)), paint);
        }
        @Override public JsonObject serialize(PaintedSurfacePredicate value) {
            JsonObject json = new JsonObject();
            json.addProperty("divisions", value.divisions);
            json.addProperty("cube", value.cube);
            json.addProperty("face", value.face.getSerializedName());
            json.addProperty("paint", value.paint);
            return json;
        }
    };

    @Override public boolean testForBlockState(BlockAndTintGetter view, BlockPos pos, BlockState state) {
        if (view == null || pos == null) return false;
        var entity = view.getBlockEntity(pos);
        int x = cube % divisions, y = (cube / divisions) % divisions, z = cube / (divisions * divisions);
        if (divisions == 4 && entity instanceof MiniCubePaintEntity mini)
            return mini.exposed(cube, face, view)
                    && mini.squarePaint(face, FinePaintBlockEntity.quadrant(face, x, y, z)).rendered().ordinal() == paint;
        if (divisions == 2 && entity instanceof PaintableCustomBlockEntity custom) {
            int nx = x + face.getStepX(), ny = y + face.getStepY(), nz = z + face.getStepZ();
            boolean internal = nx >= 0 && nx < 2 && ny >= 0 && ny < 2 && nz >= 0 && nz < 2;
            return custom.hasCube(cube) && (!internal || !custom.hasCube(nx + 2 * ny + 4 * nz))
                    && custom.squarePaint(face, PaintableCustomBlockEntity.quadrant(face, x, y, z)).rendered().ordinal() == paint;
        }
        return divisions == 1 && entity instanceof PaintableBlockEntity block && block.paint(face).rendered().ordinal() == paint;
    }

    @Override public boolean testForItem(ItemStack stack) { return false; }
    @Override public Serializer<? extends ModelPredicate> getSerializer() { return SERIALIZER; }
}
