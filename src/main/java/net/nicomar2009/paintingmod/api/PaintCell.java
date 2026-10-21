package net.nicomar2009.paintingmod.api;

/** Stable surface ID and row-major piece-local 8px cell index. */
public record PaintCell(String surface,int index) {
    public PaintCell {
        if (surface==null || !surface.matches("[a-z0-9_./:-]{1,128}") || index<0 || index>=4096) throw new IllegalArgumentException("Invalid cell");
    }
}
