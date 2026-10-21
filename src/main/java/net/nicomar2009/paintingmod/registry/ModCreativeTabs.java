package net.nicomar2009.paintingmod.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nicomar2009.paintingmod.PaintingMod;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PaintingMod.MOD_ID);
    public static final Supplier<CreativeModeTab> PAINTING = TABS.register("painting", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.paintingmod.painting"))
            .icon(() -> ModItems.PAINTABLE_BLOCK.get().getDefaultInstance())
            .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());
    private ModCreativeTabs() {}
}
