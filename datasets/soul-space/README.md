# Soul Space dataset workspace

This directory is the source-controlled home for the seven independent offline Soul Space datasets. Data should be stored separately from UI code and include a stable reference, language, attribution, licence, and record type for each entry.

## Dataset targets

| Dataset | Target | Current bundled status |
|---|---:|---|
| Qur’an | 300–500 | 66 selected Arabic verse records; full Arabic/Bengali/English source files also bundled |
| Sufi wisdom | 200–500 | 160 quote records: Rumi 120, Kabir 20, Sa'di Bustān 10, Hafiz 10; Gulistan story summaries excluded; Bengali renderings are AI-assisted and need review |
| Philosophy & literature | 200–500 | Only a small starter paraphrase exists; source-verified corpus expansion remains |
| Bhagavad Gita | 200 | 67 selected Sanskrit verse records; English/Bengali are Soul Space renderings and require review |
| Dhammapada | 200 | 76 selected Pāli verse records; English/Bengali are Soul Space renderings and require review |
| Guru Granth Sahib | 200 | Bengali translation excluded pending express approval; current entries are paraphrases, not verbatim Gurbani |
| Original reflections | 200 | Small original set exists; expansion and quality review remain |

## Data integrity rules

- Keep source text unchanged when the source licence prohibits modification; put Bengali/English translations in separate fields and label them as original translations or paraphrases.
- Every record must have a stable book/verse/section reference and a source URL.
- Every bundled source must be recorded in ATTRIBUTION.md with the exact licence notice and upstream contributors.
- Do not count one passage split into multiple records as multiple independent quotations.
- Never mark the dataset complete until automated record counts and reference checks pass.

## Current source status

- `dhammapada/pali.txt` is the Pāli source text from ETCBC/dhammapada under its upstream MIT licence. `dhammapada/entries.json` now contains 76 selected verse records; English and Bengali fields are Soul Space renderings, not published translations.
- `sufi-wisdom/sources/` now contains three historical English Rumi source texts with Project Gutenberg headers and license notices preserved.
- `sufi-wisdom/records.jsonl` contains 160 quote records across Rumi, Kabir, Sa'di Bustān, and Hafiz. Ten Sa'di/Gulistan narrative records were removed from the quote feed. Bengali fields are AI-assisted translations or renderings, not published/expert-certified translations. The corpus is incomplete and the 200+ target has not been met.
- Scripture quote expansion (2026-10-21): current normalized totals are Qur'an 66, Bhagavad Gita 67, Dhammapada 76, and Guru Granth Sahib 2. This pass added 52 new Qur'an records, 50 Gita records, and 54 Dhammapada records, skipping existing references. Qur'an Arabic excerpts were matched to bundled Tanzil text; Gita Sanskrit verses came from `gita/gita` under its Unlicense/public-domain dedication; Dhammapada Pāli text came from the bundled ETCBC source. English/Bengali renderings are Soul Space-authored, not expert-certified. No new Guru Granth Sahib verbatim quotes were added because the approved source/translation status is not sufficient.
- Raw source texts are not automatically app-integrated. The app still needs a loader/translation path before these records appear in Soul Space UI.


- Scripture expansion pass 3 (2026-10-21): added 50 unique Bhagavad Gita verse selections (total 117) and 50 unique Dhammapada selections (total 126). Qur'an remains at 66: I did not append another batch because the bundled flat Arabic text has verse-boundary ambiguity that must be resolved before assigning new surah:ayah references. Do not infer that the whole 150-record target is complete.
