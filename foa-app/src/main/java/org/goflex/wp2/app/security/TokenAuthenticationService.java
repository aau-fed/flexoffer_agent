package org.goflex.wp2.app.security;

/**
 * Created by bijay on 7/6/17.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class TokenAuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationService.class);

    static final String CLAIM_FOAID = "TheSecretKeyForFOA";
    static final String SECRET = "TheSecretKeyForFOA";
    static final String CLAIM_ISSUER = "iss";
    static final String TOKEN_PREFIX = "Bearer";
    static final String HEADER_STRING = "Authorization";
    static final String ROLE = "role";

    static final String CLAIM_CREATIONDATE = "iat";
    static final String CLAIM_EXPIRATIONDATE = "exp";
    static final long EXPIRATIONTIME = 864_000_000; // 10 days

    static final String issuer = "FMANFMAR.io";


    static Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    static String getFoaID(String token) {
        String foaID;
        try {
            final Claims claims = getClaimsFromToken(token);
            foaID = (String) claims.get(CLAIM_FOAID);

        } catch (Exception ex) {
            foaID = null;
        }

        return foaID;
    }

    static List<Map<String, String>> getRoles(String token) {
        List<Map<String, String>> roles;
        try {
            final Claims claims = getClaimsFromToken(token);
            roles = (List<Map<String, String>>) claims.get(ROLE);

        } catch (Exception ex) {
            roles = null;
        }

        return roles;
    }


    static String getIssuer(String token) {
        String issuer;
        try {
            final Claims claims = getClaimsFromToken(token);
            issuer = (String) claims.get(CLAIM_ISSUER);

        } catch (Exception ex) {
            issuer = null;
        }

        return issuer;
    }

    static Date getCreationDate(String token) {
        Date createdDate;
        try {
            final Claims claims = getClaimsFromToken(token);
            createdDate = new Date((Long) claims.get(CLAIM_CREATIONDATE));

        } catch (Exception ex) {
            createdDate = null;
        }

        return createdDate;
    }


    static void addAuthentication(HttpServletResponse res, String foaID, Collection<? extends GrantedAuthority> authorities, Map<String, Object> usr) {

        /*Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_FOAID, foaID);
        claims.put(ROLE, authorities.stream().map(s -> new SimpleGrantedAuthority(s.getAuthority())).filter(Objects::nonNull).collect(Collectors.toList()));
        claims.put(CLAIM_ISSUER, issuer);
        claims.put(CLAIM_CREATIONDATE, new Date());
        Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATIONTIME);
        claims.put(CLAIM_EXPIRATIONDATE, expirationDate);

        String JWT = Jwts.builder()
                .setSubject(foaID)
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();*/
        TokenGenerator tokenGenerator = new TokenGenerator();
        String JWT = tokenGenerator.getToken(foaID, authorities);

        res.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + JWT);

        PrintWriter out = null;
        try {
            out = res.getWriter();
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            Gson gson = new GsonBuilder().serializeNulls().create();
            //usr.put("token","Bearer "+JWT);
            usr.put("token", JWT);
            String usrString = gson.toJson(usr);
            out.print("{\"status\": \"OK\", \"message\": \"success\", \"data\": " + usrString + "}");
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    static Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);

        try {
            if (token != null) {
                // parse the token.
            /*String user1 = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody().toString();*/
                String user = getFoaID(token);
                List<Map<String, String>> roles = getRoles(token);
                Collection<GrantedAuthority> authorities = roles.stream().map(role -> new SimpleGrantedAuthority(role.get("authority"))).collect(Collectors.toList());
                Date creationDate = getCreationDate(token);

                return user != null ?
                        //new UsernamePasswordAuthenticationToken(user, null, emptyList()) :
                        new UsernamePasswordAuthenticationToken(user, null, authorities) :
                        null;
            }
            return null;
        } catch (Exception ex) {
            LOGGER.error("authentication error: no roles present");
            return null;
        }
    }

}