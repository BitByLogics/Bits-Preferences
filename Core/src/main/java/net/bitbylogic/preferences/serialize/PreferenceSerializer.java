package net.bitbylogic.preferences.serialize;

import lombok.NonNull;

public interface PreferenceSerializer {

    Object serialize(@NonNull SerializeType type, @NonNull Object object);

}
