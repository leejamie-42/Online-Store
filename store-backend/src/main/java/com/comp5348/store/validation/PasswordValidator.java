package com.comp5348.store.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

  private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
  private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    if (password == null) {
      return false;
    }

    if (password.length() < 8) {
      return false;
    }

    return UPPERCASE_PATTERN.matcher(password).find()
        && DIGIT_PATTERN.matcher(password).find();
  }
}
