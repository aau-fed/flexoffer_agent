package org.goflex.wp2.app.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

public class TokenGenerator {

    static final String CLAIM_FOAID = "TheSecretKeyForFOA";
    static final String SECRET = "TheSecretKeyForFOA";
    static final String CLAIM_ISSUER = "iss";
    static final String TOKEN_PREFIX = "Bearer";
    static final String HEADER_STRING = "Authorization";
    static final String ROLE = "role";

    static final String CLAIM_CREATIONDATE = "iat";
    static final String CLAIM_EXPIRATIONDATE = "exp";
    //static final long EXPIRATIONTIME = 864_000_000; // 10 days
    static final long EXPIRATIONTIME = 10*24*60*60*1000; // (hours*minutes*seconds*milliseconds)

    static final String issuer = "AAUFOA.io";


    public String getToken(String foaID, Collection<? extends GrantedAuthority> authorities){

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_FOAID, foaID);
        //claims.put(ROLE, authorities.stream().map(s -> new SimpleGrantedAuthority(s.getAuthority())).filter(Objects::nonNull).collect(Collectors.toList()));
        claims.put(ROLE, authorities.stream().map(ga -> new SimpleGrantedAuthority(((GrantedAuthority) ga).getAuthority())).filter(Objects::nonNull).collect(Collectors.toList()));
        claims.put(CLAIM_ISSUER, issuer);
        claims.put(CLAIM_CREATIONDATE, new Date());
        Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATIONTIME);
        claims.put(CLAIM_EXPIRATIONDATE, expirationDate);

        String JWT = Jwts.builder()
                .setSubject(foaID)
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
        return JWT;

    }
}
