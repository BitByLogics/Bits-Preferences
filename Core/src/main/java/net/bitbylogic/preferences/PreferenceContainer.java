package net.bitbylogic.preferences;

import lombok.Getter;
import lombok.NonNull;
import net.bitbylogic.orm.BormAPI;
import net.bitbylogic.preferences.data.Preference;
import net.bitbylogic.preferences.data.PreferenceType;
import net.bitbylogic.preferences.database.PreferenceTable;
import net.bitbylogic.preferences.serialize.SerializeType;
import net.bitbylogic.utils.reflection.ReflectionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreferenceContainer {

    private static final Logger LOGGER = Logger.getLogger(PreferenceContainer.class.getName());

    private final Map<String, PreferenceType> preferenceTypes = new ConcurrentHashMap<>();

    @Getter
    private volatile @Nullable PreferenceTable table;

    private boolean initialized;

    /**
     * Constructs a new {@code PreferenceContainer} without a database. Preference types can be registered
     * immediately, values fall back to their defaults until {@link #initialize(BormAPI)} completes.
     */
    public PreferenceContainer() {
    }

    /**
     * Constructs a new {@code PreferenceContainer} and immediately calls {@link #initialize(BormAPI)}.
     *
     * @param bormAPI The {@link BormAPI} instance used to register the {@link PreferenceTable}.
     * @throws NullPointerException If {@code bormAPI} is {@code null}.
     */
    public PreferenceContainer(@NonNull BormAPI bormAPI) {
        initialize(bormAPI);
    }

    /**
     * Registers the {@link PreferenceTable} with the provided {@link BormAPI}. The table becomes
     * available once its data has been registered. Calling this more than once logs a warning and is ignored.
     *
     * @param bormAPI The {@link BormAPI} instance used to register the {@link PreferenceTable}.
     * @throws NullPointerException If {@code bormAPI} is {@code null}.
     */
    public synchronized void initialize(@NonNull BormAPI bormAPI) {
        if (initialized) {
            LOGGER.log(Level.WARNING, "Preference container is already initialized.");
            return;
        }

        initialized = true;
        bormAPI.registerTable(PreferenceTable.class, preferenceTable -> table = preferenceTable);
    }

    /**
     * Checks whether the preference table is available for reading and writing.
     *
     * @return {@code true} if the table has been registered.
     */
    public boolean isReady() {
        return table != null;
    }

    /**
     * Retrieves a read-only view of all registered preference types.
     *
     * @return An unmodifiable view of the registered types, keyed by ID.
     */
    public Map<String, PreferenceType> getPreferenceTypes() {
        return Collections.unmodifiableMap(preferenceTypes);
    }

    /**
     * Registers one or more {@link PreferenceType} objects. If a preference type with the same ID is
     * already registered, it will be skipped, and a warning will be logged.
     *
     * @param types The {@link PreferenceType} objects to register. Multiple types can be registered at once.
     * @throws NullPointerException if any of the provided {@link PreferenceType} objects is {@code null}.
     */
    public void registerPreferenceType(@NonNull PreferenceType... types) {
        for (PreferenceType type : types) {
            if (preferenceTypes.putIfAbsent(type.getId(), type) != null) {
                LOGGER.log(Level.WARNING, "Skipped registering preference type: " + type.getId() + ", it's already registered.");
            }
        }
    }

    /**
     * Retrieves the {@link PreferenceType} associated with the given ID.
     *
     * @param id The ID of the preference type to retrieve.
     * @return An {@link Optional} containing the {@link PreferenceType} if found, or {@link Optional#empty()} if not found.
     * @throws NullPointerException if the provided {@code id} is {@code null}.
     */
    public Optional<PreferenceType> getType(@NonNull String id) {
        return Optional.ofNullable(preferenceTypes.get(id));
    }

    /**
     * Retrieves the user's preference value for the specified type. If no preference is stored, the
     * table is not ready, or the stored value is invalid, the default value for the type is returned.
     *
     * @param <T>    The expected return type of the preference value.
     * @param userId The unique identifier of the user whose preference is being retrieved.
     * @param typeId The ID of the preference type to retrieve.
     * @return The user's preference value or the type's default value. Returns {@code null} only if the
     * preference type does not exist or its default value is {@code null}.
     * @throws NullPointerException If either {@code userId} or {@code typeId} is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getPreference(@NonNull UUID userId, @NonNull String typeId) {
        Optional<PreferenceType> optionalType = getType(typeId);

        if (optionalType.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Unable to locate preference type with ID: " + typeId);
            return null;
        }

        PreferenceType type = optionalType.get();
        PreferenceTable currentTable = table;

        if (currentTable == null) {
            return (T) type.getDefaultValue();
        }

        Optional<Object> optionalValue = currentTable.getPreferenceData(userId, typeId).map(Preference::getValue);

        if (optionalValue.isEmpty()) {
            return (T) type.getDefaultValue();
        }

        try {
            Object value = type.getProcessor().serialize(SerializeType.DESERIALIZE, optionalValue.get());

            if (value == null || !ReflectionUtil.isType(value, type.getDataClass())) {
                LOGGER.log(Level.WARNING, "Stored value for preference " + typeId + " of user " + userId + " has an invalid type, using default.");
                return (T) type.getDefaultValue();
            }

            return (T) value;
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Unable to deserialize preference " + typeId + " of user " + userId + ", using default.", e);
            return (T) type.getDefaultValue();
        }
    }

    /**
     * Sets a user's preference value for the specified type. If the preference does not already exist,
     * it will be created. Validates that the new value is of the correct type for the preference.
     *
     * @param userId   The unique identifier of the user whose preference is being set.
     * @param typeId   The ID of the preference type to set.
     * @param newValue The new value to assign to the user's preference. Must match the data type
     *                 specified by the preference type.
     * @throws NullPointerException If any of the parameters {@code userId}, {@code typeId}, or
     *                              {@code newValue} is {@code null}.
     */
    public synchronized void setPreference(@NonNull UUID userId, @NonNull String typeId, @NonNull Object newValue) {
        Optional<PreferenceType> optionalType = getType(typeId);

        if (optionalType.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Unable to locate preference type with ID: " + typeId);
            return;
        }

        PreferenceType type = optionalType.get();
        PreferenceTable currentTable = table;

        if (currentTable == null) {
            LOGGER.log(Level.WARNING, "Attempted to set preference " + type.getFriendlyName() + " for user " + userId + " to " + newValue + " failed. Preference table is not ready.");
            return;
        }

        if (!ReflectionUtil.isType(newValue, type.getDataClass())) {
            LOGGER.log(Level.WARNING, "Attempted to set preference " + type.getFriendlyName() + " for user " + userId + " to " + newValue + " failed. Invalid data type.");
            return;
        }

        if (!type.getAllowedValues().isEmpty() && !type.getAllowedValues().contains(newValue)) {
            LOGGER.log(Level.WARNING, "Attempted to set preference " + type.getFriendlyName() + " for user " + userId + " to " + newValue + " failed. Disallowed value.");
            return;
        }

        Object processedValue;

        try {
            processedValue = type.getProcessor().serialize(SerializeType.SERIALIZE, newValue);
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Attempted to set preference " + type.getFriendlyName() + " for user " + userId + " failed. Unable to serialize value.", e);
            return;
        }

        if (!(processedValue instanceof String serializedValue)) {
            LOGGER.log(Level.WARNING, "Attempted to set preference " + type.getFriendlyName() + " for user " + userId + " failed. Serializer must produce a String.");
            return;
        }

        Optional<Preference> optionalPreference = currentTable.getPreferenceData(userId, type.getId());

        if (optionalPreference.isEmpty()) {
            currentTable.add(new Preference(UUID.randomUUID(), userId, type.getId(), serializedValue));
            return;
        }

        Preference preference = optionalPreference.get();
        preference.setValue(serializedValue);
        preference.save();
    }

}
