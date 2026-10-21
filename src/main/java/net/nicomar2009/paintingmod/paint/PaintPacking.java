package net.nicomar2009.paintingmod.paint;

/** Explicit bit width keeps legacy zeros and numeric paint IDs unchanged.
 * Five-bit storage uses 12 paints per low word and the remainder in the high word.
 * Legacy 8px data keeps its original 16/8 split at three or four bits;
 * legacy 4px data keeps one four-bit word per face. No registry migration occurs here.
 */
public final class PaintPacking {
    private PaintPacking() {}

    public static long pack(int[] paints, int start, int count, int bits) {
        long packed = 0;
        for (int i = 0; i < count; i++) packed |= (long) paints[start + i] << (i * bits);
        return packed;
    }

    public static int unpack(long packed, int index, int bits) {
        return Paint.fromId((int) ((packed >>> (index * bits)) & ((1 << bits) - 1))).ordinal();
    }
}
