package org.goflex.wp2.fogenerator.security;


import org.goflex.wp2.foa.implementation.FOAUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Created by bijay on 12/11/17.
 */
public class ApiMultiHttpSecurity {


    @Configuration
    @Order(1)
    public static class ApiSecurityAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private FOAUserDetailsService foaUserDetailsService;


        @Autowired
        private PasswordEncoder passwordEncoder;


        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider authProvider
                    = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(foaUserDetailsService);
            authProvider.setPasswordEncoder(passwordEncoder);
            return authProvider;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.csrf().disable()
                    .antMatcher("/api/**").authorizeRequests()
                    .antMatchers(HttpMethod.POST, "/api/auth").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    // We filter the api/login requests
                    .addFilterBefore(new JWTAuthFilter("/api/auth", authenticationManager()),
                            UsernamePasswordAuthenticationFilter.class)
                    // And filter other requests to check the presence of JWT in header
                    .addFilterBefore(new JWTAuthenticationFilter(),
                            UsernamePasswordAuthenticationFilter.class);

        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth)
                throws Exception {
            auth.userDetailsService(foaUserDetailsService);
        }

    }

    @Configuration
    @Order(2)
    public static class WebSecurityAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private FOAUserDetailsService foaUserDetailsService;

        @Autowired
        private PasswordEncoder passwordEncoder;


        //@Autowired
        //private AuthenticationEntryPoint authEntryPoint;

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider authProvider
                    = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(foaUserDetailsService);
            authProvider.setPasswordEncoder(passwordEncoder);
            return authProvider;
        }


        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable()
                    .antMatcher("/foa/**")
                    .authorizeRequests()
                    .anyRequest().authenticated().and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                    .and()
                    //.formLogin();
                    .httpBasic();
            //.authenticationEntryPoint(authEntryPoint);
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth)
                throws Exception {
            auth.userDetailsService(foaUserDetailsService);
        }

        @Bean
        public FilterRegistrationBean corsFilter() {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
            config.addAllowedOrigin("*");
            config.addAllowedHeader("*");
            config.addAllowedMethod("*");
            source.registerCorsConfiguration("/**", config);
            FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
            bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return bean;
        }

        /*@Bean
        CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigin("*");
            configuration.setAllowedMethods(Arrays.asList("GET","POST"));
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("*//**", configuration);
         return source;
         }
         */

    }
}
