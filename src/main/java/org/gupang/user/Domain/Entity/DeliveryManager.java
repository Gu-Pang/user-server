package org.gupang.user.Domain.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.gupang.common.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "p_delivery_manager", indexes = {
        @Index(name = "idx_delivery_manager_hub_type_seq", columnList = "hubId, deliveryType, sequence")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE p_delivery_manager SET deleted_at = CURRENT_TIMESTAMP WHERE user_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class DeliveryManager extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private UUID hubId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeliveryType deliveryType = DeliveryType.HUB;

    @Column(nullable = false)
    private Integer sequence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.AVAILABLE;

}
