package io.lin.read.config.jpa;

import io.lin.read.common.audit.AuditAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(modifyOnCreate = false)
public class JpaConfig {
    @Bean
    public AuditorAware<Long> auditAware() {
        return new AuditAwareImpl();
    }
}
