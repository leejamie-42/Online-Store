package com.comp5348.store.dto.event;

import java.time.LocalDateTime;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundEvent {

    /**
     * Order ID for which payment should be refund.
     */
    private Long orderId;

    private String reason;

    /**
     * Unique event identifier for tracking.
     */
    private String eventId;

    /**
     * Event timestamp.
     */
    private LocalDateTime timestamp;

    private Long userId;
}
