package com.smartbus360.app.data.model.response

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("id"            ) var id            : Int?    = null,
    val busId: Int,
    @SerializedName("message"       ) var message       : String? = null,
    @SerializedName("instituteType" ) var instituteType : String? = null,
    @SerializedName("status"        ) var status        : String? = null,
    @SerializedName("expiryDate"    ) var expiryDate    : String? = null,
    @SerializedName("createdAt"     ) var createdAt     : String? = null,
    @SerializedName("updatedAt"     ) var updatedAt     : String? = null,
    @SerializedName("isMandatory"   ) var isMandatory   : Int?    = null
)