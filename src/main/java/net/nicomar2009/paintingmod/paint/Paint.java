package net.nicomar2009.paintingmod.paint;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Explicit IDs: 0 unpainted, 1 checker, 2..338 vanilla, 339..353 inactive archive. */
public final class Paint {
    public static final int PALETTE_VERSION = 2;
    private static final Paint[] COLORS = new Paint[354];
    public static final Paint UNPAINTED = new Paint(0, "minecraft:light_gray_concrete", false);
    public static final Paint DEFAULT = new Paint(1, null, true);
    static {
        COLORS[0] = UNPAINTED;
        COLORS[1] = DEFAULT;
        try (var stream = Paint.class.getResourceAsStream("/data/paintingmod/paint_catalog.json")) {
            if (stream == null) throw new IllegalStateException("Missing paint catalog");
            for (var element : JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray()) {
                var entry = element.getAsJsonObject();
                int id = entry.get("id").getAsInt();
                if (id < 2 || id > 338 || COLORS[id] != null) throw new IllegalStateException("Invalid paint ID " + id);
                COLORS[id] = new Paint(id, entry.get("block").getAsString(), true);
            }
            for (int id = 2; id <= 338; id++)
                if (COLORS[id] == null) throw new IllegalStateException("Incomplete paint catalog at " + id);
        } catch (java.io.IOException error) { throw new ExceptionInInitializerError(error); }
        for (int id = 339; id < COLORS.length; id++) COLORS[id] = new Paint(id, null, false);
    }
    private final int id;
    private final String blockId;
    private final boolean enabled;
    private Paint(int id, String blockId, boolean enabled) { this.id = id; this.blockId = blockId; this.enabled = enabled; }
    public int id() { return id; }
    public String blockId() { return blockId; }
    public boolean vanilla() { return id >= 2 && id <= 338; }
    public boolean refundable() { return vanilla(); }
    public boolean enabled() { return enabled; }
    public String itemName() { return vanilla() ? blockId.substring("minecraft:".length()) + "_paint" : "default_checker_paint"; }
    public static boolean validId(int id) { return id >= 0 && id < COLORS.length; }
    public static Paint fromId(int id) { return validId(id) ? COLORS[id] : UNPAINTED; }
    public static Paint[] values() { return COLORS.clone(); }
    public Paint rendered() { return enabled || this == UNPAINTED ? this : UNPAINTED; }
    /** Old IDs are decoded only when an old palette is explicitly identified. */
    public static int decode(int id, int palette) {
        if (palette < PALETTE_VERSION) return id == 16 ? 1 : id >= 1 && id <= 15 ? id + 338 : 0;
        return fromId(id).id;
    }
}
