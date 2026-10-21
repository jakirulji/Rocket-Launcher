# Bengali scripture translations — integration status
Reviewed: 2026-10-21
Repository branch: soul-space

This is a provenance/status register, not a claim that the listed full texts are already in the app dataset.

## Already integrated
- Qur'an Bengali: Rowwad Translation Center / QuranEnc, version 1.1.2.
  File: `datasets/soul-space/quran/bengali_rowwad_quranenc_v1.1.2.json`
  Re-publication is permitted under QuranEnc's stated conditions. Preserve the source JSON verbatim, attribution, version and notes; keep it updated and avoid inappropriate advertising.
  Terms: https://quranenc.com/en/home/api
  Source: https://quranenc.com/en/browse/bengali_rwwad/

## Strong historical candidate, but full machine-readable text not yet imported
- Bhagavad Gita in Bengali, translated by Gauri Shankar Bhattacharya (1871).
  Wikisource catalogues the edition and its scan (197 pages), but the scan index reports 0% proofread and 0% validated. The text therefore needs a complete OCR/transcription pass and verification against the scan before it can be safely imported as verse-level data. The original scan is listed here:
  https://bn.wikisource.org/wiki/নির্ঘণ্ট:ভগবদ্গীতা_-_গৌরীশঙ্কর_তর্কবাগীশ.pdf
  Wikidata metadata and scan link: https://www.wikidata.org/wiki/Q118640742
  Do not label it as an integrated dataset until the transcription is complete and checked.

## Not yet accepted for redistribution
- Dhammapada Bengali, translated by Charuchandra Basu (1905/1916 editions).
  Scans are available through Bengali Wikisource / Wikimedia Commons, but the visible public-domain determination is India-specific and the translator's death date is not established in the source metadata. Bangladesh-wide redistribution status is therefore not confirmed. The scans also require full OCR and verse-by-verse checking.
  1905 scan: https://bn.wikisource.org/wiki/নির্ঘণ্ট:ধম্মপদ_-_চারুচন্দ্র_বসু_(১৯০৫).pdf
  1916 scan: https://commons.wikimedia.org/wiki/File:ধম্মপদ_-_চারুচন্দ্র_বসু_(১৯১৬).pdf

- Guru Granth Sahib Bengali translation (Kulbir Singh Thind, MD; sentence-by-sentence PDF).
  The source page explicitly says use in commercial purposes or internet projects requires express written approval. This does not meet the project's no-contact/no-extra-permission criterion, so do not redistribute it in the app.
  Source and terms: https://punjabonline.com/gujrati%2B/SGGS-Bengali-SBS-Uni.pdf

## Release rule
Only complete, checked, and legally eligible text files count as integrated. Source links and OCR status notes are not substitutes for the offline text. Never create missing translation verses or silently modernize historical wording. Keep source, translator, edition, year, license basis, and verse/page references alongside each imported text.
