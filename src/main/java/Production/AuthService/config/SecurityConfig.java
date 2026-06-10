package Production.AuthService.config;

import Production.AuthService.Oidc.CustomOidcUserService;
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
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
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

    @Autowired(required = false)
    private org.springframework.security.ldap.authentication.LdapAuthenticationProvider ldapAuthenticationProvider;

    private CustomOidcUserService customOidcUserService;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS) //by defualt statefull  ...
                )
                .httpBasic(AbstractHttpConfigurer::disable) // disable default http basic auth
                .logout(AbstractHttpConfigurer::disable)// disable default logout handling
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
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
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        jwtauthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(customAuthenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler)
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(u -> u
                                .oidcUserService(customOidcUserService)
                                .userService(customOAuth2UserService)  // our custom loader
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
        ;

        // register optional LDAP provider if present
        if (ldapAuthenticationProvider != null) {
            http.authenticationProvider(ldapAuthenticationProvider);
        }

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    //    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration authenticationConfiguration
//    ) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
//            throws Exception {
//        return config.getAuthenticationManager();
//    }

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
/**
 *
 * 1.first request comes any[login or R1]  ---checking it if it have Authorization Header present as beare token or not
 * 2. in Login its not Present -[ username and password] -- then spring security chain pass to usernamepassword filter and then that filter call authentication manager and then that manager call userdetailservice to load user from db and then match password
 * 3. after Validate --> here we create jwt token and send to client
 * <p>
 * 4. in other R1 --in which Authorization header is present with Bearer token -- then custom jwt filter passed and extract it process  request and validate --NOT correct--pass to next chain
 * 5. YES  it validated ... now extract info from token ie email, claims, and call customerUserDetailsSerice [db call on validating  email] --find [username, password]
 * <p>
 * 6.. similiay in login we again having password and username --UsernamepasswordFilter calles  then  init call authentication manager and then that manager call userdetailservice to load user from db and then match password
 * <p>
 * 7. set authentication in security context and then pass to next filter
 *
 */