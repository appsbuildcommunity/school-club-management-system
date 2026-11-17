package com.appsBuild.club_management_system.service;

import com.appsBuild.club_management_system.dto.common.AttachmentResponse;
import com.appsBuild.club_management_system.dto.common.ClubReference;
import com.appsBuild.club_management_system.dto.common.CommentResponse;
import com.appsBuild.club_management_system.dto.common.ImageReference;
import com.appsBuild.club_management_system.dto.common.LocationResponse;
import com.appsBuild.club_management_system.dto.common.Pagination;
import com.appsBuild.club_management_system.dto.common.UserReference;
import com.appsBuild.club_management_system.dto.event.EventPageResponse;
import com.appsBuild.club_management_system.dto.event.EventRequest;
import com.appsBuild.club_management_system.dto.event.EventResponse;
import com.appsBuild.club_management_system.dto.event.ReservationRequest;
import com.appsBuild.club_management_system.dto.event.ReservationResponse;
import com.appsBuild.club_management_system.exception.ApplicationException;
import com.appsBuild.club_management_system.exception.impl.ConflictException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Attachment;
import com.appsBuild.club_management_system.model.entity.Club;
import com.appsBuild.club_management_system.model.entity.Comment;
import com.appsBuild.club_management_system.model.entity.Event;
import com.appsBuild.club_management_system.model.entity.Location;
import com.appsBuild.club_management_system.model.entity.LocationReservation;
import com.appsBuild.club_management_system.model.entity.ProfilePicture;
import com.appsBuild.club_management_system.model.entity.User;
import com.appsBuild.club_management_system.model.enums.DemandStatus;
import com.appsBuild.club_management_system.model.enums.LocationType;
import com.appsBuild.club_management_system.model.enums.VisibilityLevel;
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.EventRepository;
import com.appsBuild.club_management_system.repository.LocationRepository;
import com.appsBuild.club_management_system.repository.LocationReservationRepository;
import com.appsBuild.club_management_system.security.ViewerContext;
import com.appsBuild.club_management_system.security.ViewerContextResolver;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class EventService {

  private static final int DEFAULT_LIMIT = 20;
  private static final int MAX_LIMIT = 100;
  private static final long NEW_EVENT_ID = -1L;
  private static final List<DemandStatus> ACTIVE_RESERVATION_STATUSES =
      List.of(DemandStatus.PENDING, DemandStatus.APPROVED);

  private final EventRepository eventRepository;
  private final ClubRepository clubRepository;
  private final LocationRepository locationRepository;
  private final LocationReservationRepository locationReservationRepository;
  private final ViewerContextResolver viewerContextResolver;

  // ── Read ────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public EventPageResponse listEvents(
      int page, int limit, String clubName, LocalDate startDate, LocalDate endDate) {
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new ApplicationException("start_date must not be after end_date");
    }

    int pageNumber = Math.max(page, 1);
    int pageSize = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
    ViewerContext viewer = viewerContextResolver.resolve();

    Specification<Event> specification =
        visibleTo(viewer)
            .and(
                (root, query, cb) -> {
                  List<Predicate> predicates = new ArrayList<>();
                  if (clubName != null && !clubName.isBlank()) {
                    predicates.add(
                        cb.like(
                            cb.lower(root.get("club").get("clubName")),
                            "%" + clubName.toLowerCase(Locale.ROOT) + "%"));
                  }
                  // Overlap semantics: keep events whose range intersects [startDate, endDate].
                  if (startDate != null) {
                    predicates.add(
                        cb.greaterThanOrEqualTo(root.get("endDate"), toDate(startDate)));
                  }
                  if (endDate != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), toDate(endDate)));
                  }
                  return cb.and(predicates.toArray(new Predicate[0]));
                });

    Page<Event> result =
        eventRepository.findAll(
            specification,
            PageRequest.of(
                pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "startDate")));

    List<EventResponse> events = result.getContent().stream().map(this::toResponse).toList();
    Pagination pagination =
        new Pagination(result.getNumber() + 1, result.getTotalPages(), result.getTotalElements());
    return new EventPageResponse(events, pagination);
  }

  @Transactional(readOnly = true)
  public EventResponse getEvent(Long eventId) {
    Event event = resolveEvent(eventId);
    if (!isVisible(event, viewerContextResolver.resolve())) {
      // Do not leak the existence of events the caller is not allowed to see.
      throw new NotFoundException("Event not found: " + eventId);
    }
    return toResponse(event);
  }

  // ── Write ───────────────────────────────────────────────────────────

  @Transactional
  public EventResponse createEvent(Long clubId, EventRequest request) {
    Club club = resolveClub(clubId);
    validateDates(request.getStartDate(), request.getEndDate(), "event");

    Event event =
        Event.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .visibility(request.getVisibility())
            .startDate(toDate(request.getStartDate()))
            .endDate(toDate(request.getEndDate()))
            .club(club)
            .locationReservations(new ArrayList<>())
            .build();

    addReservations(event, request.getReservations(), Set.of());
    return toResponse(eventRepository.save(event));
  }

  @Transactional
  public EventResponse updateEvent(Long clubId, Long eventId, EventRequest request) {
    Event event = resolveEventInClub(clubId, eventId);
    validateDates(request.getStartDate(), request.getEndDate(), "event");

    event.setTitle(request.getTitle());
    event.setDescription(request.getDescription());
    event.setVisibility(request.getVisibility());
    event.setStartDate(toDate(request.getStartDate()));
    event.setEndDate(toDate(request.getEndDate()));

    replaceReservations(event, request.getReservations());
    return toResponse(eventRepository.save(event));
  }

  @Transactional
  public void deleteEvent(Long clubId, Long eventId) {
    eventRepository.delete(resolveEventInClub(clubId, eventId));
  }

  // ── Reservations ────────────────────────────────────────────────────

  private void replaceReservations(Event event, List<ReservationRequest> requests) {
    List<LocationReservation> current = event.getLocationReservations();
    if (current == null) {
      current = new ArrayList<>();
      event.setLocationReservations(current);
    }

    Set<String> keptKeys = new HashSet<>();
    Iterator<LocationReservation> iterator = current.iterator();
    while (iterator.hasNext()) {
      LocationReservation reservation = iterator.next();
      if (reservation.getStatus() == DemandStatus.APPROVED) {
        keptKeys.add(reservationKey(reservation));
      } else {
        iterator.remove();
        locationReservationRepository.delete(reservation);
      }
    }

    addReservations(event, requests, keptKeys);
  }

  private void addReservations(
      Event event, List<ReservationRequest> requests, Set<String> existingKeys) {
    if (requests == null || requests.isEmpty()) {
      return;
    }

    Set<String> seen = new HashSet<>(existingKeys);
    List<LocationReservation> kept =
        event.getLocationReservations() == null
            ? List.of()
            : List.copyOf(event.getLocationReservations());
    List<LocationReservation> added = new ArrayList<>();
    long eventId = event.getEventId() == null ? NEW_EVENT_ID : event.getEventId();

    for (ReservationRequest request : requests) {
      validateDates(request.startDate(), request.endDate(), "reservation");
      Date startDate = toDate(request.startDate());
      Date endDate = toDate(request.endDate());

      if (!seen.add(reservationKey(request.locationId(), startDate, endDate))) {
        continue;
      }
      if (overlapsAny(kept, request.locationId(), startDate, endDate)) {
        // Already covered by an approved reservation of this event; keep it unchanged.
        continue;
      }
      if (overlapsAny(added, request.locationId(), startDate, endDate)) {
        throw new ConflictException(
            "Duplicate reservation request for location " + request.locationId());
      }
      if (locationReservationRepository.countOverlappingReservations(
              request.locationId(), ACTIVE_RESERVATION_STATUSES, startDate, endDate, eventId)
          > 0) {
        throw new ConflictException(
            "Location " + request.locationId() + " is already reserved for the selected dates");
      }

      Location location =
          locationRepository
              .findById(request.locationId())
              .orElseThrow(
                  () -> new NotFoundException("Location not found: " + request.locationId()));

      LocationReservation reservation =
          LocationReservation.builder()
              .startDate(startDate)
              .endDate(endDate)
              .status(DemandStatus.PENDING)
              .event(event)
              .location(location)
              .build();
      added.add(reservation);
      event.getLocationReservations().add(reservation);
    }
  }

  private boolean overlapsAny(
      List<LocationReservation> reservations,
      Long locationId,
      Date startDate,
      Date endDate) {
    return reservations.stream()
        .anyMatch(
            reservation ->
                reservation.getLocation() != null
                    && locationId.equals(reservation.getLocation().getLocationId())
                    && !reservation.getEndDate().before(startDate)
                    && !reservation.getStartDate().after(endDate));
  }

  private String reservationKey(LocationReservation reservation) {
    return reservationKey(
        reservation.getLocation().getLocationId(),
        reservation.getStartDate(),
        reservation.getEndDate());
  }

  private String reservationKey(Long locationId, Date startDate, Date endDate) {
    return locationId + "|" + startDate.getTime() + "|" + endDate.getTime();
  }

  // ── Visibility ──────────────────────────────────────────────────────

  private Specification<Event> visibleTo(ViewerContext viewer) {
    return (root, query, cb) -> {
      if (viewer.admin()) {
        return cb.conjunction();
      }

      List<Predicate> allowed = new ArrayList<>();
      allowed.add(cb.equal(root.get("visibility"), VisibilityLevel.PUBLIC));
      if (viewer.student()) {
        allowed.add(cb.equal(root.get("visibility"), VisibilityLevel.STUDENTS_ONLY));
      }
      if (!viewer.clubIds().isEmpty()) {
        allowed.add(
            cb.and(
                cb.equal(root.get("visibility"), VisibilityLevel.MEMBERS_ONLY),
                root.get("club").get("clubId").in(viewer.clubIds())));
      }
      return cb.or(allowed.toArray(new Predicate[0]));
    };
  }

  private boolean isVisible(Event event, ViewerContext viewer) {
    if (viewer.admin()) {
      return true;
    }
    VisibilityLevel visibility = event.getVisibility();
    if (visibility == null) {
      return false;
    }
    return switch (visibility) {
      case PUBLIC -> true;
      case STUDENTS_ONLY -> viewer.student();
      case MEMBERS_ONLY -> viewer.clubIds().contains(event.getClub().getClubId());
      case HIDDEN -> false;
    };
  }

  // ── Mapping ─────────────────────────────────────────────────────────

  private EventResponse toResponse(Event event) {
    Club club = event.getClub();
    return new EventResponse(
        event.getEventId(),
        event.getTitle(),
        event.getDescription(),
        club == null
            ? null
            : new ClubReference(
                club.getClubId(),
                club.getClubName(),
                club.getDescription(),
                profilePictureOf(club.getProfilePicture())),
        event.getVisibility(),
        toLocalDate(event.getStartDate()),
        toLocalDate(event.getEndDate()),
        locationsOf(event),
        attachmentsOf(event),
        commentsOf(event),
        reservationsOf(event));
  }

  private List<LocationResponse> locationsOf(Event event) {
    if (event.getLocationReservations() == null) {
      return List.of();
    }
    Map<Long, LocationResponse> locations = new LinkedHashMap<>();
    for (LocationReservation reservation : event.getLocationReservations()) {
      Location location = reservation.getLocation();
      if (location != null) {
        locations.putIfAbsent(location.getLocationId(), toLocationResponse(location));
      }
    }
    return List.copyOf(locations.values());
  }

  private List<AttachmentResponse> attachmentsOf(Event event) {
    if (event.getAttachments() == null) {
      return List.of();
    }
    return event.getAttachments().stream().map(this::toAttachmentResponse).toList();
  }

  private List<CommentResponse> commentsOf(Event event) {
    if (event.getComments() == null) {
      return List.of();
    }
    return event.getComments().stream()
        .map(
            comment ->
                new CommentResponse(
                    comment.getCommentId(),
                    comment.getContent(),
                    toUserReference(comment.getUser()),
                    comment.getCreatedAt()))
        .toList();
  }

  private List<ReservationResponse> reservationsOf(Event event) {
    if (event.getLocationReservations() == null) {
      return List.of();
    }
    return event.getLocationReservations().stream()
        .map(
            reservation ->
                new ReservationResponse(
                    reservation.getLocationReservationId(),
                    toLocalDate(reservation.getStartDate()),
                    toLocalDate(reservation.getEndDate()),
                    reservation.getStatus(),
                    reservation.getLocation() == null
                        ? null
                        : toLocationResponse(reservation.getLocation())))
        .toList();
  }

  private LocationResponse toLocationResponse(Location location) {
    return new LocationResponse(
        location.getLocationId(),
        location.getLabel(),
        location.getType() == LocationType.INTERNAL);
  }

  private AttachmentResponse toAttachmentResponse(Attachment attachment) {
    return new AttachmentResponse(
        attachment.getAttachmentId(), attachment.getType(), attachment.getS3Key());
  }

  private UserReference toUserReference(User user) {
    if (user == null) {
      return null;
    }
    return new UserReference(
        user.getUserId(),
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        profilePictureOf(user.getProfilePicture()));
  }

  private ImageReference profilePictureOf(ProfilePicture profilePicture) {
    return profilePicture == null ? null : new ImageReference(profilePicture.getS3Key());
  }

  // ── Helpers ─────────────────────────────────────────────────────────

  private Event resolveEvent(Long eventId) {
    return eventRepository
        .findById(eventId)
        .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
  }

  private Event resolveEventInClub(Long clubId, Long eventId) {
    Event event = resolveEvent(eventId);
    if (!event.getClub().getClubId().equals(clubId)) {
      throw new NotFoundException("Event not found: " + eventId);
    }
    return event;
  }

  private Club resolveClub(Long clubId) {
    return clubRepository
        .findById(clubId)
        .orElseThrow(() -> new NotFoundException("Club not found: " + clubId));
  }

  private void validateDates(LocalDate startDate, LocalDate endDate, String owner) {
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new ApplicationException(owner + " endDate must not be before startDate");
    }
  }

  private static Date toDate(LocalDate date) {
    return date == null ? null : Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
  }

  private static LocalDate toLocalDate(Date date) {
    return date == null ? null : date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
  }
}
