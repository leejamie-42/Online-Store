package com.comp5348.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.comp5348.store.dto.delivery.*;
import com.comp5348.store.exception.ShipmentNotFoundException;
import com.comp5348.store.grpc.warehouse.DeliveryPackage;
import com.comp5348.store.model.Product;
import com.comp5348.store.model.order.Order;
import com.comp5348.store.model.order.OrderStatus;
import com.comp5348.store.model.shipment.*;
import com.comp5348.store.repository.*;
import com.comp5348.store.service.delivery.DeliveryCoClient;
import com.comp5348.store.service.event.EventPublisher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DeliveryCoClient deliveryCoClient;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ShipmentService shipmentService;

    @Test
    void requestShipment_Success() {
        // Given
        Order order = Order.builder()
            .id(1L)
            .status(OrderStatus.PROCESSING)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .mobileNumber("+61412345678")
            .addressLine1("123 Test St")
            .city("Sydney")
            .state("NSW")
            .postcode("2000")
            .totalAmount(BigDecimal.valueOf(100.00))
            .build();

        List<DeliveryPackage> packages = Collections.singletonList(
            DeliveryPackage.newBuilder()
                .setWarehouseAddress(
                    "Warehouse A, 123 Storage St, Sydney NSW 2000"
                )
                .setProductId("PRD-1")
                .setQuantity(1)
                .build()
        );

        DeliveryResponse deliveryResponse = DeliveryResponse.builder()
            .shipmentId("SHP-001")
            .trackingNumber("TRACK-001")
            .carrier("DeliveryCo")
            .estimatedDelivery(LocalDateTime.now().plusDays(3))
            .status("SHIPMENT_CREATED")
            .build();

        when(shipmentRepository.existsByOrderId(1L)).thenReturn(false);
        when(
            deliveryCoClient.requestShipment(any(DeliveryRequest.class))
        ).thenReturn(deliveryResponse);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(i ->
            i.getArgument(0)
        );

        // When
        Shipment shipment = shipmentService.requestShipment(order, packages);

        // Then
        assertNotNull(shipment);
        assertEquals("SHP-001", shipment.getShipmentId());
        assertEquals(ShipmentStatus.SHIPMENT_CREATED, shipment.getStatus());
        assertEquals("TRACK-001", shipment.getTrackingNumber());
        assertEquals("DeliveryCo", shipment.getCarrier());
        assertEquals(
            "123 Test St, Sydney NSW 2000",
            shipment.getDeliveryAddress()
        );

        verify(shipmentRepository).existsByOrderId(1L);
        verify(deliveryCoClient).requestShipment(
            argThat(
                request ->
                    request.getDeliveryPackages() != null &&
                    request.getDeliveryPackages().size() == 1 &&
                    request.getPackageCount() == 1
            )
        );
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    void requestShipment_Success_WithMultipleWarehouses() {
        // Given
        Order order = Order.builder()
            .id(2L)
            .status(OrderStatus.PROCESSING)
            .firstName("Jane")
            .lastName("Smith")
            .email("jane@example.com")
            .mobileNumber("+61423456789")
            .addressLine1("456 Market Rd")
            .city("Melbourne")
            .state("VIC")
            .postcode("3000")
            .totalAmount(BigDecimal.valueOf(250.00))
            .build();

        // Multiple packages from different warehouses
        List<DeliveryPackage> packages = List.of(
            DeliveryPackage.newBuilder()
                .setWarehouseAddress(
                    "Warehouse A, 123 Storage St, Sydney NSW 2000"
                )
                .setProductId("PRD-1")
                .setQuantity(2)
                .build(),
            DeliveryPackage.newBuilder()
                .setWarehouseAddress(
                    "Warehouse B, 456 Industrial Ave, Melbourne VIC 3001"
                )
                .setProductId("PRD-1")
                .setQuantity(3)
                .build()
        );

        DeliveryResponse deliveryResponse = DeliveryResponse.builder()
            .shipmentId("SHP-002")
            .trackingNumber("TRACK-002")
            .carrier("DeliveryCo")
            .estimatedDelivery(LocalDateTime.now().plusDays(5))
            .status("SHIPMENT_CREATED")
            .build();

        when(shipmentRepository.existsByOrderId(2L)).thenReturn(false);
        when(
            deliveryCoClient.requestShipment(any(DeliveryRequest.class))
        ).thenReturn(deliveryResponse);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(i ->
            i.getArgument(0)
        );

        // When
        Shipment shipment = shipmentService.requestShipment(order, packages);

        // Then
        assertNotNull(shipment);
        assertEquals("SHP-002", shipment.getShipmentId());
        assertEquals(ShipmentStatus.SHIPMENT_CREATED, shipment.getStatus());
        assertEquals("TRACK-002", shipment.getTrackingNumber());
        assertEquals(2, packages.size()); // Verify multiple packages

        verify(shipmentRepository).existsByOrderId(2L);
        verify(deliveryCoClient).requestShipment(
            argThat(
                request ->
                    request.getDeliveryPackages() != null &&
                    request.getDeliveryPackages().size() == 2 &&
                    request.getPackageCount() == 2
            )
        );
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    void requestShipment_ThrowsException_WhenOrderNotInProcessingStatus() {
        // Given
        Order order = Order.builder()
            .id(1L)
            .status(OrderStatus.PENDING)
            .build();

        List<DeliveryPackage> packages = Collections.singletonList(
            DeliveryPackage.newBuilder()
                .setWarehouseAddress("123 Warehouse St, Sydney NSW 2000")
                .setProductId("PRD-1")
                .setQuantity(1)
                .build()
        );

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> shipmentService.requestShipment(order, packages)
        );

        assertTrue(
            exception
                .getMessage()
                .contains(
                    "Cannot request shipment for order not in PROCESSING status"
                )
        );
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void requestShipment_ThrowsException_WhenShipmentAlreadyExists() {
        // Given
        Order order = Order.builder()
            .id(1L)
            .status(OrderStatus.PROCESSING)
            .build();

        List<DeliveryPackage> packages = Collections.singletonList(
            DeliveryPackage.newBuilder()
                .setWarehouseAddress("123 Warehouse St, Sydney NSW 2000")
                .setProductId("PRD-1")
                .setQuantity(1)
                .build()
        );

        when(shipmentRepository.existsByOrderId(1L)).thenReturn(true);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> shipmentService.requestShipment(order, packages)
        );

        assertTrue(
            exception.getMessage().contains("Shipment already exists for order")
        );
        verify(deliveryCoClient, never()).requestShipment(any());
    }

    @Test
    void handleDeliveryWebhook_UpdatesStatusAndSendsEmail_ForPickedUp() {
        // Given
        DeliveryWebhookEvent event = DeliveryWebhookEvent.builder()
            .shipmentId("SHP-001")
            .event("PICKED_UP")
            .timestamp(LocalDateTime.now())
            .build();

        Order order = Order.builder()
            .id(1L)
            .email("john@example.com")
            .firstName("John")
            .lastName("Doe")
            .status(OrderStatus.PROCESSING)
            .build();

        Shipment shipment = Shipment.builder()
            .id(1L)
            .order(order)
            .shipmentId("SHP-001")
            .status(ShipmentStatus.SHIPMENT_CREATED)
            .trackingNumber("TRACK-001")
            .estimatedDelivery(LocalDateTime.now().plusDays(3))
            .build();

        when(shipmentRepository.findByShipmentId("SHP-001")).thenReturn(
            Optional.of(shipment)
        );
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(i ->
            i.getArgument(0)
        );
        when(orderRepository.save(any(Order.class))).thenAnswer(i ->
            i.getArgument(0)
        );

        // When
        shipmentService.handleDeliveryWebhook(event);

        // Then
        assertEquals(ShipmentStatus.PICKED_UP, shipment.getStatus());
        assertEquals(OrderStatus.PICKED_UP, order.getStatus());
        verify(shipmentRepository).save(shipment);
        verify(orderRepository).save(order);
        verify(eventPublisher).publishEmailEvent(
            argThat(
                emailEvent ->
                    emailEvent.getType().equals("DELIVERY_UPDATE") &&
                    emailEvent.getTo().equals("john@example.com") &&
                    emailEvent.getParams().get("status").equals("PICKED_UP")
            )
        );
    }

    @Test
    void handleDeliveryWebhook_UpdatesStatusAndSendsEmail_ForInTransit() {
        // Given
        DeliveryWebhookEvent event = DeliveryWebhookEvent.builder()
            .shipmentId("SHP-002")
            .event("IN_TRANSIT")
            .timestamp(LocalDateTime.now())
            .build();

        Order order = Order.builder()
            .id(2L)
            .email("jane@example.com")
            .firstName("Jane")
            .lastName("Smith")
            .status(OrderStatus.PICKED_UP)
            .build();

        Shipment shipment = Shipment.builder()
            .id(2L)
            .order(order)
            .shipmentId("SHP-002")
            .status(ShipmentStatus.PICKED_UP)
            .trackingNumber("TRACK-002")
            .estimatedDelivery(LocalDateTime.now().plusDays(2))
            .build();

        when(shipmentRepository.findByShipmentId("SHP-002")).thenReturn(
            Optional.of(shipment)
        );
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(i ->
            i.getArgument(0)
        );
        when(orderRepository.save(any(Order.class))).thenAnswer(i ->
            i.getArgument(0)
        );

        // When
        shipmentService.handleDeliveryWebhook(event);

        // Then
        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.getStatus());
        assertEquals(OrderStatus.DELIVERING, order.getStatus());
        verify(shipmentRepository).save(shipment);
        verify(orderRepository).save(order);
        verify(eventPublisher).publishEmailEvent(
            argThat(
                emailEvent ->
                    emailEvent.getType().equals("DELIVERY_UPDATE") &&
                    emailEvent.getParams().get("status").equals("IN_TRANSIT")
            )
        );
    }

    @Test
    void handleDeliveryWebhook_UpdatesStatusAndSendsEmail_ForDelivered() {
        // Given
        DeliveryWebhookEvent event = DeliveryWebhookEvent.builder()
            .shipmentId("SHP-003")
            .event("DELIVERED")
            .timestamp(LocalDateTime.now())
            .build();

        Order order = Order.builder()
            .id(3L)
            .email("bob@example.com")
            .firstName("Bob")
            .lastName("Johnson")
            .status(OrderStatus.DELIVERING)
            .build();

        Shipment shipment = Shipment.builder()
            .id(3L)
            .order(order)
            .shipmentId("SHP-003")
            .status(ShipmentStatus.IN_TRANSIT)
            .trackingNumber("TRACK-003")
            .estimatedDelivery(LocalDateTime.now().plusDays(1))
            .build();

        when(shipmentRepository.findByShipmentId("SHP-003")).thenReturn(
            Optional.of(shipment)
        );
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(i ->
            i.getArgument(0)
        );
        when(orderRepository.save(any(Order.class))).thenAnswer(i ->
            i.getArgument(0)
        );

        // When
        shipmentService.handleDeliveryWebhook(event);

        // Then
        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertNotNull(shipment.getActualDelivery());
        verify(shipmentRepository).save(shipment);
        verify(orderRepository).save(order);
        verify(eventPublisher).publishEmailEvent(
            argThat(
                emailEvent ->
                    emailEvent.getType().equals("DELIVERY_UPDATE") &&
                    emailEvent.getParams().get("status").equals("DELIVERED")
            )
        );
    }

    @Test
    void handleDeliveryWebhook_UpdatesStatusAndSendsEmail_ForLost() {
        // Given
        DeliveryWebhookEvent event = DeliveryWebhookEvent.builder()
            .shipmentId("SHP-004")
            .event("LOST")
            .timestamp(LocalDateTime.now())
            .build();

        Product product = Product.builder()
            .id(100L)
            .name("Test Product")
            .build();

        Order order = Order.builder()
            .id(4L)
            .email("alice@example.com")
            .firstName("Alice")
            .lastName("Williams")
            .status(OrderStatus.DELIVERING)
            .product(product)
            .quantity(2)
            .build();

        Shipment shipment = Shipment.builder()
            .id(4L)
            .order(order)
            .shipmentId("SHP-004")
            .status(ShipmentStatus.IN_TRANSIT)
            .trackingNumber("TRACK-004")
            .build();

        when(shipmentRepository.findByShipmentId("SHP-004")).thenReturn(
            Optional.of(shipment)
        );
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(i ->
            i.getArgument(0)
        );
        when(orderRepository.save(any(Order.class))).thenAnswer(i ->
            i.getArgument(0)
        );

        // When
        shipmentService.handleDeliveryWebhook(event);

        // Then
        assertEquals(ShipmentStatus.LOST, shipment.getStatus());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(shipmentRepository).save(shipment);
        verify(orderRepository).save(order);
        verify(eventPublisher).publishEmailEvent(
            argThat(
                emailEvent ->
                    emailEvent.getType().equals("DELIVERY_UPDATE") &&
                    emailEvent.getParams().get("status").equals("LOST")
            )
        );

        // Verify inventory rollback is published
        verify(eventPublisher).publishInventoryRollbackEvent(
            argThat(
                rollbackEvent ->
                    rollbackEvent.getOrderId().equals(4L) &&
                    rollbackEvent.getProductId().equals(100L) &&
                    rollbackEvent.getAmount() == 2 &&
                    rollbackEvent.getReason().equals("Shipment lost")
            )
        );
    }

    @Test
    void handleDeliveryWebhook_ThrowsException_WhenShipmentNotFound() {
        // Given
        DeliveryWebhookEvent event = DeliveryWebhookEvent.builder()
            .shipmentId("SHP-999")
            .event("PICKED_UP")
            .timestamp(LocalDateTime.now())
            .build();

        when(shipmentRepository.findByShipmentId("SHP-999")).thenReturn(
            Optional.empty()
        );

        // When & Then
        ShipmentNotFoundException exception = assertThrows(
            ShipmentNotFoundException.class,
            () -> shipmentService.handleDeliveryWebhook(event)
        );

        assertTrue(
            exception.getMessage().contains("Shipment not found: SHP-999")
        );
        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(eventPublisher, never()).publishEmailEvent(any());
    }

    @Test
    void handleDeliveryWebhook_ThrowsException_ForInvalidEvent() {
        // Given
        DeliveryWebhookEvent event = DeliveryWebhookEvent.builder()
            .shipmentId("SHP-005")
            .event("INVALID_EVENT")
            .timestamp(LocalDateTime.now())
            .build();

        Order order = Order.builder().id(5L).build();
        Shipment shipment = Shipment.builder()
            .id(5L)
            .order(order)
            .shipmentId("SHP-005")
            .status(ShipmentStatus.PROCESSING)
            .build();

        when(shipmentRepository.findByShipmentId("SHP-005")).thenReturn(
            Optional.of(shipment)
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> shipmentService.handleDeliveryWebhook(event)
        );

        assertTrue(exception.getMessage().contains("Unknown delivery event"));
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void handleDeliveryWebhook_DoesNotSendEmail_ForShipmentCreatedEvent() {
        // Given
        DeliveryWebhookEvent event = DeliveryWebhookEvent.builder()
            .shipmentId("SHP-006")
            .event("SHIPMENT_CREATED")
            .timestamp(LocalDateTime.now())
            .build();

        Order order = Order.builder()
            .id(6L)
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .status(OrderStatus.PROCESSING)
            .build();

        Shipment shipment = Shipment.builder()
            .id(6L)
            .order(order)
            .shipmentId("SHP-006")
            .status(ShipmentStatus.PROCESSING)
            .build();

        when(shipmentRepository.findByShipmentId("SHP-006")).thenReturn(
            Optional.of(shipment)
        );
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(i ->
            i.getArgument(0)
        );

        // When
        shipmentService.handleDeliveryWebhook(event);

        // Then
        assertEquals(ShipmentStatus.SHIPMENT_CREATED, shipment.getStatus());
        verify(shipmentRepository).save(shipment);
        // No email should be sent for SHIPMENT_CREATED event
        verify(eventPublisher, never()).publishEmailEvent(any());
    }

    @Test
    void handleDeliveryWebhook_DoesNotPublishInventoryRollback_ForNonLostStatuses() {
        // Given
        DeliveryWebhookEvent event = DeliveryWebhookEvent.builder()
            .shipmentId("SHP-007")
            .event("DELIVERED")
            .timestamp(LocalDateTime.now())
            .build();

        Product product = Product.builder()
            .id(200L)
            .name("Test Product 2")
            .build();

        Order order = Order.builder()
            .id(7L)
            .email("test2@example.com")
            .firstName("Test")
            .lastName("User2")
            .status(OrderStatus.DELIVERING)
            .product(product)
            .quantity(3)
            .build();

        Shipment shipment = Shipment.builder()
            .id(7L)
            .order(order)
            .shipmentId("SHP-007")
            .status(ShipmentStatus.IN_TRANSIT)
            .trackingNumber("TRACK-007")
            .estimatedDelivery(LocalDateTime.now().plusDays(1))
            .build();

        when(shipmentRepository.findByShipmentId("SHP-007")).thenReturn(
            Optional.of(shipment)
        );
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(i ->
            i.getArgument(0)
        );
        when(orderRepository.save(any(Order.class))).thenAnswer(i ->
            i.getArgument(0)
        );

        // When
        shipmentService.handleDeliveryWebhook(event);

        // Then
        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
        assertEquals(OrderStatus.DELIVERED, order.getStatus());

        // Verify inventory rollback is NOT published for DELIVERED status
        verify(eventPublisher, never()).publishInventoryRollbackEvent(any());

        // Verify email is still sent
        verify(eventPublisher).publishEmailEvent(
            argThat(
                emailEvent ->
                    emailEvent.getType().equals("DELIVERY_UPDATE") &&
                    emailEvent.getParams().get("status").equals("DELIVERED")
            )
        );
    }
}
