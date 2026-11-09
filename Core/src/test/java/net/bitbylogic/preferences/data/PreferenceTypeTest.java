package net.bitbylogic.preferences.data;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreferenceTypeTest {

    @Test
    void shortConstructorProvidesDefaultSerializer() {
        PreferenceType type = new PreferenceType("id", "Name", Boolean.class, true);

        assertNotNull(type.getProcessor());
        assertTrue(type.getAllowedValues().isEmpty());
    }

    @Test
    void allowedValuesAreCopiedAndReadOnly() {
        List<Object> source = new ArrayList<>(List.of("a"));
        PreferenceType type = new PreferenceType("id", "Name", String.class, "a", (t, o) -> o, source);

        source.add("b");

        assertEquals(Set.of("a"), type.getAllowedValues());
        assertThrows(UnsupportedOperationException.class, () -> type.getAllowedValues().add("c"));
    }

    @Test
    void nullProcessorIsRejected() {
        assertThrows(NullPointerException.class, () -> new PreferenceType("id", "Name", String.class, "a", null));
    }

}
