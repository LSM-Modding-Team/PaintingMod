package net.nicomar2009.paintingmod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.nicomar2009.paintingmod.network.PaintCopyPayload;
import net.nicomar2009.paintingmod.registry.*;

@Mod(PaintingMod.MOD_ID)
public final class PaintingMod {
    public static final String MOD_ID = "paintingmod";

    public PaintingMod(IEventBus bus) {
        ModBlocks.BLOCKS.register(bus);
        ModPaintItems.initialize();
        ModItems.ITEMS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        ModCreativeTabs.TABS.register(bus);
        bus.addListener(PaintCopyPayload::register);
    }
}
