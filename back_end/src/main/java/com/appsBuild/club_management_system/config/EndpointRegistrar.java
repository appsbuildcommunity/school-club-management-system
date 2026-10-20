package com.appsBuild.club_management_system.config;

import com.appsBuild.club_management_system.annotation.GrantableEndpoint;
import com.appsBuild.club_management_system.model.entity.Endpoint;
import com.appsBuild.club_management_system.repository.EndpointRepository;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * Keeps the {@link Endpoint} table in sync with the code: registers every
 * {@code @GrantableEndpoint}-annotated method as an {@link Endpoint} row (upsert), then prunes
 * rows whose annotation disappeared from the code.
 *
 * <p>Pruned rows are removed together with their references — profile-template join rows
 * ({@code club_profile_endpoints}) and member grants ({@code club_membership_endpoint}) are
 * cleaned up by DB-level {@code ON DELETE CASCADE} constraints.
 */
@Component
public class EndpointRegistrar implements ApplicationRunner {

  private final EndpointRepository endpointRepository;
  private final ApplicationContext context;

  public EndpointRegistrar(EndpointRepository endpointRepository, ApplicationContext context) {
    this.endpointRepository = endpointRepository;
    this.context = context;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    Set<String> seen = new HashSet<>();
    for (Object bean : context.getBeansWithAnnotation(RestController.class).values()) {
      Class<?> targetClass = AopUtils.getTargetClass(bean);
      for (Method method : targetClass.getDeclaredMethods()) {
        GrantableEndpoint annotation = method.getAnnotation(GrantableEndpoint.class);
        if (annotation == null) {
          continue;
        }
        seen.add(annotation.name());
        upsert(annotation);
      }
    }
    prune(seen);
  }

  /** Removes endpoint rows that no longer correspond to any {@code @GrantableEndpoint}. */
  private void prune(Set<String> seen) {
    List<Endpoint> orphans =
        endpointRepository.findAll().stream()
            .filter(endpoint -> !seen.contains(endpoint.getName()))
            .toList();
    if (!orphans.isEmpty()) {
      endpointRepository.deleteAll(orphans);
    }
  }

  private void upsert(GrantableEndpoint annotation) {
    Optional<Endpoint> existing = endpointRepository.findByName(annotation.name());
    if (existing.isPresent()) {
      Endpoint endpoint = existing.get();
      boolean changed =
          !endpoint.getDescription().equals(annotation.description())
              || endpoint.getCategory() != annotation.category()
              || endpoint.isPrivileged() != annotation.privileged();
      if (changed) {
        endpoint.setDescription(annotation.description());
        endpoint.setCategory(annotation.category());
        endpoint.setPrivileged(annotation.privileged());
        endpointRepository.save(endpoint);
      }
      return;
    }
    endpointRepository.save(
        Endpoint.builder()
            .name(annotation.name())
            .description(annotation.description())
            .category(annotation.category())
            .privileged(annotation.privileged())
            .build());
  }
}
