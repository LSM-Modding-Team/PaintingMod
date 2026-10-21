package net.nicomar2009.paintingmod.paint;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Nine-bit words: seven IDs per word. Legacy words are decoded before validating new IDs. */
public final class PaintStorage {
    private PaintStorage() {}
    public static void header(ValueOutput output) {
        output.putInt("paint_palette", Paint.PALETTE_VERSION);
        output.putInt("paint_bits", 9);
    }
    public static void write(ValueOutput output, int[] paints, int start, int count, String... keys) {
        for (int word = 0; word < keys.length; word++) {
            int offset = word * 7, length = Math.min(7, Math.max(0, count - offset));
            output.putLong(keys[word], PaintPacking.pack(paints, start + offset, length, 9));
        }
    }
    public static void read(ValueInput input, int[] paints, int start, int count, int legacyBits, String... keys) {
        int bits = input.getIntOr("paint_bits", legacyBits);
        int palette = input.getIntOr("paint_palette", 1);
        if (bits != 9 && bits != 5 && bits != 4 && bits != 3) bits = legacyBits;
        int split = bits == 9 ? 7 : bits == 5 ? 12 : 16;
        for (int i = 0; i < count; i++) {
            int word = i / split, shift = (i % split) * bits;
            long packed = word < keys.length ? input.getLongOr(keys[word], 0) : 0;
            int raw = (int) ((packed >>> shift) & ((1L << bits) - 1));
            paints[start + i] = Paint.decode(raw, palette);
        }
    }
}
