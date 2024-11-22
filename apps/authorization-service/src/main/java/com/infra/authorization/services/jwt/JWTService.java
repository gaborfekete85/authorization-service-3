package com.infra.authorization.services.jwt;

import com.infra.authorization.config.AppProperties;
import com.infra.authorization.model.UserDetail;
import com.infra.authorization.persistence.entities.Role;
import com.infra.authorization.persistence.entities.User;
import com.infra.authorization.services.UserService;
import com.infra.authorization.utils.SecurityUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.lang.Strings;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JWTService {

    @Value("${app.publicEndpoint}")
    private String baseUrl;

    private final AppProperties appProperties;

    private final UserService userService;

    private final String ROLE_PREFIX = "ROLE_";

    public String createToken(UserDetails userDetail) {
        User user = userService.findByEmail(userDetail.getUsername());
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());
//        List<String> userRoles = user.getRoles().stream().map(x -> x.getName().name()).toList();
//        List<String> userRoles = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().map(x -> x.getAuthority().replaceAll("ROLE_", "")).toList();

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(expiryDate)
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles().stream().map(x -> x.getName().name().replaceAll(ROLE_PREFIX, Strings.EMPTY)).toList())
                .issuer(baseUrl)
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

    public UUID getUserId(String token) {
        return UUID.fromString(Objects.requireNonNull(extractClaim(token, Claims::getSubject)));
    }
    public String getUserName(String token) {
        return extractClaim(token, (x) -> x.get("email").toString());
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

    public Map<String, Object> getClaims(String token) {
        Claims claims = extractClaims(token);
        List<String> roles = claims.get("roles", List.class);
        Set<String> rolesPrefixed = roles.stream().map(x -> String.format("%s_%s", SecurityUtil.ROLE_PREFIX, x)).collect(Collectors.toSet());
        return Map.of(
                Claims.SUBJECT, claims.getSubject(),
                Claims.ISSUED_AT, claims.getIssuedAt(),
                Claims.EXPIRATION, claims.getExpiration(),
                Claims.ISSUER, claims.getIssuer(),
                "email", claims.get("email"),
                "roles", rolesPrefixed
        );
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
