package org.pocketretail.core.deliverylayer.security.filter;

import org.pocketretail.lib.JWTType;
import org.pocketretail.lib.JWTUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import java.util.ArrayList;

import reactor.core.publisher.Mono;

public class DeliveryLayerApiFilter implements WebFilter {

    JWTUtils jwtUtils = new JWTUtils();


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        return Mono.justOrEmpty(request.getCookies().getFirst("access"))
                   .switchIfEmpty(Mono.error(new RuntimeException("Access cookie not found")))
                   .map(HttpCookie::getValue)
                   .filter(token -> !jwtUtils.isJWTExpired(token, JWTType.ACCESSANY))
                   .switchIfEmpty(Mono.error(new RuntimeException("JWT is expired")))
                   .flatMap(token -> {
                       SecurityContext context = new SecurityContextImpl();
                       context.setAuthentication(
                               new UsernamePasswordAuthenticationToken(
                                       jwtUtils.getSubject(token, JWTType.ACCESSANY), null,
                                       new ArrayList<>())
                                                );
                       return chain.filter(exchange).contextWrite(
                               ReactiveSecurityContextHolder.withSecurityContext(
                                       Mono.just(context)));
                   });
    }
}
