package net.nicomar2009.paintingmod.paint;

/** Saved IDs stay fixed: 0 is unpainted, 1..15 are reserved, 16 is the default checker.
 * Reserved slots preserve stored data without registering or loading disabled paints.
 */
public enum Paint {
    UNPAINTED("minecraft:block/light_gray_concrete"),
    RESERVED_01(null),
    RESERVED_02(null),
    RESERVED_03(null),
    RESERVED_04(null),
    RESERVED_05(null),
    RESERVED_06(null),
    RESERVED_07(null),
    RESERVED_08(null),
    RESERVED_09(null),
    RESERVED_10(null),
    RESERVED_11(null),
    RESERVED_12(null),
    RESERVED_13(null),
    RESERVED_14(null),
    RESERVED_15(null),
    DEFAULT("paintingmod:block/default_checker");

    private static final Paint[] COLORS = values();
    public final String texture;
    Paint(String texture) { this.texture = texture; }
    public static boolean validId(int id) { return id >= 0 && id < COLORS.length; }
    public static Paint fromId(int id) { return validId(id) ? COLORS[id] : UNPAINTED; }
    /** Inactive colors stay in saved data but render as unpainted until re-enabled. */
    public Paint rendered() { return texture == null ? UNPAINTED : this; }
}
