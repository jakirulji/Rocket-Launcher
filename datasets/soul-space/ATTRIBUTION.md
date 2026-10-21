# Soul Space offline datasets — attribution and licensing

## Dhammapada Pāli source text

- Source repository: ETCBC, dhammapada — https://github.com/ETCBC/dhammapada
- Upstream file: sources/pali.txt
- Edition described upstream: Pāli text in the edition with Latin translation by V. Fausböll (1900).
- Upstream repository license: MIT License, copyright (c) 2019 Dirk Roorda. Upstream README credits prof. dr. Bee Scherer, prof. dr. Willem van Peursen, Yvonne Mataar, and dr. Dirk Roorda for corpus conversion/transcription work.
- Source text is preserved as-is; this file is not an English or Bengali translation.

### Required notice for this bundled file

MIT License

Copyright (c) 2019 Dirk Roorda

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.

## Qur’an

- Arabic source candidate: Tanzil Quran Text, https://tanzil.net/
- License: CC BY 3.0; verbatim redistribution is allowed, but the Arabic text must not be changed. Include Tanzil attribution and link to https://tanzil.net/updates/.
- Do not bundle a translation until that specific translation edition's redistribution terms are verified.

## Sufi wisdom — Rumi source texts

### Project Gutenberg eBook #45159
- Title: *The Persian Mystics: Jalálu'd-dín Rúmí*.
- Historical edition: F. Hadland Davis, 1907 (the eBook metadata lists a later release date).
- Source: https://www.gutenberg.org/ebooks/45159
- Bundled file: `sufi-wisdom/sources/rumi_persian_mystics_davis_1907.txt`
- The book's preface credits multiple translators for different selections. Preserve this book's acknowledgements and do not assume every poem uses the same English translator.

### Project Gutenberg eBook #61724
- Title: *The Mesnevi*, Book I, Rumi.
- Translator: Sir James W. Redhouse; original edition 1881.
- Source: https://www.gutenberg.org/ebooks/61724
- Bundled file: `sufi-wisdom/sources/rumi_mesnevi_redhouse_book1_1881.txt`

### Project Gutenberg eBook #57068
- Title: *The Festival of Spring, from the Díván of Jeláleddín*.
- Translator: William Hastie; historical edition 1903.
- Source: https://www.gutenberg.org/ebooks/57068
- Bundled file: `sufi-wisdom/sources/rumi_festival_of_spring_hastie_1903.txt`

All three files preserve the Project Gutenberg header and license text. Project Gutenberg describes its eBooks as public domain in the USA and advises readers outside the USA to check local law. These historical editions were selected after a preliminary review of contributor dates under the Bangladesh life-plus-60-year rule as understood for this project. Keep the notices intact and recheck jurisdiction-specific terms if the distribution model changes.

### Structured selections
- File: `sufi-wisdom/records.jsonl`
- Contains 100 titled selections from eBook #45159 with source line anchors and English source text.
- Bengali translation fields are null and explicitly marked `pending_ai_simple_translation`. Do not label them as verified Bengali translations until the on-device AI translation path is implemented and its output is reviewed.
- Status: `sufi-wisdom/SUFI_CORPUS_STATUS.md`.

## Other categories — source review still required

Bhagavad Gita, Guru Granth Sahib, philosophy & literature, and Soul Space original reflections still require their own source-by-source license review and verified records. Public availability alone is not permission to redistribute. Do not treat paraphrases as quotations or fabricate attributions.

This file is an attribution index, not a claim that all target counts have been met.