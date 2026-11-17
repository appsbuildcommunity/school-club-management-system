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
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.EventRepository;
import com.appsBuild.club_management_system.repository.LocationRepository;
import com.appsBuild.club_management_system.repository.LocationReservationRepository;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.ZoneId;
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

	private final EventRepository eventRepository;
	private final ClubRepository clubRepository;
	private final LocationRepository locationRepository;
	private final LocationReservationRepository locationReservationRepository;

	// ── Read ────────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public EventPageResponse listEvents(
			int page, int limit, String clubName, LocalDate startDate, LocalDate endDate) {
		int pageNumber = Math.max(page, 1);
		int pageSize = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

		Specification<Event> specification = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (clubName != null && !clubName.isBlank()) {
				predicates.add(
						cb.like(
								cb.lower(root.get("club").get("clubName")),
								"%" + clubName.toLowerCase(Locale.ROOT) + "%"));
			}
			if (startDate != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), toDate(startDate)));
			}
			if (endDate != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), toDate(endDate)));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};

		Page<Event> result = eventRepository.findAll(
				specification,
				PageRequest.of(
						pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "startDate")));

		List<EventResponse> events = result.getContent().stream().map(this::toResponse).toList();
		Pagination pagination = new Pagination(result.getNumber() + 1, result.getTotalPages(), result.getTotalElements());
		return new EventPageResponse(events, pagination);
	}

	@Transactional(readOnly = true)
	public EventResponse getEvent(Long eventId) {
		return toResponse(resolveEvent(eventId));
	}

	// ── Write ───────────────────────────────────────────────────────────

	@Transactional
	public EventResponse createEvent(Long clubId, EventRequest request) {
		Club club = resolveClub(clubId);
		validateDates(request.getStartDate(), request.getEndDate(), "event");

		Event event = Event.builder()
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
		for (ReservationRequest request : requests) {
			validateDates(request.startDate(), request.endDate(), "reservation");
			Date startDate = toDate(request.startDate());
			Date endDate = toDate(request.endDate());
			if (!seen.add(reservationKey(request.locationId(), startDate, endDate))) {
				continue;
			}
			Location location = locationRepository
					.findById(request.locationId())
					.orElseThrow(
							() -> new NotFoundException("Location not found: " + request.locationId()));
			event
					.getLocationReservations()
					.add(
							LocationReservation.builder()
									.startDate(startDate)
									.endDate(endDate)
									.status(DemandStatus.PENDING)
									.event(event)
									.location(location)
									.build());
		}
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
				event.getStartDate(),
				event.getEndDate(),
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
		return event.getAttachments().stream()
				.map(this::toAttachmentResponse)
				.toList();
	}

	private List<CommentResponse> commentsOf(Event event) {
		if (event.getComments() == null) {
			return List.of();
		}
		return event.getComments().stream()
				.map(
						comment -> new CommentResponse(
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
						reservation -> new ReservationResponse(
								reservation.getLocationReservationId(),
								reservation.getStartDate(),
								reservation.getEndDate(),
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
		return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
}
