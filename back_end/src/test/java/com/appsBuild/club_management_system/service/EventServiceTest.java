package com.appsBuild.club_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.appsBuild.club_management_system.dto.event.EventPageResponse;
import com.appsBuild.club_management_system.dto.event.EventRequest;
import com.appsBuild.club_management_system.dto.event.EventResponse;
import com.appsBuild.club_management_system.dto.event.ReservationRequest;
import com.appsBuild.club_management_system.dto.event.ReservationResponse;
import com.appsBuild.club_management_system.exception.ApplicationException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Club;
import com.appsBuild.club_management_system.model.entity.Event;
import com.appsBuild.club_management_system.model.entity.Location;
import com.appsBuild.club_management_system.model.entity.LocationReservation;
import com.appsBuild.club_management_system.model.enums.DemandStatus;
import com.appsBuild.club_management_system.model.enums.LocationType;
import com.appsBuild.club_management_system.model.enums.VisibilityLevel;
import com.appsBuild.club_management_system.repository.ClubRepository;
import com.appsBuild.club_management_system.repository.EventRepository;
import com.appsBuild.club_management_system.repository.LocationRepository;
import com.appsBuild.club_management_system.repository.LocationReservationRepository;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

	@Mock
	private EventRepository eventRepository;
	@Mock
	private ClubRepository clubRepository;
	@Mock
	private LocationRepository locationRepository;
	@Mock
	private LocationReservationRepository locationReservationRepository;

	@InjectMocks
	private EventService eventService;

	private Club club;
	private Location location;

	@BeforeEach
	void setUp() {
		club = Club.builder().clubId(1L).clubName("Robotics Club").build();
		location = Location.builder()
				.locationId(5L)
				.label("Main Hall")
				.type(LocationType.INTERNAL)
				.build();
	}

	private EventRequest request() {
		EventRequest request = new EventRequest();
		request.setTitle("Workshop");
		request.setDescription("Intro to robotics");
		request.setVisibility(VisibilityLevel.PUBLIC);
		request.setStartDate(LocalDate.of(2026, 11, 10));
		request.setEndDate(LocalDate.of(2026, 11, 11));
		return request;
	}

	private Event event(Long eventId) {
		return Event.builder()
				.eventId(eventId)
				.title("Workshop")
				.visibility(VisibilityLevel.PUBLIC)
				.club(club)
				.locationReservations(new ArrayList<>())
				.build();
	}

	// ── create ──────────────────────────────────────────────────────────

	@Test
	void createEvent_mapsFieldsAndReservations() {
		when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
		when(locationRepository.findById(5L)).thenReturn(Optional.of(location));
		when(eventRepository.save(any(Event.class)))
				.thenAnswer(
						invocation -> {
							Event saved = invocation.getArgument(0);
							saved.setEventId(100L);
							return saved;
						});

		EventRequest request = request();
		request.setReservations(
				List.of(
						new ReservationRequest(
								5L, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10))));

		EventResponse response = eventService.createEvent(1L, request);

		assertEquals(100L, response.eventId());
		assertEquals("Workshop", response.title());
		assertEquals(VisibilityLevel.PUBLIC, response.visibility());
		assertEquals(1L, response.club().id());
		assertEquals("Robotics Club", response.club().name());
		assertEquals(1, response.reservations().size());

		ReservationResponse reservation = response.reservations().get(0);
		assertEquals(DemandStatus.PENDING, reservation.status());
		assertEquals(5L, reservation.location().id());
		assertEquals("Main Hall", reservation.location().name());

		verify(eventRepository).save(any(Event.class));
	}

	@Test
	void createEvent_endBeforeStart_throws() {
		when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

		EventRequest request = request();
		request.setEndDate(LocalDate.of(2026, 11, 9));

		assertThrows(ApplicationException.class, () -> eventService.createEvent(1L, request));
		verify(eventRepository, never()).save(any());
	}

	@Test
	void createEvent_clubNotFound_throws() {
		when(clubRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> eventService.createEvent(99L, request()));
	}

	@Test
	void createEvent_locationNotFound_throws() {
		when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
		when(locationRepository.findById(5L)).thenReturn(Optional.empty());

		EventRequest request = request();
		request.setReservations(
				List.of(
						new ReservationRequest(
								5L, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10))));

		assertThrows(NotFoundException.class, () -> eventService.createEvent(1L, request));
	}

	// ── read ────────────────────────────────────────────────────────────

	@Test
	void getEvent_notFound_throws() {
		when(eventRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> eventService.getEvent(99L));
	}

	@Test
	void listEvents_mapsPageAndPagination() {
		Page<Event> page = new PageImpl<>(List.of(event(100L)), PageRequest.of(0, 20), 1);
		when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(page);

		EventPageResponse response = eventService.listEvents(1, 20, null, null, null);

		assertEquals(1, response.events().size());
		assertEquals(100L, response.events().get(0).eventId());
		assertEquals(1, response.pagination().currentPage());
		assertEquals(1, response.pagination().totalPages());
		assertEquals(1, response.pagination().totalItems());
	}

	// ── update ──────────────────────────────────────────────────────────

	@Test
	void updateEvent_replacesNonApprovedReservations_keepsApproved() {
		LocationReservation approved = LocationReservation.builder()
				.locationReservationId(50L)
				.startDate(new Date())
				.endDate(new Date())
				.status(DemandStatus.APPROVED)
				.location(location)
				.build();
		LocationReservation pending = LocationReservation.builder()
				.locationReservationId(51L)
				.startDate(new Date())
				.endDate(new Date())
				.status(DemandStatus.PENDING)
				.location(location)
				.build();

		List<LocationReservation> reservations = new ArrayList<>(List.of(approved, pending));
		Event event = event(100L);
		event.setLocationReservations(reservations);
		approved.setEvent(event);
		pending.setEvent(event);

		when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
		when(locationRepository.findById(5L)).thenReturn(Optional.of(location));
		when(eventRepository.save(event)).thenReturn(event);

		EventRequest request = request();
		request.setReservations(
				List.of(
						new ReservationRequest(5L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 1))));

		EventResponse response = eventService.updateEvent(1L, 100L, request);

		assertEquals("Workshop", response.title());
		assertEquals(2, response.reservations().size());
		verify(locationReservationRepository).delete(pending);
		verify(locationReservationRepository, never()).delete(approved);
		verify(eventRepository).save(event);
	}

	@Test
	void updateEvent_eventBelongsToDifferentClub_throws() {
		Club otherClub = Club.builder().clubId(2L).clubName("Other Club").build();
		Event event = event(100L);
		event.setClub(otherClub);
		when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

		assertThrows(NotFoundException.class, () -> eventService.updateEvent(1L, 100L, request()));
	}

	// ── delete ──────────────────────────────────────────────────────────

	@Test
	void deleteEvent_success() {
		Event event = event(100L);
		when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

		eventService.deleteEvent(1L, 100L);

		verify(eventRepository).delete(event);
	}

	@Test
	void deleteEvent_eventBelongsToDifferentClub_throws() {
		Club otherClub = Club.builder().clubId(2L).clubName("Other Club").build();
		Event event = event(100L);
		event.setClub(otherClub);
		when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

		assertThrows(NotFoundException.class, () -> eventService.deleteEvent(1L, 100L));
		verify(eventRepository, never()).delete(any(Event.class));
	}
}
