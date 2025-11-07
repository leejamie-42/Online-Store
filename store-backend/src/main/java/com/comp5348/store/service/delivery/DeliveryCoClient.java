package com.comp5348.store.service.delivery;

import com.comp5348.store.dto.delivery.*;
import com.comp5348.store.exception.DeliveryServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryCoClient {

    private final RestTemplate restTemplate;

    @Value("${deliveryco.service.base-url}")
    private String deliveryCoBaseUrl;

    /**
     * Request shipment from DeliveryCo.
     */
    public DeliveryResponse requestShipment(DeliveryRequest request) {
        String url = deliveryCoBaseUrl + "/deliveryCo/api/shipments";

        try {
            log.info("Requesting shipment for order {}", request.getOrderId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DeliveryRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<DeliveryResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                DeliveryResponse.class
            );

            DeliveryResponse deliveryResponse = response.getBody();
            log.info("Successfully created shipment for order {}: shipmentId={}",
                request.getOrderId(), deliveryResponse.getShipmentId());

            return deliveryResponse;

        } catch (RestClientException e) {
            log.error("Failed to request shipment for order {}: {}",
                request.getOrderId(), e.getMessage());
            throw new DeliveryServiceException("Failed to communicate with DeliveryCo", e);
        }
    }

    /**
     * Register webhook callback URL with DeliveryCo.
     */
    public void registerWebhook(String callbackUrl) {
        String url = deliveryCoBaseUrl + "/deliveryCo/api/webhooks/register";

        try {
            log.info("Registering delivery webhook: {}", callbackUrl);

            Map<String, String> request = Map.of(
                "event", "SHIPMENT_STATUS_UPDATE",
                "callback_url", callbackUrl
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

            log.info("Successfully registered delivery webhook");

        } catch (RestClientException e) {
            log.error("Failed to register delivery webhook: {}", e.getMessage());
            // Don't throw - webhook registration failure shouldn't prevent app startup
        }
    }
}

