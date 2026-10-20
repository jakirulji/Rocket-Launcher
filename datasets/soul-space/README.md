# Soul Space dataset workspace

This directory is the source-controlled home for the seven independent offline Soul Space datasets. Data should be stored separately from UI code and include a stable reference, language, attribution, licence, and record type for each entry.

## Dataset targets

| Dataset | Target | Current bundled status |
|---|---:|---|
| Qur’an | 300–500 | Source/licence identified for Arabic text; not yet imported |
| Sufi wisdom | 200–500 | Source verification required; no fabricated attributions |
| Philosophy & literature | 200–500 | Source-by-source public-domain/licence review required |
| Bhagavad Gita | 200 | Open text candidate identified; edition/licence validation required |
| Dhammapada | 200 | Pāli source text bundled; verse-level normalisation and Bengali/English fields pending |
| Guru Granth Sahib | 200 | Source/licence review required, especially translations |
| Original reflections | 200 | To be authored specifically for Soul Space and labelled original |

## Data integrity rules

- Keep source text unchanged when the source licence prohibits modification; put Bengali/English translations in separate fields and label them as original translations or paraphrases.
- Every record must have a stable book/verse/section reference and a source URL.
- Every bundled source must be recorded in ATTRIBUTION.md with the exact licence notice and upstream contributors.
- Do not count one passage split into multiple records as multiple independent quotations.
- Never mark the dataset complete until automated record counts and reference checks pass.

## Current source

dhammapada/pali.txt is the Pāli source text from ETCBC/dhammapada under its upstream MIT licence. It is not yet the final app-ready JSONL dataset; conversion into verse-level records and authored Bengali/English renderings is a separate data-preparation step.