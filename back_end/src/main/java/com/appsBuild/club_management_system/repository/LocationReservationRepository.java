package com.appsBuild.club_management_system.repository;

import com.appsBuild.club_management_system.model.entity.LocationReservation;
import com.appsBuild.club_management_system.model.enums.DemandStatus;
import java.util.Collection;
import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationReservationRepository extends JpaRepository<LocationReservation, Long> {

  /**
   * Counts active reservations (pending or approved) for the location whose date range overlaps
   * the given one, ignoring reservations that belong to {@code excludedEventId}.
   */
  @Query(
      """
      select count(r)
      from LocationReservation r
      where r.location.locationId = :locationId
        and r.status in :statuses
        and r.startDate <= :endDate
        and r.endDate >= :startDate
        and r.event.eventId <> :excludedEventId
      """)
  long countOverlappingReservations(
      @Param("locationId") Long locationId,
      @Param("statuses") Collection<DemandStatus> statuses,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("excludedEventId") Long excludedEventId);
}
