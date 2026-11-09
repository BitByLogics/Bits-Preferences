<div align="center">

# Bit's Preferences

[![Build](https://img.shields.io/github/actions/workflow/status/BitByLogics/Bits-Preferences/maven.yml?branch=master)](https://github.com/BitByLogics/Bits-Preferences/actions)
[![License](https://img.shields.io/github/license/BitByLogics/Bits-Preferences)](LICENSE)

A lightweight Paper plugin and library that lets plugins store per-player preferences with database storage out of the box.

</div>

## Features

* Typed preferences with a default value and an optional set of allowed values
* Built-in serialization for strings, numbers, booleans and enums, plus custom serializers
* SQLite out of the box, MySQL when configured
* Preferences are global, so plugins can read each other's preferences
* Safe to use before the database is ready: reads return the default value until it is

## Requirements

* Java 21
* Paper 1.21 or newer

## Installation

### Server owners

Download the latest `bits-preferences-bukkit` jar from the [releases page](https://github.com/BitByLogics/Bits-Preferences/releases) and place it in your `plugins` folder.

### Developers

```xml
<repository>
    <id>minebit</id>
    <url>https://repo.minebit.dev/releases</url>
</repository>

<dependency>
    <groupId>net.bitbylogic</groupId>
    <artifactId>bits-preferences-bukkit</artifactId>
    <version>1.2.0</version>
    <scope>provided</scope>
</dependency>
```

Add `depend: [BitsPreferences]` to your `plugin.yml`. The platform independent `bits-preferences-core` artifact is also published.

## Quick start

Register a preference type:

```java
PreferenceContainer container = BitsPreferences.getInstance().getContainer();

container.registerPreferenceType(
        new PreferenceType("example_preference", "Example Preference", Boolean.class, true)
);
```

Read a player's preference:

```java
boolean enabled = container.getPreference(player.getUniqueId(), "example_preference");
```

Change a player's preference:

```java
container.setPreference(player.getUniqueId(), "example_preference", false);
```

To restrict values or use a custom serializer, use the longer `PreferenceType` constructors.

## Configuration

`config.yml` supports `Track-Metrics` (bStats) and `Database-Details`. If `Address` or `Database` is empty, or the connection fails, the plugin falls back to SQLite using `database.sqlite` in the server's working directory.

## Notes

* Reads return the type's default value while the database is still connecting, and writes are ignored with a warning until it is ready. Use `PreferenceContainer#isReady()` to check.
* `getPreference` returns `null` only for unknown type IDs or when the default value is `null`.
* Invalid or unreadable stored values are logged and replaced by the default value on read.
* Not yet tested on a real Folia server.

## Building

```bash
mvn clean verify
```

This runs the tests, JaCoCo and SpotBugs.

## Releasing

Update the version in all `pom.xml` files, update `CHANGELOG.md`, then push a tag that matches the version exactly, for example `1.2.0`. The Release workflow checks the tag, deploys to `repo.minebit.dev` and creates the GitHub release.

## License

[MIT](LICENSE)
