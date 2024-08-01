package uk.ac.man.cs.eventlite.controllers;

import java.util.List;
import com.mapbox.geojson.Point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.man.cs.eventlite.config.Container;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

    private final static Logger log = LoggerFactory.getLogger(EventsController.class);
    private final String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoiaDEwIiwiYSI6ImNsdjJieG9nYTBnZHQybG9xOGowNHd6ejMifQ.yM0CR9CYCdzxATuK_qv92w";

	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id, Model model) throws InterruptedException{
		
		
		if(!eventService.findById(id)){
			throw new EventNotFoundException(id);
		}
	
		Event event = eventService.getEventById(id);
		model.addAttribute("event", event);
		model.addAttribute("venues",venueService.findAll());
		for (Venue venue : venueService.findAll()) {
			venueService.mapboxGeocoding(venue);
			Thread.sleep(1000L);
		}
		return "events/seperate_eventPage";
	}
	
	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}


	
	@GetMapping("/new")
	public String newEvent(Model model) {
		if(!model.containsAttribute("event")) {
			model.addAttribute("event", new Event());
		}
		List<Venue> venues = (List<Venue>) venueService.findAll();
		model.addAttribute("venues", venues);
		return "events/new";
	}
	
    @GetMapping("/search")
    public String searchEventsByName(@RequestParam("query") String query, Model model) {
        List<Event> searchResults = eventService.search(query);
//        EventComparator eventComparator = new EventComparator();
//        searchResults.sort(eventComparator);
        model.addAttribute("event", searchResults);
        
        List<Event> pastSearchResults = eventService.searchPastEvents(query);
        model.addAttribute("past_event", pastSearchResults);
        
        return "events/index"; // Assuming you want to display search results on the same page as the event list
    }
    
	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createEvent(@Valid @ModelAttribute("event") Event event, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {
		
		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venues", venueService.findAll());
			return "events/new";
		}
		
		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "New event added.");
		
		return "redirect:/events";
	}
	
	
	@GetMapping("/update")
	public String showInfo(@RequestParam("eventId") long eventId,Model m) {
		Optional<Event> uevent = eventService.updateFindById(eventId);
		if(!uevent.isEmpty()) {
			m.addAttribute("event",uevent.get());
			m.addAttribute("venues", venueService.findAll());
			return "events/update";
		}
		m.addAttribute("venues", venueService.findAll());
		throw new EventNotFoundException(eventId);
	}
	
	@PostMapping("/update")
    public String updatedEvent(@Valid @ModelAttribute Event e, BindingResult result,RedirectAttributes redirectAttrs,Model m) {
        if (result.hasErrors()) {
        	m.addAttribute("venues", venueService.findAll());
            return "events/update";
        }
        eventService.updateEvent(e);
        redirectAttrs.addFlashAttribute("ok_message", "The event updated.");
        return "redirect:/events";
    }

	
	
	
	@GetMapping
	public String getAllEvents(Model model) {
		//shows all events that are before the current date
		model.addAttribute("past_event", eventService.findPastEvents());
		//shows all events that are after the current date
		model.addAttribute("event", eventService.findUpcomingEvents());
//		model.addAttribute("venues", venueService.findAll());#
		return "events/index";
	}
	
	@DeleteMapping("/{id}")
	public String deleteEvent(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		if(!eventService.findById(id)) {
			throw new EventNotFoundException(id);
		}
		
		eventService.deleteById(id);
		redirectAttrs.addFlashAttribute("ok_message", "Event deleted successfully.");
		
		return "redirect:/events";
	}

}
