# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v4.0.10-1.19.2] - 2023-03-15
### Changed
- Renaming / repairing no longer accounts for the prior work penalty of an item
- The 'Too Expensive!' limit is now disabled by default once more
- Overhauled server config file
  - Separated all options into three categories
  - Added a few new options for controlling different costs, like multipliers for enchantment rarities and repairs
  - Improved many descriptions to hopefully be easier to understand
### Fixed
- Fixed an issue where the 'Too Expensive!' label wouldn't show

## [v4.0.9-1.19.2] - 2023-03-10
### Changed
- Easy Anvils now internally converts formatted item names to Minecraft's component based text format for saving instead of saving raw strings using the legacy chat formatting format
- This change should help with other mods that see `§` as an invalid character in item names, causing issues
- Overhauled the anvil rename edit box with a lot of useful text manipulation features, included as an experimental feature to test as a possible future standalone text box enhancement mod
- Most features are modelled after text handling on macOS, not everything is implemented yet (notably missing right now is double-click+dragging to select individual words)
  - Right-click the edit box to clear
  - Double-click a word to select that word
  - Triple-click anywhere in the box to select everything
  - Click+drag across text to select all characters dragged above
  - `Alt`+`Delete` to delete the word left of the cursor
  - `Ctrl`/`Cmd`+`Delete` to delete all characters left of the cursor
  - `Alt`+`Left Arrow`/`Right Arrow` to move between words
  - `Ctrl`/`Cmd`+`Left Arrow`/`Right Arrow` to move to the beginning/end of the text box
  - `Shift`+`Alt`+`Left Arrow`/`Right Arrow` to select individual words
  - `Shift`+`Ctrl`/`Cmd`+`Left Arrow`/`Right Arrow` to select everything to the beginning/end of the text box
### Fixed
- Fixed a crash when right-clicking anvils on Forge for real this time
- Fixed a crash when entering Emojis into the anvil rename / name tag edit box
- Fixed the anvil rename / name tag edit box allowing fewer characters than vanilla when formatting codes are used

## [v4.0.8-1.19.2] - 2023-03-07
### Fixed
- Fixed a crash when right-clicking anvils on Forge introduced in the last version

## [v4.0.7-1.19.2] - 2023-03-06
### Changed
- Easy Anvil now is much more careful when applying any changes to anvil mechanics in regard to other mods
- All changes to those mechanics from any other mod take precedence (like a new anvil recipe), Easy Anvils only takes effect if the outcome would be the same as vanilla
- This change is necessary to offer out-of-the-box compatibility with many mods which previously broke due to Easy Anvils

## [v4.0.6-1.19.2] - 2022-11-15
### Fixed
- Fixed compatibility with Apotheosis (Forge) and Things (Fabric) mod

## [v4.0.5-1.19.2] - 2022-10-19
### Fixed
- Fixed compatibility with Ledger mod

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
