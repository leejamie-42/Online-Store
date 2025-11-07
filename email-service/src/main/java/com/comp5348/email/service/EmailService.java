package com.comp5348.email.service;

import com.comp5348.email.common.entity.ProcessedMessage;
import com.comp5348.email.common.repository.ProcessedMessageRepository;
import com.comp5348.email.entity.EmailLog;
import com.comp5348.email.repository.EmailLogRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Handles email sending logic
// In real app would integrate with actual email service (SendGrid, AWS SES etc)
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(
        EmailService.class
    );

    private final EmailLogRepository emailLogRepository;
    private final ProcessedMessageRepository processedMessageRepository;

    public EmailService(
        EmailLogRepository emailLogRepository,
        ProcessedMessageRepository processedMessageRepository
    ) {
        this.emailLogRepository = emailLogRepository;
        this.processedMessageRepository = processedMessageRepository;
    }

    // Send email notification
    // For now just logs and saves to database (mock implementation)
    @Transactional
    public void sendEmail(
        Long orderId,
        String emailType,
        String recipient,
        String subject,
        String messageBody,
        String messageId
    ) {
        // Check if we've already processed this message (idempotency)
        if (
            messageId != null &&
            processedMessageRepository.existsByMessageId(messageId)
        ) {
            log.info("Email already sent for message {}, skipping", messageId);
            return;
        }

        EmailLog emailLog = new EmailLog(
            orderId,
            emailType,
            recipient,
            subject,
            messageBody
        );

        try {
            // Print formatted email to console (mock email sending)
            printEmailToConsole(recipient, subject, messageBody);

            // Mark as sent
            emailLog.setStatus("SENT");
            emailLogRepository.save(emailLog);

            // Record that we've processed this message
            if (messageId != null) {
                ProcessedMessage processed = new ProcessedMessage(
                    messageId,
                    "EMAIL"
                );
                processedMessageRepository.save(processed);
            }

            log.info("Email sent successfully to {}", recipient);
        } catch (Exception e) {
            // If sending fails, log error and save the failure
            log.error(
                "Failed to send email to {}: {}",
                recipient,
                e.getMessage()
            );
            emailLog.setStatus("FAILED");
            emailLog.setErrorMessage(e.getMessage());
            emailLog.setRetryCount(emailLog.getRetryCount() + 1);
            emailLogRepository.save(emailLog);
        }
    }

    // Get email history for an order
    // Frontend can show this to customer
    public List<EmailLog> getEmailsForOrder(Long orderId) {
        return emailLogRepository.findByOrderIdOrderBySentAtDesc(orderId);
    }

    /**
     * Print formatted email to console (mock implementation)
     * In production, this would integrate with Mailgun/SendGrid/AWS SES
     */
    private void printEmailToConsole(
        String recipient,
        String subject,
        String body
    ) {
        String border = "=".repeat(80);
        String separator = "-".repeat(80);

        System.out.println("\n" + border);
        System.out.println("📧 EMAIL NOTIFICATION");
        System.out.println(border);
        System.out.println("To: " + recipient);
        System.out.println("Subject: " + subject);
        System.out.println(separator);
        System.out.println(body);
        System.out.println(border + "\n");

        log.info(
            "Email printed to console: to={}, subject={}",
            recipient,
            subject
        );
    }
}
