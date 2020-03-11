package org.goflex.wp2.fogenerator.security;

/**
 * Created by bijay on 7/6/17.
 */

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;

class TokenAuthenticationService {

    static final String CLAIM_FOAID = "TheSecretKeyForFOA";
    static final String SECRET = "TheSecretKeyForFOA";
    static final String CLAIM_ISSUER = "iss";
    static final String TOKEN_PREFIX = "Bearer";
    static final String HEADER_STRING = "Authorization";

    static final String CLAIM_CREATIONDATE = "iat";
    static final String CLAIM_EXPIRATIONDATE = "exp";
    static final long EXPIRATIONTIME = 864_000_000; // 10 days

    static final String issuer = "FMANFMAR.io";

    static Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
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

    static void addAuthentication(HttpServletResponse res, String foaID) {

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_FOAID, foaID);
        claims.put(CLAIM_ISSUER, issuer);
        claims.put(CLAIM_CREATIONDATE, new Date());
        final Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATIONTIME);

        String JWT = Jwts.builder()
                .setSubject(foaID)
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
        res.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + JWT);
    }

    static Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);

        if (token != null) {
            // parse the token.
            /*String user = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody()
                    .getSubject();*/
            String user = getFoaID(token);


            return user != null ?
                    new UsernamePasswordAuthenticationToken(user, null, emptyList()) :
                    null;
        }
        return null;
    }

    public Date getCreationDate(String token) {
        Date createdDate;
        try {
            final Claims claims = getClaimsFromToken(token);
            createdDate = new Date((Long) claims.get(CLAIM_CREATIONDATE));

        } catch (Exception ex) {
            createdDate = null;
        }

        return createdDate;
    }
}