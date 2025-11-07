package com.comp5348.store.dto.order;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reusable shipping information DTO with Australian address validation.
 *
 * <p>Validation Rules:</p>
 * <ul>
 *   <li>State: Must be valid Australian state (NSW, VIC, QLD, SA, WA, TAS, NT, ACT)</li>
 *   <li>Postcode: Exactly 4 digits</li>
 *   <li>Mobile: Australian format (10 digits starting with 0)</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingInfoDto {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(
        regexp = "^0[2-9]\\d{8}$",
        message = "Mobile number must be a valid Australian mobile number (10 digits starting with 0)"
    )
    private String mobileNumber;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Pattern(
        regexp = "^(NSW|VIC|QLD|SA|WA|TAS|NT|ACT)$",
        message = "State must be a valid Australian state (NSW, VIC, QLD, SA, WA, TAS, NT, ACT)"
    )
    private String state;

    @NotBlank(message = "Postcode is required")
    @Pattern(
        regexp = "^\\d{4}$",
        message = "Postcode must be exactly 4 digits"
    )
    private String postcode;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Builder.Default
    private String country = "Australia";
}
