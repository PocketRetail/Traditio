package org.pocketretail.core.deliverylayer.database.entity

import org.pocketretail.core.deliverylayer.database.constant.ParameterType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("client_request_parameter",schema = "tables")
data class ClientRequestParameter(
    @Id
    @Column("client_request_parameter_id")
    val clientRequestParameterId: Int?=null,
    @Column("client_request_id")
    var clientRequestId: Int,
    @Column("client_request_parameter_name")
    var clientRequestParameterName: String,
    @Column("client_request_parameter_type")
    var clientRequestParameterType: ParameterType,
    @Column("client_request_parameter_data_type")
    var clientRequestParameterDataType: String,
    @Column("client_request_parameter_data_type_name")
    var clientRequestParameterDataTypeName:String?,
    @Column("client_request_parameter_of_type_data_type")
    var clientRequestParameterOfTypeDataType: String? = null,
    @Column("client_request_parameter_of_type_data_type_name")
    var clientRequestParameterOfTypeDataTypeName: String? = null,
    @Column("parent_client_request_parameter_id")
    var parentClientRequestParameterId: Int? = null,
)