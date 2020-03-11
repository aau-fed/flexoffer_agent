package org.goflex.wp2.app.security;

/**
 * Created by bijay on 7/6/17.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.goflex.wp2.core.models.FOAUserPrincipal;
import org.goflex.wp2.core.models.UserT;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JWTAuthFilter extends AbstractAuthenticationProcessingFilter {


    public JWTAuthFilter(String url, AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException, IOException, ServletException {
        UserT creds = new ObjectMapper()
                .readValue(req.getInputStream(), UserT.class);
        return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(
                        creds.getUserName(),
                        creds.getPassword(),
                        Collections.emptyList()
                )
        );
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest req,
            HttpServletResponse res, FilterChain chain,
            Authentication auth) throws IOException, ServletException {

        Object principal = auth.getPrincipal();
        UserT user = ((FOAUserPrincipal) principal).getUser();
        Map<String, Object> loggedInUser = new HashMap<>();
        loggedInUser.put("user", user.getUserName());
        loggedInUser.put("role", user.getRole());
        loggedInUser.put("organizationId", user.getOrganizationId());
        loggedInUser.put("pic", user.getPic());

        TokenAuthenticationService
                .addAuthentication(res, auth.getName(), auth.getAuthorities(), loggedInUser);
    }

    @Override
    protected void unsuccessfulAuthentication( HttpServletRequest req,
                                               HttpServletResponse res,
                                               AuthenticationException failed)throws IOException {
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Credential");
    }
}