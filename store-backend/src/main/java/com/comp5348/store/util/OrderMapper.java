package com.comp5348.store.util;

import com.comp5348.store.dto.order.*;
import com.comp5348.store.model.order.Order;
import java.util.List;

/**
 * Utility class for mapping Order entities to DTOs.
 *
 * <p>Provides static methods for converting Order entities to various response DTOs.</p>
 */
public class OrderMapper {

    private OrderMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Convert Order entity to CreateOrderResponse.
     *
     * @param order the order entity
     * @return CreateOrderResponse DTO
     */
    public static CreateOrderResponse toCreateOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        return CreateOrderResponse.builder()
            .orderId(order.getId())
            .status(order.getStatus().name())
            .total(order.getTotalAmount())
            .build();
    }

    /**
     * Convert Order entity to OrderDetailResponse.
     *
     * @param order the order entity
     * @return OrderDetailResponse DTO with complete information
     */
    public static OrderDetailResponse toOrderDetailResponse(Order order) {
        if (order == null) {
            return null;
        }

        OrderProductDto productDto = getOrderProductDto(order);

        List<OrderProductDto> products = List.of(productDto);

        return OrderDetailResponse.builder()
            .orderId(order.getId())
            .userId(order.getUser().getId())
            .products(products)
            .totalAmount(order.getTotalAmount())
            .status(order.getStatus().name())
            .shippingInfo(toShippingInfoDto(order))
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }

    /**
     * Convert Order entity to OrderSummaryResponse.
     *
     * @param order the order entity
     * @return OrderSummaryResponse DTO for list views
     */
    public static OrderHistoryResponse toOrderHistoryResponse(Order order) {
        if (order == null) {
            return null;
        }

        OrderProductDto productDto = getOrderProductDto(order);

        List<OrderProductDto> products = List.of(productDto);

        return OrderHistoryResponse.builder()
            .orderId(order.getId())
            .products(products)
            .totalAmount(order.getTotalAmount())
            .status(order.getStatus().name())
            .createdAt(order.getCreatedAt())
            .build();
    }

    /**
     * Extract shipping information from Order entity to ShippingInfoDto.
     *
     * @param order the order entity
     * @return ShippingInfoDto with address details
     */
    public static ShippingInfoDto toShippingInfoDto(Order order) {
        if (order == null) {
            return null;
        }

        return ShippingInfoDto.builder()
            .firstName(order.getFirstName())
            .lastName(order.getLastName())
            .email(order.getEmail())
            .mobileNumber(order.getMobileNumber())
            .addressLine1(order.getAddressLine1())
            .city(order.getCity())
            .state(order.getState())
            .postcode(order.getPostcode())
            .country(order.getCountry())
            .build();
    }

    private static OrderProductDto getOrderProductDto(Order order) {
        return OrderProductDto.builder()
            .id(order.getProduct().getId())
            .description(order.getProduct().getDescription())
            .name(order.getProduct().getName())
            .price(order.getProduct().getPrice())
            .quantity(order.getQuantity())
            .build();
    }
}
