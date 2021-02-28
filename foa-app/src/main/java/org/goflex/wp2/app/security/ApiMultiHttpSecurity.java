package org.goflex.wp2.app.security;


import org.goflex.wp2.foa.implementation.FOAUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
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
        @EnableGlobalMethodSecurity(prePostEnabled = true)
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

                // Disable CSRF (cross site request forgery)
                http.csrf().disable();

                http.authorizeRequests()
                        .antMatchers("/api/v1.0/prosumer/register").permitAll()//
                        .antMatchers("/api/v1.0/prosumer/forgotPassword").permitAll()//
                        .antMatchers("/api/v1.0/server/status").permitAll()//
                        .antMatchers("/api/v1.0/server/getModelStateMap/{orgName}").permitAll()//
                        .antMatchers("/api/v1.0//organization/flexOffers/{orgName}").permitAll()//
                        .antMatchers("/api/v1.0/prosumer/login").permitAll()//
                        .antMatchers("/api/v1.0/prosumer/userNameExists").permitAll()//
                        .antMatchers("/api/v1.0/prosumer/tpLinkAccountExists").permitAll()//
                        .anyRequest().authenticated()
                        .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .and()
                        // We filter the api/login requests
                        .addFilterBefore(new JWTAuthFilter("/api/v1.0/prosumer/login", authenticationManager()),
                                UsernamePasswordAuthenticationFilter.class)
                        // And filter other requests to check the presence of JWT in header
                        .addFilterBefore(new JWTAuthenticationFilter(),
                                UsernamePasswordAuthenticationFilter.class);

                // If a user try to access a resource without having enough permissions
                http.exceptionHandling().accessDeniedPage("/login");

                http.httpBasic();

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
                //config.addExposedHeader("Authorization");
                source.registerCorsConfiguration("/**", config);
                FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
                bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
                return bean;
            }

        }

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @Order(2)
    public static class WebSecurityAdapter extends WebSecurityConfigurerAdapter {


//        @Autowired
//        private FOAUserDetailsService foaUserDetailsService;
//
//        @Autowired
//        @Lazy
//        private PasswordEncoder passwordEncoder;
//
//
//        @Bean
//        public DaoAuthenticationProvider authenticationProvider() {
//            DaoAuthenticationProvider authProvider
//                    = new DaoAuthenticationProvider();
//            authProvider.setUserDetailsService(foaUserDetailsService);
//            authProvider.setPasswordEncoder(passwordEncoder);
//            return authProvider;
//        }
//
//
//        @Override
//        protected void configure(HttpSecurity http) throws Exception {
//            http.csrf().disable()
//                    .antMatcher("/web/v1.0/**")
//                    .authorizeRequests()
//                    .antMatchers("/web/v1.0/prosumer/register").permitAll()
//                    .antMatchers("/web/v1.0/prosumer/forgotPassword").permitAll()
//                    .antMatchers("/web/v1.0/server/status").permitAll()
//                    .anyRequest().authenticated().and()
//                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
//                    .and()
//                    .httpBasic();
//
//
///*
//            http.csrf().disable()
//                    .authorizeRequests()
//                    .antMatchers("/prosumer/register").permitAll()
//                    .antMatchers("/prosumer/login").permitAll()
//                    .anyRequest().authenticated().and()
//                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
//                    .and()
//                    .httpBasic();
//            */
//
//        }
//
//        @Override
//        protected void configure(AuthenticationManagerBuilder auth)
//                throws Exception {
//            auth.userDetailsService(foaUserDetailsService);
//        }
//
//        @Bean
//        public FilterRegistrationBean corsFilter() {
//            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//            CorsConfiguration config = new CorsConfiguration();
//            config.setAllowCredentials(true);
//            config.addAllowedOrigin("*");
//            config.addAllowedHeader("*");
//            config.addAllowedMethod("*");
//            source.registerCorsConfiguration("/**", config);
//            FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
//            bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
//            return bean;
//        }
//
//
///*@Bean
//        CorsConfigurationSource corsConfigurationSource() {
//            CorsConfiguration configuration = new CorsConfiguration();
//            configuration.setAllowedOrigin("*");
//            configuration.setAllowedMethods(Arrays.asList("GET","POST"));
//            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//            source.registerCorsConfiguration("*//*
//*/
///**", configuration);
//         return source;
//         }
//         */


    }
}
