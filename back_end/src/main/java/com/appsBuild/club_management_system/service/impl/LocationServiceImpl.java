package com.appsBuild.club_management_system.service.impl;

import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.Location;
import com.appsBuild.club_management_system.repository.LocationRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl {
  private final LocationRepository locationRepository;

  public Location save(Location location) {
    if (location == null) {
      throw new NoContentException("Location cannot be null");
    }
    return locationRepository.save(location);
  }

  public Location findById(Long id) {
    return locationRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Location not found with id: " + id));
  }

  public Optional<Location> findByIdOptional(Long id) {
    return locationRepository.findById(id);
  }

  public List<Location> findAll() {
    List<Location> locations = locationRepository.findAll();
    if (locations.isEmpty()) {
      throw new NoContentException("No locations found");
    }
    return locations;
  }

  public void delete(Long id) {
    Location location = findById(id);
    locationRepository.delete(location);
  }
}
