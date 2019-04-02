package webfreak.si.remotecast

import kotlinx.serialization.Serializable


@Serializable
data class CCModel(val name: String, val address: String)