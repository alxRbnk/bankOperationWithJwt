package org.rubnikovich.bankoperation.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.rubnikovich.bankoperation.security.JwtUtil;
import org.rubnikovich.bankoperation.service.DetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final DetailsService usersDetailsService;

    public JwtFilter(JwtUtil jwtUtil, DetailsService usersDetailsService) {
        this.jwtUtil = jwtUtil;
        this.usersDetailsService = usersDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws IOException, ServletException {
        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            if (jwt.isBlank()) {
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpServletResponse.getWriter().write("Invalid JWT Token in Bearer Header");
                return;
            } else {
                try {
                    String username = jwtUtil.validateTokenAndGetClaim(jwt);
                    UserDetails userDetails = usersDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails,
                                    userDetails.getPassword(),
                                    userDetails.getAuthorities());
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch (JWTVerificationException exc) {
                    httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    httpServletResponse.getWriter().write("Invalid JWT Token");
                    return;
                }
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
