package com.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 255)
    private String venue;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "duration_minutes", nullable = false)
    private int durationInMinutes;

    @Column(name = "organizer_name", nullable = false, length = 255)
    private String organizerName;

    @Column(name = "organizer_email", nullable = false, length = 255)
    private String organizerEmail;

    // Derived (not persisted)
    public LocalTime getEndTime() {
        return startTime != null ? startTime.plusMinutes(durationInMinutes) : null;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public int getDurationInMinutes() { return durationInMinutes; }
    public void setDurationInMinutes(int durationInMinutes) { this.durationInMinutes = durationInMinutes; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public String getOrganizerEmail() { return organizerEmail; }
    public void setOrganizerEmail(String organizerEmail) { this.organizerEmail = organizerEmail; }
}
