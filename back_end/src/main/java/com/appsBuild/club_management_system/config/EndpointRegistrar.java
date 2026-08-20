package com.appsBuild.club_management_system.config;

import com.appsBuild.club_management_system.annotation.GrantableEndpoint;
import com.appsBuild.club_management_system.model.entity.Endpoint;
import com.appsBuild.club_management_system.repository.EndpointRepository;
import java.lang.reflect.Method;
import java.util.Optional;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registers every {@code @GrantableEndpoint}-annotated method as an {@link Endpoint} row.
 *
 * <p>Upsert-only: existing rows are refreshed from the annotation, but rows whose
 * annotation disappeared from the code are left untouched (no pruning).
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
  public void run(ApplicationArguments args) {
    for (Object bean : context.getBeansWithAnnotation(RestController.class).values()) {
      Class<?> targetClass = AopUtils.getTargetClass(bean);
      for (Method method : targetClass.getDeclaredMethods()) {
        GrantableEndpoint annotation = method.getAnnotation(GrantableEndpoint.class);
        if (annotation == null) {
          continue;
        }
        upsert(annotation);
      }
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
