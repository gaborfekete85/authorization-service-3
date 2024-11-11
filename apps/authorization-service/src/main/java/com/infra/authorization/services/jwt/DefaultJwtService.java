package com.infra.authorization.services.jwt;

import com.infra.authorization.config.AppProperties;
import com.infra.authorization.model.UserDetail;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultJwtService implements JWTService {

    private final AppProperties appProperties;
    public String generateToken(UserDetail userDetail) {
        SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        return Jwts.builder().subject(userDetail.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .claim("authorities", SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .claim("emailVerified", userDetail.getUser().getEmailVerified())
                .claim("roles", SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().map( x -> x.getAuthority().replaceAll("ROLE_", "")).toList())
                .expiration(new Date(System.currentTimeMillis() + appProperties.getAuth().getTokenExpirationMsec()))
                .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret())
                .compact();
    }

    public String createToken(UserDetail userDetail) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());

        return Jwts.builder()
                .setSubject(userDetail.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .claim("authorities", SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .claim("emailVerified", userDetail.getUser().getEmailVerified())
                .claim("roles", SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().map( x -> x.getAuthority().replaceAll("ROLE_", "")).toList())
                .setIssuer(userDetail.getUsername())
                .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret())
                .compact();
    }
    public String generateRefreshToken(Map<String, Object> extraClaims, UserDetail userDetail) {
        return Jwts.builder().subject(userDetail.getUsername())
                .claims(extraClaims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 7 * appProperties.getAuth().getTokenExpirationMsec()))
                .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret())
                .compact();
    }

    public String getUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(appProperties.getAuth().getTokenSecret()).build()
                .parseClaimsJws(token)
                .getBody();
//        return Jwts.parser().setSigningKey(appProperties.getAuth().getTokenSecret().getBytes()).build().parseSignedClaims(token).getPayload();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(appProperties.getAuth().getTokenSecret()).build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());

        //return Long.parseLong(claims.getSubject());
    }
    // TODO: REVISIT
    private Key getSigninKey() {
//        byte[] key = Decoders.BASE64.decode(appProperties.getAuth().getTokenSecret());
        byte[] key = appProperties.getAuth().getTokenSecret().getBytes();
        return Keys.hmacShaKeyFor(key);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = getUserName(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(final String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(appProperties.getAuth().getTokenSecret()).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }
}
