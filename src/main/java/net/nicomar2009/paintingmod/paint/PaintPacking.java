package net.nicomar2009.paintingmod.paint;

/** Packs explicitly sized palette words; callers keep all shifts below 64 bits. */
public final class PaintPacking {
    private PaintPacking() {}

    public static long pack(int[] paints, int start, int count, int bits) {
        long packed = 0;
        for (int i = 0; i < count; i++) packed |= (long) paints[start + i] << (i * bits);
        return packed;
    }

}
