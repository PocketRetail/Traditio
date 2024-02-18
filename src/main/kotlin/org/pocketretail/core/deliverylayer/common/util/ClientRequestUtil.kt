package org.pocketretail.core.deliverylayer.common.util

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class ClientRequestUtil {

    companion object {
        @Throws(IOException::class)
        fun readGetSchemasGraphQLFile(): String {
            val uri =
                javaClass.getClassLoader().getResource("graphql/get_schemas.graphql")!!.toURI()
            return String(Files.readAllBytes(Path.of(uri)))
        }
    }
}