package com.appsBuild.club_management_system.service.impl;

import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.LocationReservation;
import com.appsBuild.club_management_system.repository.LocationReservationRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationReservationServiceImpl {
  private final LocationReservationRepository locationReservationRepository;

  public LocationReservation save(LocationReservation locationReservation) {
    if (locationReservation == null) {
      throw new NoContentException("LocationReservation cannot be null");
    }
    return locationReservationRepository.save(locationReservation);
  }

  public LocationReservation findById(Long id) {
    return locationReservationRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("LocationReservation not found with id: " + id));
  }

  public Optional<LocationReservation> findByIdOptional(Long id) {
    return locationReservationRepository.findById(id);
  }

  public List<LocationReservation> findAll() {
    List<LocationReservation> locationReservations = locationReservationRepository.findAll();
    if (locationReservations.isEmpty()) {
      throw new NoContentException("No location reservations found");
    }
    return locationReservations;
  }

  public void delete(Long id) {
    LocationReservation locationReservation = findById(id);
    locationReservationRepository.delete(locationReservation);
  }
}
