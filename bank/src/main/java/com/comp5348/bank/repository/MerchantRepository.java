package com.comp5348.bank.repository;

import com.comp5348.bank.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByBillerCode(String billerCode);

    Optional<Merchant> findByAccountId(Long accountId);
}
