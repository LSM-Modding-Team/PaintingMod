package net.nicomar2009.paintingmod.api;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/** Explicit block-to-provider routing. Register resolved consumer blocks during common setup (enqueueWork).
 * No automatic instanceof discovery, no dependency on Core, no registration of consumer blocks.
 */
public final class PaintProviders {
    public record Registration(Identifier id,PaintProvider provider) {}
    private static final Map<Block,Registration> BLOCKS=new ConcurrentHashMap<>();
    private PaintProviders() {}
    public static void register(Identifier id,PaintProvider provider,Block... blocks) {
        Objects.requireNonNull(id); Objects.requireNonNull(provider);
        synchronized(BLOCKS) {
            if (blocks.length==0) throw new IllegalArgumentException("Missing blocks");
            for (Block block:blocks) if (BLOCKS.containsKey(Objects.requireNonNull(block))) throw new IllegalStateException("Paint provider already registered");
            for (var registration:BLOCKS.values())
                if (registration.id().equals(id) && registration.provider()!=provider) throw new IllegalStateException("Provider ID already registered");
            for (Block block:blocks) BLOCKS.put(block,new Registration(id,provider));
        }
    }
    public static Optional<Registration> find(Block block) { return Optional.ofNullable(BLOCKS.get(block)); }
}
