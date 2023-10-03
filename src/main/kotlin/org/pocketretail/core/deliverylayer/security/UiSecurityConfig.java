package org.pocketretail.core.deliverylayer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.extern.slf4j.Slf4j;


@Configuration
@EnableWebSecurity
@Slf4j
public class UiSecurityConfig {

    @Bean
    @Order(3)
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {

        http.securityMatchers(requestMatcherConfigurer -> requestMatcherConfigurer.requestMatchers(
                    new AntPathRequestMatcher("/deliverylayer/ui/**"),
                    new AntPathRequestMatcher("/oauth2/**"),
                    new AntPathRequestMatcher("/login/**")))
            .oauth2Login(Customizer.withDefaults())
            .sessionManagement(
                    httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(
                            SessionCreationPolicy.ALWAYS))
            .headers(
                    httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.httpStrictTransportSecurity(
                            HeadersConfigurer.HstsConfig::disable))

            .authorizeHttpRequests(
                    authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry.requestMatchers(
                            new AntPathRequestMatcher("/deliverylayer/ui/**")).fullyAuthenticated())
            .authorizeHttpRequests(
                    authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry.requestMatchers(
                            new AntPathRequestMatcher("/logout"),
                            new AntPathRequestMatcher("/actuator/**"),
                            new AntPathRequestMatcher("/swagger-ui/**"),
                            new AntPathRequestMatcher("/api-docs/**"),
                            new AntPathRequestMatcher("/oauth2/**"),
                            new AntPathRequestMatcher("/login/**")).permitAll())
            .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer.logoutSuccessUrl(
                    "http://localhost:8080/realms/PocketRetailRealm/protocol/openid-connect/logout?redirect_uri=http://localhost/logout"));

        return http.build();
    }


}