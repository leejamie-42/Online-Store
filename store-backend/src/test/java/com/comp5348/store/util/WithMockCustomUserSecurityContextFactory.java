package com.comp5348.store.util;

import com.comp5348.store.model.auth.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * Security context factory for creating mock User principals in tests.
 *
 * <p>
 * Used by @WithMockCustomUser annotation to populate SecurityContext with a
 * real User object.
 * </p>
 */
public class WithMockCustomUserSecurityContextFactory
    implements WithSecurityContextFactory<WithMockCustomUser> {

  @Override
  public SecurityContext createSecurityContext(
      WithMockCustomUser customUser) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();

    User principal = User.builder()
        .id(customUser.id())
        .name(customUser.username())
        .email(customUser.email())
        .password("hashedPassword")
        .role(customUser.role())
        .enabled(true)
        .build();

    Authentication auth = new UsernamePasswordAuthenticationToken(
        principal,
        "password",
        principal.getAuthorities());
    context.setAuthentication(auth);
    return context;
  }
}
