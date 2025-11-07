package com.comp5348.email.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

// Event message received from RabbitMQ when delivery status changes
// DeliveryCo service sends these, we receive and send emails
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryEventDto {

  public Long orderId;
  public String status;
  public Integer progress;
  public String customerEmail;
  public String customerName;
  public String eventType;

  public DeliveryEventDto() {
  }

  public DeliveryEventDto(Long orderId, String status, Integer progress,
                          String customerEmail, String customerName, String eventType) {
    this.orderId = orderId;
    this.status = status;
    this.progress = progress;
    this.customerEmail = customerEmail;
    this.customerName = customerName;
    this.eventType = eventType;
  }

}
