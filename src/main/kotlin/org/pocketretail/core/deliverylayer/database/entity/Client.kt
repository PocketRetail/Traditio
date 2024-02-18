package org.pocketretail.core.deliverylayer.database.entity

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
@Table("client", schema = "tables")
data class Client(
    @Id
    @Column("client_id")
    var clientId: String,
    @Column("client_description")
    var clientDescription: String? = null,
    @Column("client_uri")
    var clientURI: String,
    @Column("is_active")
    var active: Boolean,
    @Column("created_at")
    var createdAt: LocalDateTime? = null,
) : Persistable<String> {
    override fun getId(): String = clientId
    override fun isNew(): Boolean = createdAt == null
}
