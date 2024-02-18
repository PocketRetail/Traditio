package org.pocketretail.core.deliverylayer.database.config

import io.r2dbc.spi.ConnectionFactory
import org.pocketretail.core.deliverylayer.database.converter.ClientRequestTypeReadConverter
import org.pocketretail.core.deliverylayer.database.converter.ClientRequestTypeWriteConverter
import org.pocketretail.core.deliverylayer.database.converter.ParameterTypeReadConverter
import org.pocketretail.core.deliverylayer.database.converter.ParameterTypeWriteConverter
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories
class DatabaseConfiguration(
    private val connectionFactory: ConnectionFactory
) : AbstractR2dbcConfiguration() {

    override fun connectionFactory(): ConnectionFactory {
        return connectionFactory
    }

    override fun getCustomConverters(): MutableList<Any> {
        return mutableListOf(
            ClientRequestTypeWriteConverter(),
            ClientRequestTypeReadConverter(),
            ParameterTypeWriteConverter(),
            ParameterTypeReadConverter()
        )
    }
}