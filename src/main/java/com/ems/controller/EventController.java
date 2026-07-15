package com.ems.controller;

import com.ems.dto.EventDTO;
import com.ems.entity.Event;
import com.ems.exception.UnauthorizedAccessException;
import com.ems.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
public class EventController {

    private final EventService svc;

    public EventController(EventService svc) {
        this.svc = svc;
    }

    // ── HOME ──────────────────────────────────────────────────────────────────

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recentEvents", svc.getUpcomingEvents().stream().limit(3).toList());
        model.addAttribute("activePage", "home");
        return "index";
    }

    // ── DASHBOARD ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAllAttributes(svc.getDashboardStats());
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    // ── LIST / SEARCH ─────────────────────────────────────────────────────────

    @GetMapping("/events")
    public String listEvents(
            @RequestParam(defaultValue = "") String title,
            @RequestParam(defaultValue = "") String venue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "eventDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        Page<Event> eventPage = svc.searchEvents(title, venue, page, sortBy, sortDir);
        String reverseDir = "asc".equals(sortDir) ? "desc" : "asc";

        model.addAttribute("events", eventPage.getContent());
        model.addAttribute("currentPage", eventPage.getNumber());
        model.addAttribute("totalPages", eventPage.getTotalPages());
        model.addAttribute("totalItems", eventPage.getTotalElements());
        model.addAttribute("title", title);
        model.addAttribute("venue", venue);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", reverseDir);
        model.addAttribute("activePage", "events");
        return "event-list";
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @GetMapping("/events/create")
    public String showCreateForm(Model model) {
        model.addAttribute("eventDTO", new EventDTO());
        model.addAttribute("activePage", "create");
        return "create-event";
    }

    @PostMapping("/events/create")
    public String createEvent(
            @Valid @ModelAttribute("eventDTO") EventDTO dto,
            BindingResult br,
            Model model,
            RedirectAttributes ra) {

        if (br.hasErrors()) {
            model.addAttribute("activePage", "create");
            return "create-event";
        }
        try {
            Event saved = svc.createEvent(dto);
            ra.addFlashAttribute("successMessage", "Event '" + saved.getTitle() + "' created!");
            return "redirect:/events/" + saved.getId();
        } catch (Exception ex) {
            model.addAttribute("conflictError", ex.getMessage());
            model.addAttribute("activePage", "create");
            return "create-event";
        }
    }

    // ── DETAILS ───────────────────────────────────────────────────────────────

    @GetMapping("/events/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("event", svc.getEventById(id));
            model.addAttribute("activePage", "events");
            return "event-details";
        } catch (NoSuchElementException ex) {
            return "redirect:/events";
        }
    }

    // ── AUTH (before edit/delete) ─────────────────────────────────────────────

    @GetMapping("/events/{id}/auth")
    public String showAuth(
            @PathVariable Long id,
            @RequestParam(defaultValue = "edit") String action,
            Model model) {
        model.addAttribute("event", svc.getEventById(id));
        model.addAttribute("action", action);
        model.addAttribute("activePage", "events");
        return "event-auth";
    }

    @PostMapping("/events/{id}/auth")
    public String verifyAuth(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam String organizerEmail,
            Model model,
            RedirectAttributes ra) {

        Event event = svc.getEventById(id);
        try {
            svc.verifyOrganizerEmail(event, organizerEmail);
        } catch (UnauthorizedAccessException ex) {
            model.addAttribute("event", event);
            model.addAttribute("action", action);
            model.addAttribute("authError", ex.getMessage());
            model.addAttribute("activePage", "events");
            return "event-auth";
        }

        if ("delete".equals(action)) {
            ra.addFlashAttribute("verifiedEmail", organizerEmail);
            return "redirect:/events/" + id + "/confirm-delete";
        }
        ra.addFlashAttribute("verifiedEmail", organizerEmail);
        return "redirect:/events/" + id + "/edit";
    }

    // ── EDIT ──────────────────────────────────────────────────────────────────

    @GetMapping("/events/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            @ModelAttribute("verifiedEmail") String verifiedEmail,
            Model model) {

        if (verifiedEmail == null || verifiedEmail.isBlank()) {
            return "redirect:/events/" + id + "/auth?action=edit";
        }
        Event event = svc.getEventById(id);
        model.addAttribute("eventDTO", svc.toDTO(event));
        model.addAttribute("verifiedEmail", verifiedEmail);
        model.addAttribute("activePage", "events");
        return "edit-event";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEvent(
            @PathVariable Long id,
            @Valid @ModelAttribute("eventDTO") EventDTO dto,
            BindingResult br,
            @RequestParam String verifiedEmail,
            Model model,
            RedirectAttributes ra) {

        if (br.hasErrors()) {
            model.addAttribute("verifiedEmail", verifiedEmail);
            model.addAttribute("activePage", "events");
            return "edit-event";
        }
        try {
            Event updated = svc.updateEvent(id, dto, verifiedEmail);
            ra.addFlashAttribute("successMessage", "Event '" + updated.getTitle() + "' updated!");
            return "redirect:/events/" + id;
        } catch (UnauthorizedAccessException ex) {
            model.addAttribute("authError", ex.getMessage());
            model.addAttribute("verifiedEmail", verifiedEmail);
            model.addAttribute("activePage", "events");
            return "edit-event";
        } catch (Exception ex) {
            model.addAttribute("conflictError", ex.getMessage());
            model.addAttribute("verifiedEmail", verifiedEmail);
            model.addAttribute("activePage", "events");
            return "edit-event";
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @GetMapping("/events/{id}/confirm-delete")
    public String confirmDelete(
            @PathVariable Long id,
            @ModelAttribute("verifiedEmail") String verifiedEmail,
            Model model) {

        if (verifiedEmail == null || verifiedEmail.isBlank()) {
            return "redirect:/events/" + id + "/auth?action=delete";
        }
        model.addAttribute("event", svc.getEventById(id));
        model.addAttribute("verifiedEmail", verifiedEmail);
        model.addAttribute("activePage", "events");
        return "confirm-delete";
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(
            @PathVariable Long id,
            @RequestParam String organizerEmail,
            RedirectAttributes ra) {
        try {
            Event event = svc.getEventById(id);
            String title = event.getTitle();
            svc.deleteEvent(id, organizerEmail);
            ra.addFlashAttribute("successMessage", "Event '" + title + "' deleted.");
            return "redirect:/events";
        } catch (UnauthorizedAccessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/events/" + id + "/auth?action=delete";
        } catch (NoSuchElementException ex) {
            ra.addFlashAttribute("errorMessage", "Event not found.");
            return "redirect:/events";
        }
    }
}
