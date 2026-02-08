package Production.AuthService.config;

import Production.AuthService.entities.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/h2-console/**").permitAll()
                            .requestMatchers("/api/v3/user/**").permitAll()
                            .anyRequest().permitAll()
                    )
                    .headers(headers -> headers
                            .frameOptions(frame -> frame.disable())
                    );

            return http.build();
        }


/*

the class that fetch users from db


But Spring security use different User(Inbuild) then our entity claass User

    @Bean
    public UserDetailsService userDetailsServiceFromDB(){

    }

     */

}
