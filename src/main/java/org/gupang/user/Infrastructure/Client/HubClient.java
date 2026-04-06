package org.gupang.user.Infrastructure.Client;

import org.gupang.user.Application.Dto.getHubResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-server")
public interface HubClient {
    @GetMapping("/api/v1/hubs/{hubId}")
    getHubResponseDto getHub(@PathVariable("hubId") UUID hubId);
}
