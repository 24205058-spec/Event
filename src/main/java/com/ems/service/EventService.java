package com.ems.service;

import com.ems.dto.EventDTO;
import com.ems.entity.Event;
import com.ems.exception.UnauthorizedAccessException;
import com.ems.exception.VenueConflictException;
import com.ems.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository repo;

    public EventService(EventRepository repo) {
        this.repo = repo;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public Event createEvent(EventDTO dto) {
        LocalTime endTime = dto.getStartTime().plusMinutes(dto.getDurationInMinutes());
        checkVenueConflict(dto.getVenue(), dto.getEventDate(), dto.getStartTime(), endTime, -1L);
        Event event = toEntity(dto, new Event());
        Event saved = repo.save(event);
        log.info("Created event id={}", saved.getId());
        return saved;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public Event getEventById(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new NoSuchElementException("Event not found: " + id));
    }

    public List<Event> getUpcomingEvents() {
        return repo.findUpcomingEvents(LocalDate.now());
    }

    public Page<Event> searchEvents(String title, String venue, int page, String sortBy, String sortDir) {
        String t = title == null ? "" : title.trim();
        String v = venue == null ? "" : venue.trim();

        Sort.Direction dir = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String field;
        if ("startTime".equals(sortBy)) {
            field = "startTime";
        } else if ("title".equals(sortBy)) {
            field = "title";
        } else if ("venue".equals(sortBy)) {
            field = "venue";
        } else {
            field = "eventDate";
        }
        Sort sort = Sort.by(dir, field).and(Sort.by(Sort.Direction.ASC, "startTime"));
        Pageable pageable = PageRequest.of(page, 6, sort);

        return repo.findUpcomingEventsByTitleAndVenue(LocalDate.now(), t, v, pageable);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public Event updateEvent(Long id, EventDTO dto, String requestedEmail) {
        Event existing = getEventById(id);
        verifyOrganizerEmail(existing, requestedEmail);

        LocalTime endTime = dto.getStartTime().plusMinutes(dto.getDurationInMinutes());
        checkVenueConflict(dto.getVenue(), dto.getEventDate(), dto.getStartTime(), endTime, id);

        toEntity(dto, existing);
        Event updated = repo.save(existing);
        log.info("Updated event id={}", id);
        return updated;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteEvent(Long id, String requestedEmail) {
        Event existing = getEventById(id);
        verifyOrganizerEmail(existing, requestedEmail);
        repo.deleteById(id);
        log.info("Deleted event id={}", id);
    }

    // ── DASHBOARD ─────────────────────────────────────────────────────────────

    public Map<String, Object> getDashboardStats() {
        LocalDate today = LocalDate.now();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents",    repo.countAllEvents());
        stats.put("upcomingEvents", repo.countUpcomingEvents(today));
        stats.put("todayEvents",    repo.countTodayEvents(today));
        stats.put("distinctVenues", repo.countDistinctUpcomingVenues(today));
        stats.put("venueList",      repo.findDistinctUpcomingVenues(today));
        return stats;
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    public void verifyOrganizerEmail(Event event, String email) {
        if (email == null || email.isBlank()) {
            throw new UnauthorizedAccessException("Organizer email is required.");
        }
        if (!event.getOrganizerEmail().equalsIgnoreCase(email.trim())) {
            throw new UnauthorizedAccessException(
                "Incorrect organizer email. You are not authorized to modify this event.");
        }
    }

    private void checkVenueConflict(String venue, LocalDate date,
                                    LocalTime start, LocalTime end, Long excludeId) {
        // Simple overlap check using JPQL with time comparison
        List<Event> all = repo.findUpcomingEvents(date);
        for (Event e : all) {
            if (!e.getEventDate().equals(date)) continue;
            if (excludeId != null && e.getId() != null && e.getId().equals(excludeId)) continue;
            if (!e.getVenue().equalsIgnoreCase(venue)) continue;

            LocalTime eStart = e.getStartTime();
            LocalTime eEnd   = e.getEndTime();

            // Overlap condition: start < eEnd AND end > eStart
            if (start.isBefore(eEnd) && end.isAfter(eStart)) {
                throw new VenueConflictException(
                    "Venue '" + venue + "' is already booked on " + date +
                    " from " + eStart + " to " + eEnd +
                    " (Event: '" + e.getTitle() + "'). Your slot " + start + "–" + end + " overlaps.");
            }
        }
    }

    public EventDTO toDTO(Event e) {
        EventDTO dto = new EventDTO();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setDescription(e.getDescription());
        dto.setVenue(e.getVenue());
        dto.setEventDate(e.getEventDate());
        dto.setStartTime(e.getStartTime());
        dto.setDurationInMinutes(e.getDurationInMinutes());
        dto.setOrganizerName(e.getOrganizerName());
        dto.setOrganizerEmail(e.getOrganizerEmail());
        return dto;
    }

    private Event toEntity(EventDTO dto, Event e) {
        e.setTitle(dto.getTitle().trim());
        e.setDescription(dto.getDescription() == null ? "" : dto.getDescription().trim());
        e.setVenue(dto.getVenue().trim());
        e.setEventDate(dto.getEventDate());
        e.setStartTime(dto.getStartTime());
        e.setDurationInMinutes(dto.getDurationInMinutes());
        e.setOrganizerName(dto.getOrganizerName().trim());
        e.setOrganizerEmail(dto.getOrganizerEmail().trim().toLowerCase());
        return e;
    }
}
