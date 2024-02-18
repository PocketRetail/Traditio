package org.pocketretail.core.deliverylayer.rest.constant

enum class Platform(val value: String) {
    MOBILE("mobile"),
    WEB("web");

    companion object {
        fun getPlatformByName(requestPlatform: String): Platform =
            entries.find { it.value == requestPlatform }?:throw IllegalArgumentException("Platform not found")
    }
}
