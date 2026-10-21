package net.nicomar2009.paintingmod.paint;

/** Saved IDs 0..15 retain their original meaning. DEFAULT is a new state at ID 16.
 * These paintingmod entries do not alias or migrate any lsmmod registry IDs.
 */
public enum SchoolPaint {
    UNPAINTED("school_paintable_block", "minecraft:block/light_gray_concrete"),
    LIGHT("light_school_wall", "paintingmod:block/light_school_wall"),
    DARK("dark_school_wall", "paintingmod:block/dark_school_wall"),
    STREET("street_school_wall", "paintingmod:block/street_school_wall"),
    WHITE("classroom_white_wall", "paintingmod:block/classroom_white_wall"),
    FLOOR("classroom_floor", "paintingmod:block/classroom_floor"),
    YELLOW("yellow_school_block", "paintingmod:block/yellow_school_block"),
    PLAYGROUND_WHITE("white_playground_floor", "paintingmod:block/white_playground_floor"),
    PLAYGROUND_GRAY("gray_playground_floor", "paintingmod:block/gray_playground_floor"),
    PLAYGROUND_GREEN("green_playground_floor", "paintingmod:block/green_playground_floor"),
    PLAYGROUND_ORANGE("orange_playground_floor", "paintingmod:block/orange_playground_floor"),
    DARK_TILED("dark_tiled_floor", "paintingmod:block/dark_tiled_floor"),
    RED_STREET("red_street_wall", "paintingmod:block/red_street_wall"),
    LABORATORY_TILE("school_laboratory_tile", "paintingmod:block/school_laboratory_tile"),
    LABORATORY_TABLE_TILE("laboratory_table_tile", "paintingmod:block/laboratory_table_tile"),
    AUDITORIUM_PARQUET("auditorium_parquet", "paintingmod:block/auditorium_parquet"),
    DEFAULT("default_checker", "paintingmod:block/default_checker");

    public boolean refundable() { return this != UNPAINTED && this != DEFAULT; }

    private static final SchoolPaint[] COLORS = values();
    public static boolean validId(int id) { return id >= 0 && id < COLORS.length; }
    public final String blockName;
    public final String texture;
    SchoolPaint(String blockName, String texture) { this.blockName = blockName; this.texture = texture; }
    public static SchoolPaint fromId(int id) { return validId(id) ? COLORS[id] : UNPAINTED; }
}
