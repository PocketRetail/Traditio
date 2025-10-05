package org.pocketretail.core.deliverylayer.rest.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.server.ServerWebExchange

@Configuration
class WebConfig {

    @Bean
    fun corsFilter(): CorsWebFilter {
        val corsConfig = CorsConfiguration()
        corsConfig.addAllowedOrigin("https://localdev.de:3000")
        corsConfig.addAllowedHeader("*")
        corsConfig.addAllowedMethod(HttpMethod.GET)
        corsConfig.addAllowedMethod(HttpMethod.POST)
        corsConfig.allowCredentials = true

        return CorsWebFilter { exchange: ServerWebExchange? -> corsConfig }
    }
}