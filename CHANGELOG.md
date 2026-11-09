# Changelog

## 1.2.1

Fixes 1.2.0, whose database never started on a real server. Use this release instead of 1.2.0.

### Fixed
* The plugin now registers the SQLite and MySQL JDBC drivers explicitly, so the database starts on Paper
* Preferences saved before a restart are loaded again: the table now requests data preloading, which Bits-ORM 2.0.0 no longer does by default
* `PreferenceContainer#isReady()` is only true once stored data has finished loading, so early writes can no longer create duplicate rows

### Changed
* Bits-ORM 2.0.0
* The plugin library list uses HikariCP 6.2.1 and sqlite-jdbc 3.54.1.0
* Updated Maven plugins, test libraries and GitHub Actions

## 1.2.0

### Added
* `PreferenceContainer()` no-arg constructor, `initialize(BormAPI)` and `isReady()`
* `DefaultPreferenceSerializer` and a four-argument `PreferenceType` constructor
* Unit tests, JaCoCo, SpotBugs, source and javadoc jars
* README, CHANGELOG, LICENSE, SECURITY and CONTRIBUTING files
* CI, release workflow and Dependabot configuration

### Changed
* Dependencies now come from `repo.minebit.dev` instead of JitPack
* `getPreference` returns the default value instead of `null` for invalid stored values
* `PreferenceContainer#getPreferenceTypes()` and `PreferenceType#getAllowedValues()` are read-only views
* `BitsPreferences#getContainer()` is never `null`; database setup runs asynchronously
* `plugin.yml` version is taken from the project version
* The default config no longer ships example database credentials
* `bits-preferences-core` is no longer shaded

### Fixed
* `onDisable` no longer throws when the database was never created
* Thread safety of the preference cache and type registry
* Serializer failures no longer propagate to callers

## 1.1.0

* Initial Minebit release
