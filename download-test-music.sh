#!/bin/bash

# script downloads CC licensed music for testing
# Files are organized into test directories with proper metadata

set -e

TEST_DIR="test-music"
BASE_URL="https://archive.org/download"

echo "Creating test directory: $TEST_DIR"

mkdir -p "$TEST_DIR"
cd "$TEST_DIR"

download_album() {
    local album_name="$1"
    local artist="$2"
    local archive_id="$3" 
    local files=("${@:4}")
    
    echo "Downloading: $artist - $album_name"
    
    local album_dir="${artist} - ${album_name}"
    mkdir -p "$album_dir"
    cd "$album_dir"
    
    for file in "${files[@]}"; do
        echo "  ⬇️  $file"
        curl -s -L "${BASE_URL}/${archive_id}/${file}" -o "$file" || echo "  ❌ Failed to download $file"
    done
    
    cd ..
}


echo "Downloading Rock/Alternative samples..."

download_album "Electronic Test Album" "Various Artists" "jamendo-009958" \
    "01-bartlomiej-gajda-the-sound-of-rain.mp3" \
    "02-martijn-de-boer-dances-and-drones.mp3"

download_album "Jazz Collection" "Free Music Archive" "FMA_Various_Artists_Jazz" \
    "01-jazz-sample.flac" \
    "02-blues-sample.flac"

echo "Creating mixed format test albums..."

mkdir -p "Test Band - Mixed Format Album"
cd "Test Band - Mixed Format Album"

# create empty test files with proper extensions
touch "01-track.mp3"
touch "02-track.flac" 
touch "03-track.mp3"
touch "cover.jpg"

cd ..

# live release examples
mkdir -p "Live Band - Concert 2023-SAT-"
cd "Live Band - Concert 2023-SAT-"
touch "01-live-track.flac"
touch "02-encore.flac"
cd ..

mkdir -p "Test Artist - Studio Album"
cd "Test Artist - Studio Album"
touch "01-studio-track.mp3"
touch "02-studio-track.mp3"
cd ..

# VA
mkdir -p "VA - Compilation Album"
cd "VA - Compilation Album"
touch "01-artist1-track.mp3"
touch "02-artist2-track.flac"
cd ..

echo ""
echo "   To test with real music files:"
echo "   1. Replace placeholder files with actual CC licensed music"
echo "   2. Add proper ID3/metadata tags to test files"
echo "   3. Configure music-sorter.properties to use this directory as source"
echo ""
echo "Test directory ready at: $(pwd)"