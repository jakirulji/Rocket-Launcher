# Sufi Wisdom — initial corpus import

Updated: 2026-10-21 (100 Bengali translations added)

## Imported source texts

These source files preserve Project Gutenberg headers, edition details, and license text. They are raw historical English translations/selections, not original Persian texts.

1. `sources/rumi_persian_mystics_davis_1907.txt`
   - Project Gutenberg eBook #45159, *The Persian Mystics: Jalálu'd-dín Rúmí*.
   - Project Gutenberg's 1920 edition of F. Hadland Davis's book; includes selected passages from the Dīvān and Masnavi plus editorial commentary. The original Gutenberg title page identifies London, 1920.
   - Source: https://www.gutenberg.org/ebooks/45159
2. `sources/rumi_mesnevi_redhouse_book1_1881.txt`
   - Project Gutenberg eBook #61724, *The Mesnevi*, Book I.
   - Rumi; translated and versified by Sir James W. Redhouse; 1881 edition.
   - Source: https://www.gutenberg.org/ebooks/61724
3. `sources/rumi_festival_of_spring_hastie_1903.txt`
   - Project Gutenberg eBook #57068, *The Festival of Spring, from the Díván of Jeláleddín*.
   - William Hastie, 1903 edition.
   - Source: https://www.gutenberg.org/ebooks/57068

## Structured passage records

- `records.jsonl` contains 100 titled selections extracted from the poetry-selection portion of eBook #45159.
- The English source text is retained for each record; section title and source line anchors are included.
- All 100 records now include `text_bn` with an AI-assisted plain Bengali translation; `translation_status` is `ai_assisted_simple_bengali_translation`. These are newly prepared translations, not published Bengali translations and not expert-certified. Keep the English text unchanged, retain source line anchors, and label the Bengali field as AI-assisted in the app.
- These 100 records are an initial Rumi selection, not the promised 200+ passages per Sufi sub-sector. Avoid inflating the count by splitting one poem into multiple records.
- The text includes editorial selections and translations credited to different translators inside the 1907 book. Consult the preserved source header and book text for individual translator acknowledgements; do not attribute every line exclusively to Rumi's original-language wording.
- Project Gutenberg says the ebook is public domain in the USA and asks users outside the USA to check local law. This source was selected because the historical contributors' terms are expired under the general Bangladesh life-plus-60-year rule as understood for this project; recheck if legal advice is required.


## Additional imports (2026-10-21)

The structured Sufi/wisdom records now contain 150 records in total: 120 Rumi selections, 20 Kabir songs, and 10 Sa'di/Gulistan wisdom selections. The newly added Rumi selections are from William Hastie's 1903 English rendering of the Dīván (Project Gutenberg #57068); Kabir selections are from Rabindranath Tagore's 1915 English translation (Project Gutenberg #6519); Sa'di selections are from James Ross's English translation in Project Gutenberg #13060.

All new Bengali fields are AI-assisted plain renderings; Sa'di story entries are summaries rather than certified literal translations. They are explicitly labelled as such and should not be presented as published Bengali translations. The source English text and source references are retained. Project Gutenberg marks these editions public domain in the USA; copyright status in Bangladesh and any other target jurisdiction must be reviewed before redistribution. This is an initial expansion, not a complete 200-per-author corpus. No full build was run because only data files and documentation changed.
