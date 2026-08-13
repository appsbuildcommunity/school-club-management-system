package com.appsBuild.club_management_system.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakJwtAuthenticationConverter
    implements Converter<Jwt, Collection<GrantedAuthority>> {

  public static final String REALM_ACCESS = "realm_access";
  public static final String ROLES = "roles";
  public static final String ROLE_PREFIX = "ROLE_";

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS);
    List<GrantedAuthority> authorities = new ArrayList<>();
    if (realmAccess != null && realmAccess.get(ROLES) instanceof Collection<?> roles) {
      for (Object role : roles) {
        if (role != null) {
          authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role));
        }
      }
    }
    return authorities;
  }
}
