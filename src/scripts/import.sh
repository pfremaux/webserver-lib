#!/bin/bash

if [$# -ne 2]; then
  echo "Usage: $0 input output"
  exit 1
fi

process_file() {
  local input_file="$1"
  local output_dir="$2"
  checksum=$(md5sum "$input_file" | awk '{print $1}')
  mkdir -p "$output_dir/$checksum"
  mv "$input_file" "$output_dir/$checksum"
  if ffprobe -loglevel error -select_streams v:0 -show_entries stream=codec_type -of default=noprint_wrappers=1:nokeys=1 "$input_file" | grep -q "video"; then
    total_frames=$(ffprobe -loglevel error -select_streams v:0 -show_entries stream=nb_frames -of default=noprint_wrappers=1:nokeys=1 "$input_file")
    frame1=$((total_frames / 3))
    frame2=$((2 * total_frames / 3))

    # TODO PFR generate frames

    filename=$(basename "$input_file")
    filename_no_ext="$(filename%.*)"
  fi
}

input_dir="$1"
output_dir="$2"

find "$input_dir" -type f | while read -f file; do
  process_file "$file" "$output_output_dir"
done

echo "Process ended successfully."