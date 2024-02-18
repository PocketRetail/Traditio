package org.pocketretail.core.deliverylayer.database.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("page_request_parameter_configuration", schema = "tables")
data class PageRequestParameterConfiguration(
    @Id
    @Column("page_client_request_configuration_id")
    val pageClientRequestConfigurationId: Int,
    @Column("client_request_parameter_id")
    val clientRequestParameterId: Int
)
