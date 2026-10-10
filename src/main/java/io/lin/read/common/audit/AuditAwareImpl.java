package io.lin.read.common.audit;

import io.lin.read.common.security.CurrentUserContext;
import io.lin.read.security.AuthenticatedUser;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        AuthenticatedUser user = CurrentUserContext.get();
        return user == null ? Optional.empty() : Optional.of(user.id());
    }
}
