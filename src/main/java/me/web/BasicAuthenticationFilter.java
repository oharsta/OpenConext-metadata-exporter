package me.web;

import me.model.AccessNotAllowedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

import static java.util.Base64.getDecoder;

public class BasicAuthenticationFilter extends OncePerRequestFilter {

  private final String userName;
  private final String password;

  public BasicAuthenticationFilter(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith("Basic ")) {
      throw new AccessNotAllowedException();
    }

    String[] tokens = extractAndDecodeHeader(header, request);
    String userName = tokens[0];
    String password = tokens[1];

    logger.debug("Basic Authentication Authorization header found for user '" + userName + "'");

    if (!this.userName.equals(userName) || !this.password.equals(password)) {
      throw new AccessNotAllowedException();
    }
    filterChain.doFilter(request, response);
  }

  private String[] extractAndDecodeHeader(String header, HttpServletRequest request)
      throws IOException {
    String token = new String(getDecoder().decode(header.substring(6)));
    int delim = token.indexOf(":");
    if (delim == -1) {
      throw new AccessNotAllowedException();
    }
    return new String[]{token.substring(0, delim), token.substring(delim + 1)};
  }

}