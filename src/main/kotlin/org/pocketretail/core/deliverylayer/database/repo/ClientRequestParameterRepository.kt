package org.pocketretail.core.deliverylayer.database.repo

import org.pocketretail.core.deliverylayer.database.entity.ClientRequestParameter
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface ClientRequestParameterRepository : R2dbcRepository<ClientRequestParameter, Int> {
    fun findAllByClientRequestIdAndParentClientRequestParameterIdIsNull(clientRequestId: Int): Flux<ClientRequestParameter>
    fun findAllByParentClientRequestParameterId(parentClientRequestParameterId: Int): Flux<ClientRequestParameter>
    fun findAllByClientRequestId(clientRequestId: Int): Flux<ClientRequestParameter>
}