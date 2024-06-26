package song.pg.payment.config.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import song.pg.payment.utils.JwtUtil;
import song.pg.payment.utils.KnownException;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter
{
  private final JwtUtil jwtUtil;
  private static final String AUTH_HEADER = "Authorization";
  private static final String BEARER = "Bearer ";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
  {
    log.debug("JWT 인증 필터 시작");
    final String authHeader = request.getHeader(AUTH_HEADER);

    if (authHeader != null && authHeader.startsWith(BEARER))
    {
      final String token = authHeader.substring(BEARER.length());
      Claims claims = jwtUtil.getTokenClaims(token);

      UserDetails userDetails = new CustomerUserDetails(
        claims.get("di", String.class),
        claims.get("mid", String.class)
      );

      try
      {
        SecurityContextHolder.getContext().setAuthentication(
          new UsernamePasswordAuthenticationToken(
            userDetails,
            userDetails,
            userDetails.getAuthorities()
          )
        );
      }
      catch (KnownException e)
      {
        log.warn(e.getMessage());
        filterChain.doFilter(request, response);
      }
    }

    filterChain.doFilter(request, response);
  }
}
