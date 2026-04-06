package org.gupang.user.Infrastructure.Repository;

import jakarta.persistence.criteria.Predicate;
import org.gupang.user.Domain.Entity.DeliveryManager;
import org.gupang.user.Domain.Entity.DeliveryStatus;
import org.gupang.user.Domain.Entity.DeliveryType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeliveryManagerSpecification {

    public static Specification<DeliveryManager> filterBy(UUID hubId, DeliveryType type, Integer sequence,
            DeliveryStatus status) {

        // root: 검색 대상 테이블 정보
        // query: 쿼리 구문 정보 (정렬, distinct 등)
        // cb: 조건을 생성하는 연산자 도구 (CriteriaBuilder)
        return (root, query, cb) -> {
            // 실제 SQL에 들어갈 WHERE 조건들을 담을 바구니
            List<Predicate> predicates = new ArrayList<>();

            if (hubId != null) {
                predicates.add(cb.equal(root.get("hubId"), hubId));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("deliveryType"), type));
            }

            if (sequence != null) {
                predicates.add(cb.equal(root.get("sequence"), sequence));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Ex : ([hubId = 'A']) AND ([type = 'HUB']) AND ([sequence = 1]) AND ([status =
            // 'AVAILABLE'])
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
