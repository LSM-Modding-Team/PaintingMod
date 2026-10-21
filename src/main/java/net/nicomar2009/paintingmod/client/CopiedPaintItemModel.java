package net.nicomar2009.paintingmod.client;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.nicomar2009.paintingmod.paint.CopiedPaintPattern;
import net.nicomar2009.paintingmod.registry.ModBlocks;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/** A copied palette looks like its source block until its one-shot paste is consumed. */
final class CopiedPaintItemModel implements ItemModel {
    private static final Identifier BASE = Identifier.fromNamespaceAndPath("paintingmod", "block/default_cube");
    private final ItemModel fallback;
    private final ModelRenderProperties properties;
    private final Matrix4fc transform;
    private CopiedPaintItemModel(ItemModel fallback, ModelRenderProperties properties, Matrix4fc transform) {
        this.fallback = fallback; this.properties = properties; this.transform = new Matrix4f(transform);
    }
    static void register(RegisterItemModelsEvent event) {
        event.register(Identifier.fromNamespaceAndPath("paintingmod", "copied_pattern"), Unbaked.CODEC);
    }
    @Override public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
            ItemDisplayContext context, ClientLevel level, ItemOwner owner, int seed) {
        CopiedPaintPattern pattern = CopiedPaintPattern.read(stack);
        PaintedBlockModel model = pattern == null ? null : PaintModels.preview(pattern.divisions());
        if (model == null) { fallback.update(output, stack, resolver, context, level, owner, seed); return; }
        var state = switch (pattern.divisions()) {
            case 2 -> ModBlocks.PAINTABLE_CUSTOM_BLOCK.get().defaultBlockState();
            case 4 -> ModBlocks.PAINTABLE_MINI_CUBE_BLOCK.get().defaultBlockState();
            default -> ModBlocks.PAINTABLE_BLOCK.get().defaultBlockState();
        };
        var parts = new ArrayList<BlockStateModelPart>();
        model.collectCopiedParts(pattern.modelData(), level == null ? BlockAndTintGetter.EMPTY : level,
                pattern.origin(), state, parts);
        var quads = new ArrayList<BakedQuad>();
        for (var part : parts) {
            for (Direction face : Direction.values()) quads.addAll(part.getQuads(face));
            quads.addAll(part.getQuads(null));
        }
        output.appendModelIdentityElement(this);
        output.appendModelIdentityElement(stack.get(DataComponents.CUSTOM_DATA));
        var layer = output.newLayer();
        layer.setLocalTransform(transform);
        properties.applyToLayer(layer, context);
        layer.prepareQuadList().addAll(quads);
        layer.setExtents(() -> CuboidItemModelWrapper.computeExtents(quads));
        if (stack.hasFoil()) { layer.setFoilType(ItemStackRenderState.FoilType.STANDARD); output.setAnimated(); }
        for (BakedQuad quad : quads)
            if ((quad.materialInfo().flags() & BakedQuad.FLAG_ANIMATED) != 0) { output.setAnimated(); break; }
    }
    public record Unbaked(ItemModel.Unbaked fallback) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = ItemModels.CODEC.fieldOf("fallback").xmap(Unbaked::new, Unbaked::fallback);
        @Override public MapCodec<Unbaked> type() { return CODEC; }
        @Override public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(BASE); fallback.resolveDependencies(resolver);
        }
        @Override public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {
            var baker = context.blockModelBaker();
            var model = baker.getModel(BASE);
            var properties = ModelRenderProperties.fromResolvedModel(baker, model, model.getTopTextureSlots());
            return new CopiedPaintItemModel(fallback.bake(context, transform), properties, transform);
        }
    }
}
