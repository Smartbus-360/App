package com.smartbus360.app.data.model.response

import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("CPU model"             )  val cpu: String,
    @SerializedName("Hard disk size"             )  val hardDisk: String,
    val price: Double,
    val year: Int
)