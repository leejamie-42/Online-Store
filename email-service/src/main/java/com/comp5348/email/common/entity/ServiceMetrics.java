package com.comp5348.email.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Logs performance data - throughput, response times, error rates etc.
// Useful for tracking if the service is running slow or having issues
// Both services log their metrics here
@Entity
@Table(name = "service_metrics")
public class ServiceMetrics {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "service_name", nullable = false, length = 50)
  private String serviceName;

  @Column(name = "metric_type", nullable = false, length = 50)
  private String metricType;

  @Column(name = "metric_value", nullable = false, precision = 10, scale = 2)
  private BigDecimal metricValue;

  @Column(name = "recorded_at", nullable = false)
  private LocalDateTime recordedAt;

  // Constructors
  public ServiceMetrics() {
    this.recordedAt = LocalDateTime.now();
  }

  public ServiceMetrics(String serviceName, String metricType, BigDecimal metricValue) {
    this();
    this.serviceName = serviceName;
    this.metricType = metricType;
    this.metricValue = metricValue;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getMetricType() {
    return metricType;
  }

  public void setMetricType(String metricType) {
    this.metricType = metricType;
  }

  public BigDecimal getMetricValue() {
    return metricValue;
  }

  public void setMetricValue(BigDecimal metricValue) {
    this.metricValue = metricValue;
  }

  public LocalDateTime getRecordedAt() {
    return recordedAt;
  }

  public void setRecordedAt(LocalDateTime recordedAt) {
    this.recordedAt = recordedAt;
  }
}
