package Production.AuthService.SecurityUtils;


import Production.AuthService.entities.Role;
import Production.AuthService.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTTL;
    private final long refreshTTL;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTTL,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTTL,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        if(secret ==null || secret.length()<64){
            throw new IllegalArgumentException("Invalid Secret");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTTL = accessTTL;
        this.refreshTTL = refreshTTL;
        this.issuer = issuer;
    }


    //generate token -:generateAccessToken(User user)
    /*
    {
  "iss": "api.substring.com",
  "sub": "admin@example.com",
  "uid": "a2be2538-2696-49d9-b334-2a9348efa93e",
  "roles": ["ROLE_ADMIN", "ROLE_USER"],
  "iat": 1707316220,
  "exp": 1707319820
}

     */
    public String generateAccessToken(User user) {

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTTL);

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)   // or role.getRoleName()
                .collect(Collectors.toList());

        return Jwts.builder()
                .issuer(issuer)
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())     // primary identity
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("tpe","access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //generateRefereshToken
    public String generateRefreshToken(User user,String jti) {

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(refreshTTL);

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)   // or role.getRoleName()
                .collect(Collectors.toList());

        return Jwts.builder()
                .issuer(issuer)
                .id(jti)
                .subject(user.getId().toString())     // primary identity
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("tpe","refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //parse the token
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalIdentifierException("Invalid or expired JWT token");
        }
    }


    //validate token
    public Claims validateAccessToken(String token) {
        Claims claims = parseToken(token);

        String type = claims.get("tpe", String.class);
        if (!"access".equals(type)) {
            throw new IllegalIdentifierException("Invalid token type");
        }

        return claims;
    }

    public Claims validateRefreshToken(String token) {
        Claims claims = parseToken(token);

        String type = claims.get("tpe", String.class);
        if (!"refresh".equals(type)) {
            throw new IllegalIdentifierException("Invalid token type");
        }

        return claims;
    }

    public UUID extractUserId(String token) {
        Claims claims = validateAccessToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public String extractEmail(String token) {
        return validateAccessToken(token).get("email", String.class);
    }

    public List<String> extractRoles(String token) {
        return validateAccessToken(token).get("roles", List.class);
    }
}
