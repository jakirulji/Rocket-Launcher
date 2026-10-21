# Soul Space dataset workspace

This directory is the source-controlled home for the seven independent offline Soul Space datasets. Data should be stored separately from UI code and include a stable reference, language, attribution, licence, and record type for each entry.

## Dataset targets

| Dataset | Target | Current bundled status |
|---|---:|---|
| Qur’an | 300–500 | 14 selected Arabic verse records; full Arabic/Bengali/English source files also bundled |
| Sufi wisdom | 200–500 | 160 quote records: Rumi 120, Kabir 20, Sa'di Bustān 10, Hafiz 10; Gulistan story summaries excluded; Bengali renderings are AI-assisted and need review |
| Philosophy & literature | 200–500 | Only a small starter paraphrase exists; source-verified corpus expansion remains |
| Bhagavad Gita | 200 | 17 records, including 15 newly selected Sanskrit verse excerpts; translations are Soul Space renderings and require verse-level review |
| Dhammapada | 200 | 22 Pāli verse records, including 20 newly selected verses with Soul Space English/Bengali renderings |
| Guru Granth Sahib | 200 | Bengali translation excluded pending express approval; current entries are paraphrases, not verbatim Gurbani |
| Original reflections | 200 | Small original set exists; expansion and quality review remain |

## Data integrity rules

- Keep source text unchanged when the source licence prohibits modification; put Bengali/English translations in separate fields and label them as original translations or paraphrases.
- Every record must have a stable book/verse/section reference and a source URL.
- Every bundled source must be recorded in ATTRIBUTION.md with the exact licence notice and upstream contributors.
- Do not count one passage split into multiple records as multiple independent quotations.
- Never mark the dataset complete until automated record counts and reference checks pass.

## Current source status

- `dhammapada/pali.txt` is the Pāli source text from ETCBC/dhammapada under its upstream MIT licence. `dhammapada/entries.json` now contains 22 selected verse records; English and Bengali fields are Soul Space renderings, not published translations.
- `sufi-wisdom/sources/` now contains three historical English Rumi source texts with Project Gutenberg headers and license notices preserved.
- `sufi-wisdom/records.jsonl` contains 160 quote records across Rumi, Kabir, Sa'di Bustān, and Hafiz. Ten Sa'di/Gulistan narrative records were removed from the quote feed. Bengali fields are AI-assisted translations or renderings, not published/expert-certified translations. The corpus is incomplete and the 200+ target has not been met.
- Scripture quote expansion (2026-10-21): current normalized totals are Qur'an 14, Bhagavad Gita 17, Dhammapada 22, and Guru Granth Sahib 2. The 12 Qur'an additions use verbatim Arabic ayah lines from the bundled Tanzil text; their English/Bengali are Soul Space renderings, not QuranEnc translations. The 15 new Gita records contain Sanskrit excerpts with original Soul Space translations; verify Sanskrit text against a critical edition before release. The 20 new Dhammapada records use Pāli text from the bundled ETCBC source. No new Guru Granth Sahib verbatim quotes were added because the current approved source/translation status is not sufficient.
- Raw source texts are not automatically app-integrated. The app still needs a loader/translation path before these records appear in Soul Space UI.

