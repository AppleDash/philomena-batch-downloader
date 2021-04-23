package org.appledash.pbd.philomena.structs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This is mostly used for the purposes of predicable boxing.
 */
public record PhilomenaId(int rawId) {
    @JsonValue
    public int rawId() {
        return this.rawId;
    }

    @Override
    public String toString() {
        return Integer.toString(this.rawId);
    }

    @JsonCreator
    public static PhilomenaId of(int rawId) {
        return new PhilomenaId(rawId);
    }

    @JsonCreator
    public static PhilomenaId of(String rawId) {
        return new PhilomenaId(Integer.parseInt(rawId));
    }
}
