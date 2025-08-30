package com.smartbus360.app.data.model.response

import com.smartbus360.app.data.model.request.Data

data class TestRes(
    val createdAt: String,
    val data: Data,
    val id: String,
    val name: String
)