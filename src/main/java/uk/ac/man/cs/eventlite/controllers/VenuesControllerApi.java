package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

	private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

	@Autowired
	private EventService eventService;
	

	@Autowired
	private VenueModelAssembler venueAssembler;
	
	@Autowired
	private VenueService venueService;

	@ExceptionHandler(VenueNotFoundException.class)
	public ResponseEntity<?> venueNotFoundHandler(VenueNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(String.format(NOT_FOUND_MSG, ex.getMessage(), ex.getId()));
	}

	@GetMapping("/{id}")
	public EntityModel<Venue> getVenue(@PathVariable("id") long id) {
//		throw new VenueNotFoundException(id);
		Venue venue = venueService.updateFindById(id).orElseThrow(() -> new VenueNotFoundException(id));

		return venueAssembler.toModel(venue);
	}


	@GetMapping
	public CollectionModel<EntityModel<Venue>> getAllVenues() {
		return venueAssembler.toCollectionModel(venueService.findAll())
				.add(linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel(),
						Link.of("https://localhost:8080/api/profile/venues").withRel("profile"));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteVenue(@PathVariable("id") long id){
		Optional<Venue> venueOptional = venueService.updateFindById(id);
		if(venueOptional.isEmpty()) {
			throw new VenueNotFoundException(id);
		}
		
		Venue venue = venueOptional.get();
		List<Event> events = venue.getEvents();
		if(!events.isEmpty()) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete venue with more than one event.");
		}
		venueService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/{id}/next3events")
	public List<Event> getNextThreeEvents(@PathVariable("id") long id){
		if(!venueService.findById(id)) {
			throw new VenueNotFoundException(id);
		}
		
		return venueService.getNextThreeEvents(id);
	}
}
