package net.bitbylogic.preferences.serialize;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultPreferenceSerializerTest {

    private enum Mode {
        FAST,
        SLOW
    }

    private Object roundTrip(Class<?> dataClass, Object value) {
        DefaultPreferenceSerializer serializer = new DefaultPreferenceSerializer(dataClass);
        Object serialized = serializer.serialize(SerializeType.SERIALIZE, value);

        assertEquals(String.class, serialized.getClass());
        return serializer.serialize(SerializeType.DESERIALIZE, serialized);
    }

    @Test
    void roundTripsSupportedTypes() {
        assertEquals(true, roundTrip(Boolean.class, true));
        assertEquals(42, roundTrip(Integer.class, 42));
        assertEquals(42L, roundTrip(long.class, 42L));
        assertEquals(1.5D, roundTrip(Double.class, 1.5D));
        assertEquals(2.5F, roundTrip(Float.class, 2.5F));
        assertEquals((short) 3, roundTrip(Short.class, (short) 3));
        assertEquals((byte) 4, roundTrip(Byte.class, (byte) 4));
        assertEquals("text", roundTrip(String.class, "text"));
        assertEquals(Mode.SLOW, roundTrip(Mode.class, Mode.SLOW));
    }

    @Test
    void unknownEnumConstantDeserializesToNull() {
        DefaultPreferenceSerializer serializer = new DefaultPreferenceSerializer(Mode.class);

        assertNull(serializer.serialize(SerializeType.DESERIALIZE, "MISSING"));
    }

    @Test
    void invalidNumberThrows() {
        DefaultPreferenceSerializer serializer = new DefaultPreferenceSerializer(Integer.class);

        assertThrows(NumberFormatException.class, () -> serializer.serialize(SerializeType.DESERIALIZE, "abc"));
    }

}
