package net.bitbylogic.preferences;

import net.bitbylogic.orm.BormAPI;
import net.bitbylogic.preferences.data.PreferenceType;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Tests {

    private static final File DATABASE_FILE = new File("testdb.sqlite");

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

    private static BormAPI bormAPI;
    private static PreferenceContainer container;

    @BeforeAll
    public void setup() {
        if (DATABASE_FILE.exists()) {
            DATABASE_FILE.delete();
        }

        try {
            bormAPI = new BormAPI(DATABASE_FILE);
            container = new PreferenceContainer(bormAPI);

            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public void cleanup() {
        bormAPI.close();

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
        container.registerPreferenceType(TEST_PREFERENCE_TYPE);

        // Set it for the test user
        container.setPreference(TEST_USER_ID, TEST_PREFERENCE_TYPE.getId(), false);

        // Retrieve their set preference
        boolean preferenceValue = container.getPreference(TEST_USER_ID, TEST_PREFERENCE_TYPE.getId());

        // Test
        assertFalse(preferenceValue, "Test user preference should be false");
    }

}
