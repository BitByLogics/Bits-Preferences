package net.bitbylogic.preferences.serialize;

import lombok.NonNull;

public class DefaultPreferenceSerializer implements PreferenceSerializer {

    private final Class<?> dataClass;

    public DefaultPreferenceSerializer(@NonNull Class<?> dataClass) {
        this.dataClass = dataClass;
    }

    @Override
    public Object serialize(@NonNull SerializeType type, @NonNull Object object) {
        if (type == SerializeType.SERIALIZE) {
            if (object instanceof Enum<?> enumValue) {
                return enumValue.name();
            }

            return String.valueOf(object);
        }

        String value = String.valueOf(object);

        if (dataClass == Boolean.class || dataClass == boolean.class) {
            return Boolean.parseBoolean(value);
        }

        if (dataClass == Integer.class || dataClass == int.class) {
            return Integer.parseInt(value);
        }

        if (dataClass == Long.class || dataClass == long.class) {
            return Long.parseLong(value);
        }

        if (dataClass == Double.class || dataClass == double.class) {
            return Double.parseDouble(value);
        }

        if (dataClass == Float.class || dataClass == float.class) {
            return Float.parseFloat(value);
        }

        if (dataClass == Short.class || dataClass == short.class) {
            return Short.parseShort(value);
        }

        if (dataClass == Byte.class || dataClass == byte.class) {
            return Byte.parseByte(value);
        }

        if (dataClass.isEnum()) {
            for (Object constant : dataClass.getEnumConstants()) {
                if (((Enum<?>) constant).name().equals(value)) {
                    return constant;
                }
            }

            return null;
        }

        return value;
    }

}
