package org.pocketretail.core.deliverylayer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class BasicSecurityConfig {


    @Bean
    @Order(1)
    public SecurityFilterChain basicSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .securityMatcher(new AntPathRequestMatcher("/deliverylayer/api/v1/registration/**"))
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(
                            new AntPathRequestMatcher("/deliverylayer/api/v1/registration/**"))
                    .permitAll());

        return http.build();
    }

}
