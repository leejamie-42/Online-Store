package com.comp5348.bank.service;

import com.comp5348.bank.dto.CustomerDTO;
import com.comp5348.bank.model.Customer;
import com.comp5348.bank.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerDTO createCustomer(String firstName, String lastName) {
        log.info("Creating customer: firstName={}, lastName={}", firstName, lastName);

        Customer customer = new Customer(firstName, lastName);
        customer = customerRepository.save(customer);
        log.info("Customer created: id={}", customer.getId());
        return new CustomerDTO(customer);
    }
}
