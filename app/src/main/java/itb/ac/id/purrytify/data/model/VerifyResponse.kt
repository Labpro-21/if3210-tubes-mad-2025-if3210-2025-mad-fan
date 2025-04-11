package itb.ac.id.purrytify.data.model

data class VerifyResponse(
    val valid: Boolean,
    val user: User
)

data class User(
    val id: Int,
    val username: String
)