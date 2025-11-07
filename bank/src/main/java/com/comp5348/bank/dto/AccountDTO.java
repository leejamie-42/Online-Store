package com.comp5348.bank.dto;

import com.comp5348.bank.model.Account;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO {
    private Long id;
    private Long customerId;
    private String name;
    private String type;
    private BigDecimal balance;

    public AccountDTO(Account account) {
        this.id = account.getId();
        this.customerId = account.getCustomer().getId();
        this.name = account.getName();
        this.type = account.getType().name();
        this.balance = account.getBalance();
    }
}
