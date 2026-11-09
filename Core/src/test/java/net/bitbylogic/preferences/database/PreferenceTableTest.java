package net.bitbylogic.preferences.database;

import net.bitbylogic.preferences.data.Preference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

class PreferenceTableTest {

    private PreferenceTable table;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        table = mock(PreferenceTable.class, CALLS_REAL_METHODS);

        Field field = PreferenceTable.class.getDeclaredField("preferences");
        field.setAccessible(true);
        field.set(table, new ConcurrentHashMap<>());
    }

    @Test
    void tracksAddedAndDeletedPreferences() {
        UUID userId = UUID.randomUUID();
        Preference preference = new Preference(UUID.randomUUID(), userId, "type", "value");

        table.onDataAdded(preference);
        assertEquals(preference, table.getPreferenceData(userId, "type").orElseThrow());

        table.onDataDeleted(preference);
        assertTrue(table.getPreferenceData(userId, "type").isEmpty());
    }

}
