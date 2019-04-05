package net.brutus5000.deltaforge.server.config.security;


import net.brutus5000.deltaforge.server.config.DeltaforgeServerProperties;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

import java.util.Objects;

/**
 * This filter extracts API keys from the request header and sets it into the principal object for later use.
 * The further handling is done in {@see WebSecurityConfig}.
 */
public class ApiKeyAuthFilter extends RequestHeaderAuthenticationFilter {

    private final DeltaforgeServerProperties properties;

    public ApiKeyAuthFilter(DeltaforgeServerProperties properties) {
        this.setPrincipalRequestHeader(properties.getSecurity().getAuthTokenHeaderName());
        this.setExceptionIfHeaderMissing(false);

        this.properties = properties;

        setAuthenticationManager(authentication -> {
            String providedToken = (String) authentication.getPrincipal();

            if (Objects.equals(providedToken, properties.getSecurity().getAuthToken())) {
                authentication.setAuthenticated(true);
            }

            return authentication;
        });
    }
}