package com.smartbus360.app.data.model.response

import com.google.gson.annotations.SerializedName

data class ReplacedDriver(

    @SerializedName("id"                       ) var id                       : Int?    = null,
    @SerializedName("name"                     ) var name                     : String? = null,
    @SerializedName("email"                    ) var email                    : String? = null,
    @SerializedName("phone"                    ) var phone                    : String? = null,
    @SerializedName("profilePicture"           ) var profilePicture           : String? = null,
    @SerializedName("replacementStartTime"     ) var replacementStartTime     : String? = null,
    @SerializedName("replacementDurationHours" ) var replacementDurationHours : String? = null


)
