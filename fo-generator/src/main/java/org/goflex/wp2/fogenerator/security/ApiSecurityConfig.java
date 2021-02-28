package org.goflex.wp2.fogenerator.security;


/**
 * Created by bijay on 7/6/17.
 */

import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

//@Configuration
//ableWebSecurity
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {
   /* @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .antMatchers("/api/user*//**").permitAll()
     .antMatchers("/api").permitAll()
     .antMatchers("/").denyAll()
     .antMatchers(HttpMethod.POST, "/api/auth").permitAll()
     .anyRequest().authenticated()
     .and()
     // We filter the api/login requests
     .addFilterBefore(new JWTAuthFilter("/api/auth", authenticationManager()),
     UsernamePasswordAuthenticationFilter.class)
     // And filter other requests to check the presence of JWT in header
     .addFilterBefore(new JWTAuthenticationFilter(),
     UsernamePasswordAuthenticationFilter.class);
     }           //And

     @Override protected void configure(AuthenticationManagerBuilder auth) throws Exception {
     // Create a default account
     auth.inMemoryAuthentication()
     .withUser("admin")
     .password("password")
     .roles("ADMIN");

     }*/
}