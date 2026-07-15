package com.ems.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public class EventDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be 255 characters or fewer")
    private String title;

    @Size(max = 2000, message = "Description must be 2000 characters or fewer")
    private String description;

    @NotBlank(message = "Venue is required")
    @Size(max = 255, message = "Venue must be 255 characters or fewer")
    private String venue;

    @NotNull(message = "Event date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventDate;

    @NotNull(message = "Start time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;

    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 1440, message = "Duration cannot exceed 1440 minutes (24 hours)")
    private int durationInMinutes = 60;

    @NotBlank(message = "Organizer name is required")
    @Size(max = 255)
    private String organizerName;

    @NotBlank(message = "Organizer email is required")
    @Email(message = "Please provide a valid email")
    @Size(max = 255)
    private String organizerEmail;

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
