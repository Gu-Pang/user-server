package org.gupang.user.Infrastructure.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

// feignclient로 keycloak과 통신
@FeignClient(name = "keycloakClient", url = "${spring.security.oauth2.client.provider.keycloak.token-uri}")
public interface KeycloakClient {
    // form형태로 보내야함
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> getToken(@RequestBody MultiValueMap<String, String> formData);
}
