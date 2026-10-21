package net.nicomar2009.paintingmod.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.nicomar2009.paintingmod.paint.Paint;
import net.nicomar2009.paintingmod.tool.PaintItem;

public final class ModPaintItems {
    private static final Map<Integer, DeferredItem<PaintItem>> PAINTS = new LinkedHashMap<>();
    public static final DeferredItem<PaintItem> DEFAULT_CHECKER_PAINT = register(Paint.DEFAULT);
    static { for (Paint paint : Paint.values()) if (paint.vanilla()) register(paint); }
    private static DeferredItem<PaintItem> register(Paint paint) {
        var item = ModItems.ITEMS.registerItem(paint.itemName(), properties -> new PaintItem(paint, properties));
        PAINTS.put(paint.id(), item);
        return item;
    }
    public static Item refundItem(Paint paint) { return paint.refundable() ? PAINTS.get(paint.id()).get() : null; }
    public static void initialize() {}
    private ModPaintItems() {}
}
