package Production.AuthService.config;

import Production.AuthService.SecurityUtils.CustomUserService;
import Production.AuthService.SecurityUtils.JwtAuthenticationFilter;
import Production.AuthService.exceptions.CustomAccessDeniedHandler;
import Production.AuthService.exceptions.CustomAuthenticationEntryPoint;
import Production.AuthService.oauth.CustomOAuth2UserService;
import Production.AuthService.oauth.OAuth2FailureHandler;
import Production.AuthService.oauth.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;
    @Autowired
    private OAuth2FailureHandler oAuth2FailureHandler;
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private JwtAuthenticationFilter jwtauthenticationFilter;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private CustomUserService customUserService;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS) //by defualt statefull  ...
                )
                .authorizeHttpRequests(auth -> auth

//                        .requestMatchers(PublicURLs.ALL_PUBLIC_URL).permitAll()
//                        .requestMatchers(HttpMethod.POST, "/api/v3/user/**","/api/v3/auth/refresh")
//                        .hasRole("DEVELOPER")
//                        .requestMatchers(HttpMethod.GET, "/api/v3/user/**")
//                        .hasAnyRole("ADMIN", "DEVELOPER")
                                .requestMatchers("/api/v1/auth/**").permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(u -> u
                                .userService(customOAuth2UserService)  // our custom loader
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
//
//                .logout(AbstractHttpConfigurer::disable)
//                .headers(headers ->
//                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
//                )
//                .exceptionHandling(ex ->
//                        ex.authenticationEntryPoint(customAuthenticationEntryPoint)
//                         .accessDeniedHandler(customAccessDeniedHandler)
//                )
                .authenticationProvider(authenticationProvider())
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

    //    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration authenticationConfiguration
//    ) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


/*

the class that fetch users from db


But Spring security use different User(Inbuild) then our entity claass User

    @Bean
    public UserDetailsService userDetailsServiceFromDB(){

    }

     */

}
