package com.smartbus360.app.data.olaMaps

data class StructuredFormatting(
    val main_text: String,
    val main_text_matched_substrings: List<MainTextMatchedSubstring>,
    val secondary_text: String,
    val secondary_text_matched_substrings: List<SecondaryTextMatchedSubstring>
)