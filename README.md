MUSIC-SORTER
============

Music library organizer with two operation modes:

1. **Standard Mode**: Move and organize files by format and genre
2. **Collection Mode**: Create organized views using links (no file moving)

## Usage

```bash
# Standard mode - moves files
java -jar music-sorter.jar

# Collection mode - creates organized links  
java -jar music-sorter.jar --collection
```

Creates config file on first run: `$HOME/.config/music-sorter/music-sorter.properties`

## Output Structure

**Standard Mode**: Organizes by format and genre  
**Collection Mode**: Creates multiple views with help of symlinks (by-year, by-genre, by-artist, by-format)

## Native Binary

Build standalone executable (10ms startup, 50MB memory):

```bash
# Install GraalVM 21+
sdk install java 21.0.2-graalce

# Build native binary
./build.sh
```

Creates: `./build/music-sorter` (26MB, no JVM required)

## Development

**Code Quality Tools:**

```bash
# Install Git hooks (auto-format, tests on push)
./setup-hooks.sh

# Manual formatting
mvn spotless:apply
```

**Configuration Example:**

```
source=/music/incoming
target=/music/sorted
live_releases_skip=true
live_releases_patterns=-SAT-,-DVBS-,-SBD-
# Optional: Enable checksum validation (disabled by default)
checksum_validation_enabled=false
```

## Features

- **SFV/MD5 checksum validation** - Verifies file integrity before processing (optional)
- **Cross-platform support** - Works on Windows, macOS, and Linux
- **Live release filtering** - Skip concert recordings and bootlegs
- **Collection views** - Multiple organization formats (by-year, by-genre, etc.)

