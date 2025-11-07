package com.comp5348.store.dto.auth;

import com.comp5348.store.model.auth.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Long id;
  private String name;
  private String email;
  private UserRole role;
}
