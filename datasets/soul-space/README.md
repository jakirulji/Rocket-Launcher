# Soul Space dataset workspace

This directory is the source-controlled home for the seven independent offline Soul Space datasets. Data should be stored separately from UI code and include a stable reference, language, attribution, licence, and record type for each entry.

## Dataset targets

| Dataset | Target | Current bundled status |
|---|---:|---|
| Qur’an | 300–500 | Arabic and English/Bengali source files bundled; app-ready entries are only a small starter subset |
| Sufi wisdom | 200–500 | 170 structured records: Rumi 120, Kabir 20, Sa'di 20, Hafiz 10; Bengali renderings are AI-assisted and need review |
| Philosophy & literature | 200–500 | Only a small starter paraphrase exists; source-verified corpus expansion remains |
| Bhagavad Gita | 200 | Historic K.T. Telang English text bundled; structured app-ready records are only a small starter subset |
| Dhammapada | 200 | Pāli and Latin source files bundled; verse-level normalization and accepted English/Bengali records remain incomplete |
| Guru Granth Sahib | 200 | Bengali translation excluded pending express approval; current entries are paraphrases, not verbatim Gurbani |
| Original reflections | 200 | Small original set exists; expansion and quality review remain |

## Data integrity rules

- Keep source text unchanged when the source licence prohibits modification; put Bengali/English translations in separate fields and label them as original translations or paraphrases.
- Every record must have a stable book/verse/section reference and a source URL.
- Every bundled source must be recorded in ATTRIBUTION.md with the exact licence notice and upstream contributors.
- Do not count one passage split into multiple records as multiple independent quotations.
- Never mark the dataset complete until automated record counts and reference checks pass.

## Current source status

- `dhammapada/pali.txt` is the Pāli source text from ETCBC/dhammapada under its upstream MIT licence. It is not yet the final app-ready JSONL dataset; verse-level normalization and translation fields remain pending.
- `sufi-wisdom/sources/` now contains three historical English Rumi source texts with Project Gutenberg headers and license notices preserved.
- `sufi-wisdom/records.jsonl` contains 170 structured records across Rumi, Kabir, Sa'di, and Hafiz. Bengali fields are AI-assisted plain translations or summaries, not published/expert-certified translations. The corpus is incomplete and the 200+ target has not been met.
- Raw source texts are not automatically app-integrated. The app still needs a loader/translation path before these records appear in Soul Space UI.

