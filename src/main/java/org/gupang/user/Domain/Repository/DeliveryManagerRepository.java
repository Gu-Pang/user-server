package org.gupang.user.Domain.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.gupang.user.Domain.Entity.DeliveryManager;
import org.gupang.user.Domain.Entity.DeliveryStatus;
import org.gupang.user.Domain.Entity.DeliveryType;

public interface DeliveryManagerRepository {
    Optional<DeliveryManager> findByUserId(UUID userId);

    List<DeliveryManager> search(UUID hubId, DeliveryType type, Integer sequence, DeliveryStatus status);

    List<DeliveryManager> findAll();

    DeliveryManager save(DeliveryManager deliveryManager);

    void delete(DeliveryManager deliveryManager);

    Integer findMaxSequenceByDeliveryType(DeliveryType deliveryType);
}
