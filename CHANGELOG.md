# Changelog

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
