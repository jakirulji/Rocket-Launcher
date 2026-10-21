# Sufi Wisdom — initial corpus import

Updated: 2026-10-21

## Imported source texts

These source files preserve Project Gutenberg headers, edition details, and license text. They are raw English source texts, not yet a full Bengali translation.

1. `sources/rumi_persian_mystics_davis_1907.txt`
   - Project Gutenberg eBook #45159, *The Persian Mystics: Jalálu'd-dín Rúmí*.
   - F. Hadland Davis, 1907 edition; includes selected passages from the Dīvān and Masnavi plus editorial commentary.
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
- Bengali translations are currently null and marked `pending_ai_simple_translation`. This is deliberate: no unreviewed machine translation is falsely labelled as verified. The intended app behavior is to create a plain Bengali translation using the selected on-device model, clearly label it as AI-assisted, and never alter the source text.
- These 100 records are a first import, not the promised 200+ verified passages per Sufi sub-sector. Avoid inflating the count by splitting one poem into multiple records.
- The text includes editorial selections and translations credited to different translators inside the 1907 book. Consult the preserved source header and book text for individual translator acknowledgements; do not attribute every line exclusively to Rumi's original-language wording.
- Project Gutenberg says the ebook is public domain in the USA and asks users outside the USA to check local law. This source was selected because the historical contributors' terms are expired under the general Bangladesh life-plus-60-year rule as understood for this project; recheck if legal advice is required.
