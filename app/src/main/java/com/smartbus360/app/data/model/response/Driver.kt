package com.smartbus360.app.data.model.response

import com.google.gson.annotations.SerializedName

data class Driver (

    @SerializedName("id"             ) var id            : Int?    = null,
    @SerializedName("driver_name"    ) var driverName    : String? = null,
    @SerializedName("licence_number" ) var licenceNumber : String? = null,
    @SerializedName("phone_number"   ) var phoneNumber   : String? = null,
    @SerializedName("created_at"     ) var createdAt     : String? = null

)
