package com.bank.bffmobile.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de autenticacion por API Key para el canal Movil.
 *
 * Cada peticion debe incluir el header "X-API-KEY" con el valor configurado
 * en application.properties (bff.api-key). Si coincide, se autentica la
 * peticion con el rol ROLE_MOBILE.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${bff.api-key}")
    private String apiKeyValida;

    private static final String HEADER_NAME = "X-API-KEY";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String apiKeyRecibida = request.getHeader(HEADER_NAME);

        if (apiKeyValida.equals(apiKeyRecibida)) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "canal-mobile",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_MOBILE"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}