package org.appledash.dbs.derpibooru.structs;

/**
 * This is mostly used for the purposes of predicable boxing.
 */
public record PhilomenaId(int rawId) {
    @Override
    public String toString() {
        return Integer.toString(this.rawId);
    }

    public static PhilomenaId of(int rawId) {
        return new PhilomenaId(rawId);
    }

    public static PhilomenaId of(String rawId) {
        return new PhilomenaId(Integer.parseInt(rawId));
    }
}
