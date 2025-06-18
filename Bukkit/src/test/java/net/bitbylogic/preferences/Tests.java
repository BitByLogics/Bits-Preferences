package net.bitbylogic.preferences;

import net.bitbylogic.preferences.data.PreferenceType;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Tests {

    private static final File TEST_CONFIG = new File("config.yml");
    private static final File DATABASE_FILE = new File("database.sqlite");

    private static final PreferenceType TEST_PREFERENCE_TYPE = new PreferenceType(
            "test_preference",
            "Test Preference",
            Boolean.class,
            true,
            (type, object) -> switch (type) {
                case SERIALIZE -> object.toString();
                case DESERIALIZE -> Boolean.parseBoolean(object.toString());
            }
    );

    private static final UUID TEST_USER_ID = UUID.randomUUID();

    private BitsPreferences plugin;

    @BeforeAll
    public void setup() {
        MockBukkit.mock();
        this.plugin = MockBukkit.loadWithConfig(BitsPreferences.class, TEST_CONFIG);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public void cleanup() {
        MockBukkit.unmock();

        if (!DATABASE_FILE.exists()) {
            return;
        }

        DATABASE_FILE.delete();
    }

    @Test
    @DisplayName("Set User Preference")
    @Order(1)
    public void setUserPreference() {
        // Register test preference
        plugin.getContainer().registerPreferenceType(TEST_PREFERENCE_TYPE);

        // Set it for the test user
        plugin.getContainer().setPreference(TEST_USER_ID, TEST_PREFERENCE_TYPE.getId(), false);

        // Retrieve their set preference
        boolean preferenceValue = plugin.getContainer().getPreference(TEST_USER_ID, TEST_PREFERENCE_TYPE.getId());

        // Test
        assertFalse(preferenceValue, "Test user preference should be false");
    }

}
