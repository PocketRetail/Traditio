package org.pocketretail.core.deliverylayer.security;

import org.pocketretail.core.deliverylayer.security.filter.DeliveryLayerApiFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class ApiSecurityConfig {

    @Bean
    @Order(2)
    public SecurityWebFilterChain apiSecurityFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/deliverylayer/api/v1/rendering/**"))
                .addFilterAt(new DeliveryLayerApiFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("/deliverylayer/api/v1/rendering/**").authenticated());

        return http.build();
    }
}