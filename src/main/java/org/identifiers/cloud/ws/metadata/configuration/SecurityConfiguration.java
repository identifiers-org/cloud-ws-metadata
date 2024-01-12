package org.identifiers.cloud.ws.metadata.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    @Profile("authenabled")
    public SecurityFilterChain filterChain(HttpSecurity http,
                                  @Value("${org.identifiers.cloud.ws.metadata.requiredrole}")
                                  String requiredRole) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/getMetadataForUrl*").permitAll()
                .requestMatchers("/actuator").hasAuthority(requiredRole)
                .requestMatchers("/actuator/loggers/**").hasAuthority(requiredRole)
                .requestMatchers(HttpMethod.GET, "/actuator/health/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/**").permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .anonymous(AbstractHttpConfigurer::disable)
            .oauth2ResourceServer(oauth -> oauth.jwt(withDefaults()));

        return http.build();
    }

    @Bean
    @Profile("authenabled")
    @SuppressWarnings("unchecked")
    public JwtAuthenticationConverter customJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter((Jwt source) -> {
            if (!source.hasClaim("realm_access")) {
                log.debug("Invalid Jwt token");
                return Collections.emptyList();
            }
            Object roles = source.getClaimAsMap("realm_access").getOrDefault("roles", null);
            if (roles instanceof Collection<?>) {
                Collection<String> rolesList = (Collection<String>) roles;
                return rolesList.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            } else {
                log.debug("Invalid Jwt token");
                return Collections.emptyList();
            }
        });
        return converter;
    }

    @Bean
    @Profile("!authenabled")
    public SecurityFilterChain filterChainDev(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
