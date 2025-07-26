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

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreferenceContainer {

    private static final Logger LOGGER = Logger.getGlobal();

    @Getter
    private final HashMap<String, PreferenceType> preferenceTypes = new HashMap<>();

    @Getter
    private @Nullable PreferenceTable table;

    /**
     * Constructs a new {@code PreferenceContainer} and registers the {@link PreferenceTable}
     * with the provided {@link BormAPI} instance. The {@link PreferenceTable} is assigned
     * to the internal table field after registration.
     *
     * @param bormAPI The {@link BormAPI} instance used to register the {@link PreferenceTable}.
     * @throws NullPointerException If {@code bormAPI} is {@code null}.
     */
    public PreferenceContainer(@NonNull BormAPI bormAPI) {
        bormAPI.registerTable(PreferenceTable.class, preferenceTable -> this.table = preferenceTable);
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
            if (preferenceTypes.containsKey(type.getId())) {
                LOGGER.log(Level.WARNING, "Skipped registering preference type: " + type.getId() + ", it's already registered.");
                continue;
            }

            preferenceTypes.put(type.getId(), type);
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
     * Retrieves the user's preference value for the specified type. If no preference is found in the
     * database or if the preference type does not exist, the default value for the type is returned.
     *
     * @param <T>    The expected return type of the preference value.
     * @param userId The unique identifier of the user whose preference is being retrieved.
     * @param typeId The ID of the preference type to retrieve.
     * @return The user's preference value, or the default value of the preference type if no value
     * is found or the database is unavailable. Returns {@code null} if an error occurs or if
     * the preference type does not exist.
     * @throws NullPointerException If either {@code userId} or {@code typeId} is {@code null}.
     * @throws ClassCastException   If the retrieved value cannot be cast to the expected type {@code T}.
     */
    public <T> T getPreference(@NonNull UUID userId, @NonNull String typeId) {
        Optional<PreferenceType> optionalType = getType(typeId);

        if (optionalType.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Unable to locate preference type with ID: " + typeId);
            return null;
        }

        PreferenceType type = optionalType.get();

        if (table == null) {
            return (T) type.getDefaultValue();
        }

        try {
            Optional<Object> optionalValue = table.getPreferenceData(userId, typeId).map(Preference::getValue);

            if (optionalValue.isEmpty()) {
                return (T) type.getDefaultValue();
            }

            Object value = optionalValue.get();
            return (T) type.getProcessor().serialize(SerializeType.DESERIALIZE, value);
        } catch (ClassCastException e) {
            LOGGER.log(Level.SEVERE, "Unable to cast preference " + typeId);
            e.printStackTrace();
        }

        return null;
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
    public void setPreference(@NonNull UUID userId, @NonNull String typeId, @NonNull Object newValue) {
        Optional<PreferenceType> optionalType = getType(typeId);

        if (optionalType.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Unable to locate preference type with ID: " + typeId);
            return;
        }

        PreferenceType type = optionalType.get();

        if (table == null) {
            LOGGER.log(Level.WARNING, "Attempted to set preference " + type.getFriendlyName() + " for user " + userId + " to " + newValue + " failed. Preference table is missing.");
            return;
        }

        if (!ReflectionUtil.isType(newValue, type.getDataClass())) {
            LOGGER.log(Level.WARNING, "Attempted to set preference " + type.getFriendlyName() + " for user " + userId + " to " + newValue + " failed. Invalid data type.");
            return;
        }

        if(!type.getAllowedValues().isEmpty() && !type.getAllowedValues().contains(newValue)) {
            LOGGER.log(Level.WARNING, "Attempted to set preference " + type.getFriendlyName() + " for user " + userId + " to " + newValue + " failed. Disallowed value.");
            return;
        }

        String processedValue = (String) type.getProcessor().serialize(SerializeType.SERIALIZE, newValue);
        Optional<Preference> optionalPreference = table.getPreferenceData(userId, type.getId());

        if (optionalPreference.isEmpty()) {
            Preference preference = new Preference(UUID.randomUUID(), userId, type.getId(), processedValue);
            table.add(preference);
            return;
        }

        Preference preference = optionalPreference.get();
        preference.setValue(processedValue);
        preference.save();
    }

}
