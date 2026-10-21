package net.nicomar2009.paintingmod.registry;

import net.neoforged.neoforge.registries.DeferredItem;
import net.nicomar2009.paintingmod.paint.Paint;
import net.nicomar2009.paintingmod.tool.PaintItem;

/** Only enabled paints are registered. Disabled paint sources are outside src/main. */
public final class ModPaintItems {
    public static final DeferredItem<PaintItem> DEFAULT_CHECKER_PAINT = ModItems.ITEMS.registerItem(
            "default_checker_paint", properties -> new PaintItem(Paint.DEFAULT, properties));

    public static void initialize() {}
    private ModPaintItems() {}
}
