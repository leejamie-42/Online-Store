package com.comp5348.email.dto;

import com.comp5348.email.entity.EmailLog;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

// DTO for email log responses
// Shows what emails we sent for an order, includes error info if it failed
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailLogDto {

  public Long id;
  public Long orderId;
  public String emailType;
  public String recipient;
  public String subject;
  public String messageBody;
  public LocalDateTime sentAt;
  public String status;
  public String errorMessage;
  public Integer retryCount;

  public EmailLogDto() {
  }

  public EmailLogDto(EmailLog emailLog) {
    this.id = emailLog.getId();
    this.orderId = emailLog.getOrderId();
    this.emailType = emailLog.getEmailType();
    this.recipient = emailLog.getRecipient();
    this.subject = emailLog.getSubject();
    this.messageBody = emailLog.getMessageBody();
    this.sentAt = emailLog.getSentAt();
    this.status = emailLog.getStatus();
    this.errorMessage = emailLog.getErrorMessage();
    this.retryCount = emailLog.getRetryCount();
  }

}
