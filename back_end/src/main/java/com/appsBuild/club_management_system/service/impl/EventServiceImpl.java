package com.appsBuild.club_management_system.service.impl;

import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Event;
import com.appsBuild.club_management_system.repository.EventRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl {
  private final EventRepository eventRepository;

  public Event save(Event event) {
    if (event == null) {
      throw new NoContentException("Event cannot be null");
    }
    return eventRepository.save(event);
  }

  public Event findById(Long id) {
    return eventRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));
  }

  public Optional<Event> findByIdOptional(Long id) {
    return eventRepository.findById(id);
  }

  public List<Event> findAll() {
    List<Event> events = eventRepository.findAll();
    if (events.isEmpty()) {
      throw new NoContentException("No events found");
    }
    return events;
  }

  public void delete(Long id) {
    Event event = findById(id);
    eventRepository.delete(event);
  }
}
