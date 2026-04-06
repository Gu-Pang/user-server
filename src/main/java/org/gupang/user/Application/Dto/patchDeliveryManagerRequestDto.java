package org.gupang.user.Application.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class patchDeliveryManagerRequestDto {
    private String deliveryType;
    private Integer sequence;
    private String status;
}
