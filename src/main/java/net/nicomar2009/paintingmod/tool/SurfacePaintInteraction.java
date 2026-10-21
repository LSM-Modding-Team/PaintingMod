package net.nicomar2009.paintingmod.tool;

import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.nicomar2009.paintingmod.PaintingMod;

/** Let tools reach their item hook before a consumer door consumes the click to open.
 * Respect event cancellation and item-use denial from protection mods; do not force item use.
 */
@EventBusSubscriber(modid=PaintingMod.MOD_ID)
public final class SurfacePaintInteraction {
    private SurfacePaintInteraction() {}
    private static boolean isTool(Item item) {
        return item instanceof PaintItem || item instanceof SandpaperItem || item instanceof PaintCleanerItem || item instanceof HammerItem;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void use(PlayerInteractEvent.RightClickBlock event) {
        if (!SurfacePaintTools.registered(event.getLevel(),event.getPos())) return;
        if (isTool(event.getItemStack().getItem()) || event.getHand()==InteractionHand.MAIN_HAND
                && event.getItemStack().isEmpty() && isTool(event.getEntity().getOffhandItem().getItem()))
            event.setUseBlock(TriState.FALSE);
    }
}
