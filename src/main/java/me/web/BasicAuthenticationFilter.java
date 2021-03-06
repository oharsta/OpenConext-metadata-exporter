package me.web;

import static java.util.Base64.getDecoder;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import me.model.AccessNotAllowedException;

public class BasicAuthenticationFilter extends OncePerRequestFilter {

  private final Pattern pattern = Pattern.compile("/health|/jsError|/info|/favicon\\.ico");

  private final String userName;
  private final String password;

  public BasicAuthenticationFilter(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String url = getRequestPath(request);
    if (pattern.matcher(url).matches()) {
      filterChain.doFilter(request, response);
      return;
    }

    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith("Basic ")) {
      throw new AccessNotAllowedException("Authorization header not set for protected endpoint '" + url + "'");
    }

    String[] tokens = extractAndDecodeHeader(header, request);
    String userName = tokens[0];
    String password = tokens[1];

    logger.info("Basic Authentication Authorization header found for user '" + userName + "'");

    if (!this.userName.equals(userName) || !this.password.equals(password)) {
      throw new AccessNotAllowedException("Wrong username / credentials for '" + userName + "'");
    }
    filterChain.doFilter(request, response);
  }

  private String[] extractAndDecodeHeader(String header, HttpServletRequest request)
      throws IOException {
    String token = new String(getDecoder().decode(header.substring(6)));
    int delim = token.indexOf(":");
    if (delim == -1) {
      throw new AccessNotAllowedException("Authorization contains wrong format '" + token + "'");
    }
    return new String[]{token.substring(0, delim), token.substring(delim + 1)};
  }

  private String getRequestPath(HttpServletRequest request) {
    String url = request.getServletPath();
    if (request.getPathInfo() != null) {
      url += request.getPathInfo();
    }
    return url;
  }


}
