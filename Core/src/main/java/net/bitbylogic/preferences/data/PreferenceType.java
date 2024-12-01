package net.bitbylogic.preferences.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class PreferenceType {

    private final String id;
    private final String friendlyName;

    private final Class<?> dataClass;
    private final Object defaultValue;

    private Set<Object> allowedValues = new HashSet<>();

}
