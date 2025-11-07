package com.comp5348.email.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

// Email notification message sent via RabbitMQ
// Has all the info we need to send an email - recipient, subject, body etc
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailNotificationDto {

  public Long orderId;
  public String emailType;
  public String recipient;
  public String subject;
  public String messageBody;
  public String messageId;

  public EmailNotificationDto() {
  }

  public EmailNotificationDto(Long orderId, String emailType, String recipient, String subject, String messageBody) {
    this.orderId = orderId;
    this.emailType = emailType;
    this.recipient = recipient;
    this.subject = subject;
    this.messageBody = messageBody;
  }

}
