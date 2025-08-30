package com.smartbus360.app.data.olaMaps

data class Prediction(
    val description: String,
    val distance_meters: Int,
    val geometry: Geometry,
    val layer: List<String>,
    val matched_substrings: List<MatchedSubstring>,
    val place_id: String,
    val reference: String,
    val structured_formatting: StructuredFormatting,
    val terms: List<Term>,
    val types: List<String>
)