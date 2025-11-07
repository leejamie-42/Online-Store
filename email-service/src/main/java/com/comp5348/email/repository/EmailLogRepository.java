package com.comp5348.email.repository;

import com.comp5348.email.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repo for email logs - helps us track what emails we sent and debug issues
@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

  // Get all emails sent for an order
  // Sorted newest first
  List<EmailLog> findByOrderIdOrderBySentAtDesc(Long orderId);

}
