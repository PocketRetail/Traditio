package org.pocketretail.core.deliverylayer

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity

@SpringBootApplication
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@OpenAPIDefinition(info = Info(title = "Delivery Layer", version = "0.0.1", description = "Delivery Layer"))
class DeliveryLayerApplication

fun main(args: Array<String>) {
    runApplication<DeliveryLayerApplication>(*args)
}
