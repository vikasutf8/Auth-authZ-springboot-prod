package Production.AuthService.config;

import Production.AuthService.SecurityUtils.JwtAuthenticationFilter;
import Production.AuthService.SecurityUtils.JwtService;
import Production.AuthService.entities.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtauthenticationFilter;


    //    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(Customizer.withDefaults())
//                .sessionManagement(sm ->
//                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/h2-console/**").permitAll()
//                        .requestMatchers("/api/v3/auth/login").permitAll()
//                        .requestMatchers("/api/v3/user/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint((request, response, authException) -> {
//
//                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//
//                            String message = "Unauthorized access: " + authException.getMessage();
//
//                            Map<String, Object> errorMap = Map.of(
//                                    "status", 401,
//                                    "error", "UNAUTHORIZED",
//                                    "message", message,
//                                    "path", request.getRequestURI(),
//                                    "timestamp", Instant.now().toString()
//                            );
//
//                            ObjectMapper mapper = new ObjectMapper();
//                            response.getWriter().write(mapper.writeValueAsString(errorMap));
//                        })
//                )
//                .httpBasic(AbstractHttpConfigurer::disable) // JWT only
//                .addFilterBefore(jwtauthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/h2-console/**",
                                "/api/v3/auth/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {

                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            Map<String, Object> errorMap = Map.of(
                                    "status", 401,
                                    "error", "UNAUTHORIZED",
                                    "message", "Unauthorized access",
                                    "path", request.getRequestURI(),
                                    "timestamp", Instant.now().toString()
                            );

                            new ObjectMapper().writeValue(response.getWriter(), errorMap);
                        })
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(
                        jwtauthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


/*

the class that fetch users from db


But Spring security use different User(Inbuild) then our entity claass User

    @Bean
    public UserDetailsService userDetailsServiceFromDB(){

    }

     */

}
