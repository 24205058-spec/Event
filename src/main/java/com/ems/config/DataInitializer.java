package com.ems.config;

import com.ems.entity.Event;
import com.ems.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EventRepository repo;

    public DataInitializer(EventRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        createEvent("Tech Conference 2025", "Annual technology conference with speakers from across the industry.",
                "City Convention Centre", LocalDate.now().plusDays(5), LocalTime.of(9, 0), 480,
                "Alice Johnson", "alice@techconf.com");

        createEvent("Spring Boot Workshop", "Hands-on workshop for building production-ready Spring Boot apps.",
                "Room 101, IT Building", LocalDate.now().plusDays(10), LocalTime.of(10, 0), 180,
                "Bob Smith", "bob@workshop.dev");

        createEvent("Music Night Gala", "An evening of live classical music performances.",
                "Grand Auditorium", LocalDate.now().plusDays(3), LocalTime.of(19, 0), 150,
                "Carol White", "carol@music.org");

        createEvent("Startup Pitch Day", "Entrepreneurs pitch their startups to investors.",
                "Innovation Hub", LocalDate.now().plusDays(7), LocalTime.of(13, 0), 240,
                "David Lee", "david@startups.io");

        createEvent("Art Exhibition", "Showcasing contemporary art from local artists.",
                "City Art Gallery", LocalDate.now().plusDays(14), LocalTime.of(10, 0), 480,
                "Eva Martinez", "eva@artgallery.com");

        createEvent("Team Sports Day", "Outdoor team-building sports activities.",
                "University Sports Ground", LocalDate.now().plusDays(2), LocalTime.of(8, 30), 360,
                "Frank Chen", "frank@sports.club");
    }

    private void createEvent(String title, String desc, String venue,
                              LocalDate date, LocalTime start, int duration,
                              String organizerName, String organizerEmail) {
        Event e = new Event();
        e.setTitle(title);
        e.setDescription(desc);
        e.setVenue(venue);
        e.setEventDate(date);
        e.setStartTime(start);
        e.setDurationInMinutes(duration);
        e.setOrganizerName(organizerName);
        e.setOrganizerEmail(organizerEmail);
        repo.save(e);
    }
}
