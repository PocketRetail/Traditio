package org.pocketretail.core.deliverylayer.database.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("page",schema = "tables")
data class Page(
    @Id
    @Column("page_id")
    var pageId: Int,
    @Column("page_name")
    var pageName: String,
)
