package org.gupang.user.Domain.Entity;

import org.gupang.common.entity.UserRole;

public enum UserStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public static UserStatus fromRole(UserRole role) {
        if (role == UserRole.MASTER) {
            return APPROVED;
        }
        return PENDING;
    }
}
