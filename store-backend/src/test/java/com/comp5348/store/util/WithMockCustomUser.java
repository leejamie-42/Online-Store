package com.comp5348.store.util;

import com.comp5348.store.model.auth.UserRole;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * Custom annotation for mocking authenticated User principal in tests.
 *
 * <p>
 * This annotation creates a mock User that can be used
 * with @AuthenticationPrincipal.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
  long id() default 1L;

  String username() default "Test User";

  String email() default "test@example.com";

  UserRole role() default UserRole.CUSTOMER;
}
