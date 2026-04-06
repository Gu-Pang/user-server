package org.gupang.user.Application.Dto;

import lombok.*;
import org.gupang.user.Domain.Entity.DeliveryManager;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class getDeliveryManagerResponseDto {
    private UUID userId;
    private UUID hubId;
    private String deliveryType;
    private Integer sequence;
    private String status;

    public static getDeliveryManagerResponseDto from(DeliveryManager entity, UUID hubId) {
        return getDeliveryManagerResponseDto.builder()
                .userId(entity.getUserId())
                .hubId(hubId)
                .deliveryType(entity.getDeliveryType().name())
                .sequence(entity.getSequence())
                .status(entity.getStatus().name())
                .build();
    }
}
