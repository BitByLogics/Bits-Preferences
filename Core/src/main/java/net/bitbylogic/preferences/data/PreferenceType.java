package net.bitbylogic.preferences.data;

import lombok.Getter;
import lombok.NonNull;
import net.bitbylogic.preferences.serialize.DefaultPreferenceSerializer;
import net.bitbylogic.preferences.serialize.PreferenceSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
public class PreferenceType {

    private final String id;
    private final String friendlyName;

    private final Class<?> dataClass;
    private final @Nullable Object defaultValue;

    private final PreferenceSerializer processor;

    private final Set<Object> allowedValues;

    /**
     * Creates a preference type using the {@link DefaultPreferenceSerializer}, which supports
     * strings, numbers, booleans and enums.
     */
    public PreferenceType(@NonNull String id, @NonNull String friendlyName, @NonNull Class<?> dataClass, @Nullable Object defaultValue) {
        this(id, friendlyName, dataClass, defaultValue, new DefaultPreferenceSerializer(dataClass));
    }

    public PreferenceType(@NonNull String id, @NonNull String friendlyName, @NonNull Class<?> dataClass, @Nullable Object defaultValue,
                          @NonNull PreferenceSerializer processor) {
        this(id, friendlyName, dataClass, defaultValue, processor, Collections.emptySet());
    }

    public PreferenceType(@NonNull String id, @NonNull String friendlyName, @NonNull Class<?> dataClass, @Nullable Object defaultValue,
                          @NonNull PreferenceSerializer processor, @NonNull Collection<?> allowedValues) {
        this.id = id;
        this.friendlyName = friendlyName;
        this.dataClass = dataClass;
        this.defaultValue = defaultValue;
        this.processor = processor;
        this.allowedValues = Collections.unmodifiableSet(new HashSet<>(allowedValues));
    }

}
