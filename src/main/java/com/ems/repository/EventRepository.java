package com.ems.repository;

import com.ems.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Upcoming events (today onward), ordered by date then start time
    @Query("SELECT e FROM Event e WHERE e.eventDate >= :today ORDER BY e.eventDate ASC, e.startTime ASC")
    List<Event> findUpcomingEvents(@Param("today") LocalDate today);

    // Paginated search by title and venue
    @Query("SELECT e FROM Event e " +
           "WHERE e.eventDate >= :today " +
           "AND (:title = '' OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:venue = '' OR LOWER(e.venue) LIKE LOWER(CONCAT('%', :venue, '%')))")
    Page<Event> findUpcomingEventsByTitleAndVenue(
            @Param("today") LocalDate today,
            @Param("title") String title,
            @Param("venue") String venue,
            Pageable pageable);

    // Dashboard counts
    @Query("SELECT COUNT(e) FROM Event e")
    long countAllEvents();

    @Query("SELECT COUNT(e) FROM Event e WHERE e.eventDate >= :today")
    long countUpcomingEvents(@Param("today") LocalDate today);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.eventDate = :today")
    long countTodayEvents(@Param("today") LocalDate today);

    @Query("SELECT COUNT(DISTINCT e.venue) FROM Event e WHERE e.eventDate >= :today")
    long countDistinctUpcomingVenues(@Param("today") LocalDate today);

    @Query("SELECT DISTINCT e.venue FROM Event e WHERE e.eventDate >= :today ORDER BY e.venue")
    List<String> findDistinctUpcomingVenues(@Param("today") LocalDate today);
}
