package com.comp5348.deliveryco.common.repository;

import com.comp5348.deliveryco.common.entity.ServiceMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// Query service performance metrics to analyse how the system is performing
@Repository
public interface ServiceMetricsRepository extends JpaRepository<ServiceMetrics, Long> {

  // Get metrics for a specific service and type
  // E.g. fetch all throughput data for Email service
  List<ServiceMetrics> findByServiceNameAndMetricTypeOrderByRecordedAtDesc(String serviceName, String metricType);

  // Get metrics for a service within a time range
  // Useful for performance reports or debugging issues
  List<ServiceMetrics> findByServiceNameAndRecordedAtBetweenOrderByRecordedAtDesc(
      String serviceName,
      LocalDateTime startTime,
      LocalDateTime endTime
  );

}
