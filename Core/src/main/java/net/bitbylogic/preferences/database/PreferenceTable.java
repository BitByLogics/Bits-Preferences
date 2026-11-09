package net.bitbylogic.preferences.database;

import lombok.Getter;
import lombok.NonNull;
import net.bitbylogic.orm.BormAPI;
import net.bitbylogic.orm.data.BormTable;
import net.bitbylogic.preferences.data.Preference;
import net.bitbylogic.utils.Pair;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PreferenceTable extends BormTable<Preference> {

    protected final Map<Pair<UUID, String>, Preference> preferences = new ConcurrentHashMap<>();

    @Getter
    private volatile boolean loaded;

    public PreferenceTable(@NonNull BormAPI bormAPI) {
        super(bormAPI, Preference.class, "preferences", true);
    }

    /**
     * Retrieves the preference data for a specific user and preference type.
     *
     * @param userId The unique identifier of the user whose preference data is being retrieved.
     * @param typeId The ID of the preference type to look up.
     * @return An {@link Optional} containing the {@link Preference} object if found, or {@link Optional#empty()} if no data exists for the given user and type.
     * @throws NullPointerException If either {@code userId} or {@code typeId} is {@code null}.
     */
    public Optional<Preference> getPreferenceData(@NonNull UUID userId, @NonNull String typeId) {
        return Optional.ofNullable(preferences.get(new Pair<>(userId, typeId)));
    }

    @Override
    public void onDataLoaded() {
        for (Map.Entry<Object, Preference> entry : getDataMap().entrySet()) {
            Preference preference = entry.getValue();

            preferences.put(new Pair<>(preference.getUserId(), preference.getTypeId()), preference);
        }

        loaded = true;
    }

    @Override
    public void onDataAdded(@NonNull Preference preference) {
        preferences.put(new Pair<>(preference.getUserId(), preference.getTypeId()), preference);
    }

    @Override
    public void onDataDeleted(@NonNull Preference preference) {
        preferences.remove(new Pair<>(preference.getUserId(), preference.getTypeId()));
    }

}
