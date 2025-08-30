package com.smartbus360.app.data.model.response

data class GetUserResponse(
    val routeStoppages: List<RouteStoppageX> = emptyList(),
    val success: Boolean = false,
    val user: UserX = UserX() // Assuming UserX has default values
)