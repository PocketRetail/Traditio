package org.pocketretail.core.deliverylayer.rest.constant

enum class Application(val value: String) {
    MANAGEMENT("management"),
    SHOP("shop");

    companion object {
        fun getApplicationByName(requestApplication: String): Application =
            entries.find { it.value == requestApplication }?:throw IllegalArgumentException("Application not found")
    }
}
