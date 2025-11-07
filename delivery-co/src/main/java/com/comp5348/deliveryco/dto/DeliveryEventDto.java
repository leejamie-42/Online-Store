package com.comp5348.deliveryco.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

// Event message sent through RabbitMQ when delivery status changes
// Email service listens for these and sends notifications to customers
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
