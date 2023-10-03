package org.pocketretail.core.deliverylayer.database.repo

import org.pocketretail.core.deliverylayer.database.entity.ClientRequest
import org.pocketretail.core.deliverylayer.database.entity.ClientRequestParameter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ClientRequestParameterRepository: JpaRepository<ClientRequestParameter, Int>{
    fun findAllByClientRequestIdIn(clientRequests: List<ClientRequest>)

    fun findAllByClientRequestIdAndParentClientRequestParameterIdIsNull(clientRequest: ClientRequest):List<ClientRequestParameter>

    fun findAllByParentClientRequestParameterId(parentClientRequestParameterId: ClientRequestParameter):List<ClientRequestParameter>

    fun deleteAllByParentClientRequestParameterId(parentClientRequestParameterId: ClientRequestParameter)
    fun deleteAllByClientRequestId(clientRequestId: ClientRequest)
    fun findAllByClientRequestId(clientRequest: ClientRequest):List<ClientRequestParameter>
    fun findClientRequestParameterByChildClientRequestParameterIdsContaining(childClientRequestParameterId: ClientRequestParameter):ClientRequestParameter?
}