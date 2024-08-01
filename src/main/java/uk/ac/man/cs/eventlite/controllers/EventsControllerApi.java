package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
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
import jakarta.validation.Valid;
import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@RestController
@RequestMapping(value = "/api/events", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class EventsControllerApi {

	private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

	@Autowired
	private EventService eventService;

	@Autowired
	private EventModelAssembler eventAssembler;

	@ExceptionHandler(EventNotFoundException.class)
	public ResponseEntity<?> eventNotFoundHandler(EventNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(String.format(NOT_FOUND_MSG, ex.getMessage(), ex.getId()));
	}

	@GetMapping("/{id}")
	public EntityModel<Event> getEvent(@PathVariable("id") long id) {
//		throw new EventNotFoundException(id);
		Event event = eventService.updateFindById(id).orElseThrow(() -> new EventNotFoundException(id));

		return eventAssembler.toModel(event);
	}
	
	@GetMapping("/new")
	public ResponseEntity<?> newEvent() {
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createEvent(@RequestBody @Valid Event event, BindingResult result) {
		
		if (result.hasErrors()) {
			return ResponseEntity.unprocessableEntity().build();
		}
		
		Event newEvent = eventService.save(event);
		EntityModel<Event> entity = eventAssembler.toModel(newEvent);
		
		return ResponseEntity.created(entity.getRequiredLink(IanaLinkRelations.SELF).toUri()).build();	
	}
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateEvent(@RequestBody @Valid Event event, BindingResult result) {

	    if (result.hasErrors()) {
	        return ResponseEntity.unprocessableEntity().build();
	    }
	    Optional<Event> existingEvent = eventService.updateFindById(event.getId());
	    if (existingEvent.isEmpty()) {
	        return ResponseEntity.notFound().build();
	    }
	    eventService.updateEvent(event);
	    EntityModel<Event> entity = eventAssembler.toModel(event);
	    return ResponseEntity.ok(entity);
	}


	@GetMapping
	public CollectionModel<EntityModel<Event>> getAllEvents() {
		return eventAssembler.toCollectionModel(eventService.findAll())
				.add(linkTo(methodOn(EventsControllerApi.class).getAllEvents()).withSelfRel());
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteEvent(@PathVariable("id") long id){
		if(!eventService.findById(id)) {
			throw new EventNotFoundException(id);
		}
		
		eventService.deleteById(id);
		
		return ResponseEntity.noContent().build();
	}
}
