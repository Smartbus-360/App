package com.smartbus360.app.data.model.response

data class Result(
    val address_components: List<AddressComponent>,
    val formatted_address: String,
    val geometry: Geometry,
    val layer: List<String>,
    val name: String,
    val place_id: String,
    val plus_code: PlusCode,
    val types: List<Any?>
)