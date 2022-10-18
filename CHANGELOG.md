# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v4.0.4-1.19.2] - 2022-10-18
### Fixed
- Fixed modded anvil operations always costing only a single enchantment level

## [v4.0.3-1.19.2] - 2022-10-18
### Changed
- The mod no longer changes modded anvils in any way since they usually feature significant differences from the vanilla anvil
- Overhauled how changes to the anvil mechanic are applied, should no longer interfere with other mods on Forge (Fabric mods will need dedicated integration unfortunately, see `fuzs.easyanvils.api.event.AnvilUpdateCallback`)
- Reworked prior work penalty changes:
  - Costs are now set at a fixed increase (4 levels, configurable) per previous anvil operation which scales way less aggressively compared to vanilla. They can still be completely disabled in the config as it was before.
  - Renaming and just repairing will not increase the prior work penalty at all, when the max anvil level limit is exceeded those operations are still possible.

## [v4.0.2-1.19.2] - 2022-10-13
### Changed
- Renaming any item is now free and will not increase the prior work penalty, there is a config setting to change this back to only apply for name tags

## [v4.0.1-1.19.2] - 2022-10-03
### Added
- Added ability to use formatting codes in anvils and the new name tag interface; allows for stylizing item and entity names
- Find out more on the Minecraft Wiki: https://minecraft.fandom.com/wiki/Formatting_codes#Usage

## [v4.0.0-1.19.2] - 2022-09-29
- Initial release

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
