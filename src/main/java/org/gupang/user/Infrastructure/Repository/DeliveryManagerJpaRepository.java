package org.gupang.user.Infrastructure.Repository;

import org.gupang.user.Domain.Entity.DeliveryManager;
import org.gupang.user.Domain.Entity.DeliveryStatus;
import org.gupang.user.Domain.Entity.DeliveryType;
import org.gupang.user.Domain.Repository.DeliveryManagerRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryManagerJpaRepository extends JpaRepository<DeliveryManager, UUID>, DeliveryManagerRepository,
        JpaSpecificationExecutor<DeliveryManager> {
    // JpaSpecificationExecutor= 동적 쿼리. default = 인터페이스에서 구현
    @Override
    default List<DeliveryManager> search(UUID hubId, DeliveryType type, Integer sequence, DeliveryStatus status) {
        return findAll(DeliveryManagerSpecification.filterBy(hubId, type, sequence, status));
    }

    @Query("SELECT COALESCE(MAX(dm.sequence), 0) FROM DeliveryManager dm WHERE dm.deliveryType = :deliveryType")
    Integer findMaxSequenceByDeliveryType(@Param("deliveryType") DeliveryType deliveryType);
}
