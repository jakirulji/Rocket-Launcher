# Sufi Wisdom — corpus status

Updated: 2026-10-21 (170 records; includes 10 Bustān selections)

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

The structured Sufi/wisdom records now contain 170 records in total: 120 Jalal al-Din Rumi selections, 20 Kabir selections, 20 Sa'di selections (10 Gulistan story summaries and 10 Bustān excerpts), and 10 Hafiz of Shiraz selections.

All new Bengali fields are AI-assisted plain renderings; Sa'di story entries are summaries rather than certified literal translations. They are explicitly labelled as such and should not be presented as published Bengali translations. The source English text and source references are retained. Project Gutenberg marks these editions public domain in the USA; copyright status in Bangladesh and any other target jurisdiction must be reviewed before redistribution. This is an initial expansion, not a complete 200-per-author corpus. No full build was run because only data files and documentation changed.


Additional Hafiz import: 10 selections from Gertrude Lowthian Bell's 1897 English translation, *Poems from the Divan of Hafiz* (Project Gutenberg eBook #74883), were added with AI-assisted Bengali plain renderings/summaries. The Bengali text is not a published translation or expert-certified. Source English and links are retained. Project Gutenberg labels the edition public domain in the USA; review Bangladesh/local copyright status before redistribution.


## Additional Sa'di import — The Bustān of Sadi

- Added 10 short excerpts from A. Hart Edwards's 1911 English translation, Project Gutenberg eBook #60471.
- Each record includes the exact English excerpt (line wrapping normalized only), a Bengali rendering, a section label, a source URL, and line anchors in the downloaded source text.
- Bengali fields are AI-assisted translations of the English translation, not direct translations from Persian and not expert-certified.
- Project Gutenberg labels the edition public domain in the USA. Bangladesh/local copyright status is not independently confirmed; review before redistribution.

## Remaining work

- This is not a complete Sufi corpus and does not meet the 200-record target. More Bengali and South Asian Sufi figures remain to be researched.
- Bengali Lalon material: Wikimedia Commons has a scan of *Lalon Shah O Lalon Geetika* (1899) marked public domain, but it is a scan, not proofread machine-readable text. Do not claim song-level integration until transcription and page-by-page checking are complete.
- Do not run a build for this data-only change; app integration and all other sector targets remain separate unfinished work.
