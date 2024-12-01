<div align="center">
<img src="https://i.imgur.com/mSJCRZ2.png" style="width: 20%;" alt="Icon">

## Bit's Preferences    
[![Build](https://img.shields.io/github/actions/workflow/status/BitByLogics/Bits-Preferences/.github/workflows/maven.yml?branch=master)](https://github.com/BitByLogics/Bits-Preferences/actions)
![Issues](https://img.shields.io/github/issues-raw/BitByLogics/Bits-Preferences)
[![Stars](https://img.shields.io/github/stars/BitByLogics/Bits-Preferences)](https://github.com/BitByLogics/Bits-Preferences/stargazers)
[![Chat)](https://img.shields.io/discord/1310486866272981002?logo=discord&logoColor=white)](https://discord.gg/syngw2UQUd)

<a href="/#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/compact/supported/spigot_46h.png" height="35"></a>
<a href="/#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/compact/supported/paper_46h.png" height="35"></a>
<a href="/#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/compact/supported/purpur_46h.png" height="35"></a>

**Bit's Preferences** is an all-in-one solution for allowing your users to set preferences for themselves.
</div>

## Features
* Generics support
* Simple preference type registration
* Database storage out of the box (SQL/SQLite)
* Default preference value
* Preferences are global, allowing plugins to view preferences from one another.

## Planned Features
- [ ] Admin command for modifying users preferences
- [ ] Menu allowing players to modify their preferences

## API

To register a new PreferenceType:

```java
PreferenceContainer container = BitsPreferences.getInstance().getContainer();

container.registerPreferenceType(
        new PreferenceType(
                "example_preference",
                "Example Preference",
                Boolean.class,
                true)
);
```

To get a user's preference:

```java
PreferenceContainer container = BitsPreferences.getInstance().getContainer();

Player player;

boolean preference = container.getPreference(player.getUniqueId(), "example_preference");
```

To set a user's preference:

```java
PreferenceContainer container = BitsPreferences.getInstance().getContainer();

Player player;

container.setPreference(player.getUniqueId(), "example_preference", false);
```
