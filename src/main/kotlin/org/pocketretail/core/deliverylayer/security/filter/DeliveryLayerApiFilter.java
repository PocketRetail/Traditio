package org.pocketretail.core.deliverylayer.security.filter;

import org.pocketretail.lib.JWTType;
import org.pocketretail.lib.JWTUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DeliveryLayerApiFilter extends OncePerRequestFilter {

    JWTUtils jwtUtils = new JWTUtils();
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Cookie accessCookie = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("access")).findFirst().orElseThrow();
        String token = accessCookie.getValue();
        if (jwtUtils.isJWTExpired(token, JWTType.ACCESSANY)) {
            throw new ServletException("JWT is expired");
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(jwtUtils.getSubject(token, JWTType.ACCESSANY), null, new ArrayList<>())
                                 );
        SecurityContextHolder.setContext(context);
        filterChain.doFilter(request, response);
    }
}
