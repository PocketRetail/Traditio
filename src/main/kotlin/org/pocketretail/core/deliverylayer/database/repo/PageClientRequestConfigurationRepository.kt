package org.pocketretail.core.deliverylayer.database.repo

import org.pocketretail.core.deliverylayer.database.entity.PageClientRequestConfiguration
import org.springframework.data.jpa.repository.JpaRepository

interface PageClientRequestConfigurationRepository: JpaRepository<PageClientRequestConfiguration, Int> {
}