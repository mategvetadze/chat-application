package com.example.demo;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{
    private final JwtService jwtService;
    public JwtAuthFilter(JwtService jwtService){
        this.jwtService=jwtService;
    }
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

    String header = request.getHeader("Authorization");

    if (header != null && header.startsWith("Bearer ")) {
        String token = header.substring(7);
        try {
            String username = jwtService.extractUsername(token);
            var auth = new UsernamePasswordAuthenticationToken(
                    username, null, java.util.List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ignored) {
            // bad/expired token → stay anonymous
          }
        }

        filterChain.doFilter(request, response);
    }

}