package net.bitbylogic.preferences;

import net.bitbylogic.orm.BormAPI;
import net.bitbylogic.preferences.data.Preference;
import net.bitbylogic.preferences.data.PreferenceType;
import net.bitbylogic.preferences.database.PreferenceTable;
import net.bitbylogic.preferences.serialize.PreferenceSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PreferenceContainerTest {

    private static final String TYPE_ID = "example";

    private final UUID userId = UUID.randomUUID();

    private PreferenceContainer container;
    private PreferenceTable table;

    @BeforeEach
    void setUp() {
        container = new PreferenceContainer();
        table = mock(PreferenceTable.class);
        container.registerPreferenceType(new PreferenceType(TYPE_ID, "Example", Boolean.class, true));
    }

    @SuppressWarnings("unchecked")
    private void initializeWithTable() {
        BormAPI bormAPI = mock(BormAPI.class);

        doAnswer(invocation -> {
            invocation.<Consumer<PreferenceTable>>getArgument(1).accept(table);
            return null;
        }).when(bormAPI).registerTable(eq(PreferenceTable.class), any(Consumer.class));

        container.initialize(bormAPI);
    }

    private void storeValue(String typeId, Object value) {
        Preference preference = mock(Preference.class);
        when(preference.getValue()).thenReturn(value);
        when(table.getPreferenceData(userId, typeId)).thenReturn(Optional.of(preference));
    }

    @Test
    void notReadyUntilTableRegistered() {
        assertFalse(container.isReady());
        assertNull(container.getTable());

        initializeWithTable();

        assertTrue(container.isReady());
        assertEquals(table, container.getTable());
    }

    @Test
    @SuppressWarnings("unchecked")
    void initializeTwiceRegistersTableOnce() {
        BormAPI bormAPI = mock(BormAPI.class);

        container.initialize(bormAPI);
        container.initialize(bormAPI);

        verify(bormAPI, times(1)).registerTable(eq(PreferenceTable.class), any(Consumer.class));
    }

    @Test
    void duplicateTypeRegistrationKeepsFirst() {
        container.registerPreferenceType(new PreferenceType(TYPE_ID, "Other", Boolean.class, false));

        assertEquals("Example", container.getType(TYPE_ID).orElseThrow().getFriendlyName());
    }

    @Test
    void preferenceTypesViewIsReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> container.getPreferenceTypes().clear());
    }

    @Test
    void unknownTypeReturnsNull() {
        Object value = container.getPreference(userId, "missing");

        assertNull(value);
    }

    @Test
    void getReturnsDefaultWhileTableMissing() {
        boolean value = container.getPreference(userId, TYPE_ID);

        assertTrue(value);
    }

    @Test
    void getReturnsDefaultWhenNothingStored() {
        initializeWithTable();
        when(table.getPreferenceData(userId, TYPE_ID)).thenReturn(Optional.empty());

        boolean value = container.getPreference(userId, TYPE_ID);

        assertTrue(value);
    }

    @Test
    void getDeserializesStoredValue() {
        initializeWithTable();
        storeValue(TYPE_ID, "false");

        boolean value = container.getPreference(userId, TYPE_ID);

        assertFalse(value);
    }

    @Test
    void getFallsBackToDefaultWhenDeserializerThrows() {
        container.registerPreferenceType(new PreferenceType("number", "Number", Integer.class, 7));
        initializeWithTable();
        storeValue("number", "not-a-number");

        int value = container.getPreference(userId, "number");

        assertEquals(7, value);
    }

    @Test
    void getFallsBackToDefaultOnWrongDeserializedType() {
        PreferenceSerializer wrongType = (type, object) -> 5;
        container.registerPreferenceType(new PreferenceType("wrong", "Wrong", Boolean.class, true, wrongType));
        initializeWithTable();
        storeValue("wrong", "x");

        boolean value = container.getPreference(userId, "wrong");

        assertTrue(value);
    }

    @Test
    void setIsIgnoredWhileTableMissing() {
        container.setPreference(userId, TYPE_ID, false);

        verify(table, never()).add(any());
    }

    @Test
    void setIgnoresUnknownType() {
        initializeWithTable();

        container.setPreference(userId, "missing", false);

        verify(table, never()).add(any());
    }

    @Test
    void setRejectsWrongDataType() {
        initializeWithTable();

        container.setPreference(userId, TYPE_ID, "not a boolean");

        verify(table, never()).add(any());
    }

    @Test
    void setRejectsDisallowedValue() {
        container.registerPreferenceType(new PreferenceType("mode", "Mode", String.class, "a",
                (type, object) -> String.valueOf(object), List.of("a", "b")));
        initializeWithTable();

        container.setPreference(userId, "mode", "c");

        verify(table, never()).add(any());
    }

    @Test
    void setRejectsSerializerThatThrows() {
        PreferenceSerializer throwing = (type, object) -> {
            throw new IllegalStateException("boom");
        };
        container.registerPreferenceType(new PreferenceType("bad", "Bad", String.class, "", throwing));
        initializeWithTable();

        container.setPreference(userId, "bad", "value");

        verify(table, never()).add(any());
    }

    @Test
    void setRejectsSerializerThatReturnsNonString() {
        PreferenceSerializer nonString = (type, object) -> 1;
        container.registerPreferenceType(new PreferenceType("bad", "Bad", String.class, "", nonString));
        initializeWithTable();

        container.setPreference(userId, "bad", "value");

        verify(table, never()).add(any());
    }

    @Test
    void setCreatesPreferenceWhenMissing() {
        initializeWithTable();
        when(table.getPreferenceData(userId, TYPE_ID)).thenReturn(Optional.empty());

        container.setPreference(userId, TYPE_ID, false);

        verify(table).add(any(Preference.class));
    }

    @Test
    void setUpdatesExistingPreference() {
        initializeWithTable();
        Preference preference = mock(Preference.class);
        when(table.getPreferenceData(userId, TYPE_ID)).thenReturn(Optional.of(preference));

        container.setPreference(userId, TYPE_ID, false);

        verify(preference).setValue("false");
        verify(preference).save();
        verify(table, never()).add(any());
    }

}
