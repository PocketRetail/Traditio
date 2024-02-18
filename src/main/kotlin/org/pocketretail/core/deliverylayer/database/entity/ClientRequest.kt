package org.pocketretail.core.deliverylayer.database.entity

import org.pocketretail.core.deliverylayer.database.constant.ClientRequestType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("client_request", schema = "tables")
data class ClientRequest(
    @Id
    @Column("client_request_id")
    val clientRequestId: Int?=null,
    @Column("client_request_type")
    var clientRequestType: ClientRequestType,
    @Column("client_request_uri")
    var clientRequestURI: String,
    @Column("client_id")
    var clientId: String,
    @Column("client_request_name")
    var clientRequestName: String?
)
