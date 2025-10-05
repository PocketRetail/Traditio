package org.pocketretail.core.deliverylayer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.extern.slf4j.Slf4j;


@Configuration
public class UiSecurityConfig {

    @Bean
    @Order(3)
    public SecurityWebFilterChain configure(ServerHttpSecurity http) {
        http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/deliverylayer/ui/**"))
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/oauth2/**"))
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/login/**"))
                .oauth2Login(oauth2Login -> {})
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("/deliverylayer/ui/**").authenticated()
                        .pathMatchers("/logout", "/actuator/**", "/swagger-ui/**", "/api-docs/**", "/oauth2/**", "/login/**").permitAll())
                .logout(logout -> logout.logoutUrl("http://localdev.de:8080/realms/PocketRetailRealm/protocol/openid-connect/logout?redirect_uri=http://localhost/logout"));

        return http.build();
    }
}