package com.infra.authorization.config;

import com.infra.authorization.persistence.entities.AuthProvider;
import com.infra.authorization.services.security.filter.JdbcAuthenticationFilter;
import com.infra.authorization.services.security.auth.RestAuthenticationEntryPoint;
import com.infra.authorization.services.security.filter.JwtAuthenticationFilter;
import com.infra.authorization.services.security.oauth2.CustomOAuth2UserService;
import com.infra.authorization.services.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.infra.authorization.services.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.infra.authorization.services.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {
    private static String CLIENT_PROPERTY_KEY = "spring.security.oauth2.client.registration.";

    @Value("${app.publicEndpoint}")
    private String baseUrl;

    private static final List<String> clients = List.of(AuthProvider.google.name().toLowerCase(), AuthProvider.facebook.name().toLowerCase());
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService jdbcUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OidcUserService customOIDCOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final Environment environment;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(handler -> handler.authenticationEntryPoint(new RestAuthenticationEntryPoint()))
                .authorizeHttpRequests(request -> request
                    .requestMatchers("/api/auth/public/**").permitAll()
//                    .requestMatchers("/api/auth/**").hasAnyAuthority(ERole.ROLE_USER.name())
                    .requestMatchers("/api/auth/user/me", "/test", "/api/auth/**", "/oauth2/**").permitAll()
                    .requestMatchers("/",
                            "/error",
                            "/favicon.ico",
                            "/**/*.png",
                            "/**/*.gif",
                            "/**/*.svg",
                            "/**/*.jpg",
                            "/**/*.html",
                            "/**/*.css",
                            "/**/*.js")
                    .permitAll()
                    .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> {
                    oauth.authorizationEndpoint(ep -> ep.baseUri("/oauth2/authorize").authorizationRequestRepository(cookieAuthorizationRequestRepository()))
                            .redirectionEndpoint(r -> r.baseUri("/oauth2/callback/*"))
                            .userInfoEndpoint(e -> e
                                    .oidcUserService(customOIDCOAuth2UserService)
                                    .userService(customOAuth2UserService))
                            .successHandler(oAuth2AuthenticationSuccessHandler)
                            .failureHandler(oAuth2AuthenticationFailureHandler);
                        }

                )

                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                    .addFilterBefore(customSavedRequestFilter(), UsernamePasswordAuthenticationFilter.class)
        ;
        return http.build();
    }

    public JdbcAuthenticationFilter customSavedRequestFilter() { return new JdbcAuthenticationFilter(); }
    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(jdbcUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = clients.stream()
                .map(c -> getRegistration(c))
                .filter(registration -> registration != null)
                .collect(Collectors.toList());

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration getRegistration(String client) {
        String clientId = environment.getProperty(CLIENT_PROPERTY_KEY + client + ".client-id");
        if (clientId == null) {
            return null;
        }

        String clientSecret = environment.getProperty(CLIENT_PROPERTY_KEY + client + ".client-secret");

        if (client.equals("google")) {
            return CommonOAuth2Provider.GOOGLE.getBuilder(client)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .redirectUri(baseUrl + "/oauth2/callback/{registrationId}")
                    .build();
        }

        if (client.equals("facebook")) {
            return CommonOAuth2Provider.FACEBOOK.getBuilder(client)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .redirectUri(baseUrl + "/oauth2/callback/{registrationId}")
                    .build();
        }
        return null;
    }
}
