<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# JPA Support Changelog

## [2.3.1]

- Compatible with IDEA251
- Support OpenAPI 3.0 thanks @VantStark
- Replace `Data` annotation with `Getter+Setter+SuperBuilder+NoArgsConstructor` thanks @VantStark

## [2.2.5]

### Added
- Compatible with IDEA243

## [2.2.4]

### Added
- Compatible with IDEA242

## [2.2.3]

### Fixed
- override the `getActionUpdateThread` method to avoid error reporting

## [2.2.2]

### Fixed
- Ignore the problem that the default value cannot be resolved as BigDecimal

### Added
- Compatible with IDEA241

## [2.2.1]
### Added
- Compatible with IDEA223

## [2.2.0]
### Added
- Compatible with IDEA232

## [2.1.0]
### Fixed
- Fixed a bug that could prevent the recognition of primary key field after extends with a parent class
- Support using `Jakarta EE` to replace `javax`

## [2.1.0-RC3]
### Added
- increase the compatibility of the database to resolve the database vendor

## [2.1.0-RC2]
### Fixed
- Fix npe when use file extension

## [2.1.0-RC1]
### Added
- Add `README_zh.md`
- Supports loading the source code template of the specified language through the file extension(Beta)

### Changed
- When column names are all uppercase, automatically convert to lowercase

## [2.0.10]
### Fixed
- Fix the problem of incorrect template: `MyBatis-Plus`
- Fix the issue that may trigger `Slow EDT Operations`

## [2.0.9]
### Fixed
- Some bugs fixed

## [2.0.8]
### Changed
- Some bugs fixed and optimized

## [2.0.8-RC3]
### Added
- Sort alphabetically when generating the selection table
- Supports automatic positioning to the first matching item during regular selection
- Allows to quickly search and locate items in the selection table
- Allows to further filter the database tables by configuring the schema
- Support auto-completion when entering database driver class

### Changed
- Optimize the merge rule: try to merge the missing part even if the field already exists

## [2.0.8-RC2]
### Changed
- Optimize the problem that the components cannot be fully displayed on some screens
- No longer freezes when there are too many data tables

### Fixed
- Some bugs fixed

## [2.0.8-RC1]
### Added
- Support generate Controller、Service、VO、DTO
- Support setting templates in the module directory, project directory, and home directory

### Changed
- Temporarily turn off the function of configuring templates in Settings
