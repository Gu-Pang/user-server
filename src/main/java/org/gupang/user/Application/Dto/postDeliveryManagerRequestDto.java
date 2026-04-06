package org.gupang.user.Application.Dto;

import org.gupang.user.Domain.Entity.DeliveryManager;
import org.gupang.user.Domain.Entity.DeliveryStatus;
import org.gupang.user.Domain.Entity.DeliveryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class postDeliveryManagerRequestDto {
    private DeliveryType deliveryType;

    public DeliveryManager toEntity(UUID userId, UUID hubId, int sequence, DeliveryStatus status) {
        return DeliveryManager.builder()
                .userId(userId)
                .hubId(hubId)
                .deliveryType(this.deliveryType != null ? this.deliveryType : DeliveryType.HUB)
                .sequence(sequence)
                .status(status)
                .build();
    }
}
