package org.pocketretail.core.deliverylayer.database.repo

import org.pocketretail.core.deliverylayer.database.entity.Page
import org.springframework.data.jpa.repository.JpaRepository

interface PageRepository: JpaRepository<Page, Int> {
}