package org.gupang.user.Application.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class getHubResponseDto {
    private UUID hubId;
    private String hubName;
    private String address;
    private String addressDetail;
    private double latitude;
    private double longitude;
}
