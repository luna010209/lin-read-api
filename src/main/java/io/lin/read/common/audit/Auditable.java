package io.lin.read.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class Auditable extends UserAuditable {

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    public void markDeleted() {
        this.deleted = true;
    }
}
