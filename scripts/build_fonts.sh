#!/bin/bash

# Ensure to install FontTools:
#   pip install fonttools

SOURCE_FONT_DIR=./fonts
SDK_FONT_DIR=./virtusize/src/main/res/font
LOCALIZATION_DIRS=(
  ./virtusize/src/main/res
  ./virtusize-core/src/main/res
)

# Rename the font file name and it's metadata to ensure they match.
# The inner font name change is important, so it can be loaded as:
#   let font = UIFont(name: "NewFontName") 
rename_font() {
  local font_dir="$1"
  local font="$2"
  
  local font_name=$(basename "$font" .ttf)
  local new_name="subset_$font_name"

  # Extract TTX XML
  ttx -q "$font_dir/$font"
  local ttx_file="$font_dir/$font_name.ttx"

  # Rename Font inside the TTX file
  sed -i '' "s/$font_name/$new_name/g" "$ttx_file"

  # Re-generate font
  ttx -q -o "$font_dir/$new_name.ttf" "$ttx_file"

  # Clean up TTX file
  rm "$ttx_file"

  # Clean up original font file
  rm "$font_dir/$font"

  echo "Font renamed: $new_name"
}

# Compbine multiple localization files into one file.
# The file is stored into a single 
# Return file path
combine_localization_files() {
  local language=$1
  local tmp_dir=./build/tmp/font

  # Output file
  mkdir -p $tmp_dir
  local output_file=$tmp_dir/combined_strings_$language.xml

  # Clear the output file if it exists
  > $output_file

  # Loop through each directory in the array
  for dir in "${LOCALIZATION_DIRS[@]}"; do
    file=$dir/values-$language/strings.xml
    cat $file >> $output_file
    echo "\n" >> "$output_file"  # Add a newline for separation
  done

  echo $output_file
}

# Reduce font size by using only the characters from the localization files.
# The font is copied into the Virtusize Resources directory
# The font is also renamed (file and metadata), to ensure it's properly loaded by the iOS
generate_subset_font() {
  local font="$1"
  local language="$2"
  
  echo "Processing '$font' ..."

  local text_file=$(combine_localization_files $language)

  # create subset font
  pyftsubset ${SOURCE_FONT_DIR}/${font} \
    --output-file=${SDK_FONT_DIR}/${font} \
    --unicodes=U+0020-007E \
    --text-file=$text_file

  # rename font
  rename_font ${SDK_FONT_DIR} ${font}
}

# Japanese Regular
generate_subset_font noto_sans_jp_regular.ttf ja

# Japanese Bold
generate_subset_font noto_sans_jp_bold.ttf ja

# Korean Regular
generate_subset_font noto_sans_kr_regular.ttf ko

# Korean Bold
generate_subset_font noto_sans_kr_bold.ttf ko