#!/bin/bash

FONTS_DIR=./virtusize/src/main/res/font
LOCALIZATION_DIRS=(
    ./virtusize/src/main/res
    ./virtusize-core/src/main/res
)
SKIP_CHARS=("0000c6d0") # skip '%' symbol, as it breaks the parsing

# Strategy:
#   1. Fetch all the glyphs from the font file
#   2. Prepare a list of unique unicode characters supported by font
#   3. Prepare a list of unique unicode characters used in localisation file
#   4. Compare lists
#
# Note:
#   Unfortuntely, old bash version doesn't support a feature 
#   when `echo '\ub6c7'` converts encoded symbol into actual character.
#   If it would, we could simply convert TTX unicodes into characters and
#   compare with localization files.
#
#   Instead, to ensure the script runs everywhere, we convert both - localization characters
#   and font subset glyphs into UTF-32 format: \U0000aaaa
#   And match those. Which works.
validate_font_symbols() {
    local font_file=$1
    local text_file=$2
    local tmp_dir=$3

    # Prepare the unicode lists from both, localization file and font subset
    {
        # Extract Font metadata
        ttx -q -t cmap -o $tmp_dir/font.ttx $font_file

        # Prepare Expected characters
        grep -o . $text_file | sort | uniq > $tmp_dir/text_chars.txt

        # Convert Font glyphs into list of unicodes in a UTF-32 format: \U12345678
        awk -F'"' '/<map code=/{print $2}' $tmp_dir/font.ttx | sort | uniq | while IFS= read -r code; do
            hex="$(printf '%08x' "$((code))")"
            echo "\\U$hex"
        done > $tmp_dir/font_unicodes.txt

        # Convert Text characters into list of unicodes in a UTF-32 format: \U12345678
        while IFS= read -r char; do
            hex="$(printf "$char" | iconv -f UTF-8 -t UTF-32BE | xxd -p)"
            # Skip some characters
            if [[ " ${SKIP_CHARS[@]} " =~ " $hex " ]]; then
                continue
            fi
            echo "\\U$hex"
        done < $tmp_dir/text_chars.txt | tee > $tmp_dir/text_unicodes.txt
    }

    # Validate all the necessary Unicode characters are preset in the font subset
    {
        local missing=0

        # Check if each Unicode in text_unicodes.txt is present in the font_unicodes.txt
        while IFS= read -r unicode; do
            if ! grep -q "$unicode" $tmp_dir/font_unicodes.txt; then
                echo "Missing character: $unicode"
                missing=1
            fi
        done < $tmp_dir/text_unicodes.txt

        # Output the result
        if [ $missing -eq 0 ]; then
            echo "SUCCESS: Localization texts are fully supported by the font '$(basename "$font_file")'."
        else
            echo "ERROR: Localization file '$text_file' is NOT fully supported by the font '$(basename "$font_file")'. See missing characters above."
            exit 1
        fi    
    }
}

# Wrapper function 
validate_font() {
    local font=$1
    local language=$2
    local tmp_dir="./build/tmp/font"
    local combined_text_file="$tmp_dir/combined_strings_$language.xml"

    # Prepare temp directory
    mkdir -p $tmp_dir

    # Combine multiple localization files into a single
    {
        > $combined_text_file # Clear the output file if it exists

        # Loop through each directory in the array
        for dir in "${LOCALIZATION_DIRS[@]}"; do
            file=$dir/values-$language/strings.xml
            cat $file >> $combined_text_file
            echo "\n" >> "$combined_text_file"  # Add a newline for separation
        done
    }

    # Merge remote i18n strings into the local localization file
    curl "https://i18n.virtusize.com/stg/bundle-payloads/aoyama/${language}" >> $combined_text_file

    # Validate font
    validate_font_symbols $FONTS_DIR/$font $combined_text_file $tmp_dir

    # Clean up temp directory
    rm -r $tmp_dir
}


# Japanese Regular
validate_font subset_noto_sans_jp_regular.ttf ja

# Japanese Bold
validate_font subset_noto_sans_jp_bold.ttf ja

# Korean Regular
validate_font subset_noto_sans_kr_regular.ttf ko

# Korean Bold
validate_font subset_noto_sans_kr_bold.ttf ko
