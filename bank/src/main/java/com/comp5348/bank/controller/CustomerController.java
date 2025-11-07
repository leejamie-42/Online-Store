package com.comp5348.bank.controller;

import com.comp5348.bank.dto.CustomerDTO;
import com.comp5348.bank.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(
            @RequestParam String firstName,
            @RequestParam String lastName) {

        log.info("Creating customer: firstName={}, lastName={}", firstName, lastName);
        CustomerDTO customer = customerService.createCustomer(firstName, lastName);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }
}
