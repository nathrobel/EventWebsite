package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
@Import({ Security.class, VenueModelAssembler.class })
public class VenuesControllerApiTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private EventService eventService;
	
	@MockBean
	private VenueService venueService;

	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));

		verify(venueService).findAll();
	}

	@Test
	public void getIndexWithVenues() throws Exception {
		Venue v = new Venue();
		v.setId(0);
		v.setName("Venue");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(v));

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
				.andExpect(jsonPath("$._embedded.venues[0].name", equalTo("Venue")))
				.andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)))
				.andExpect(jsonPath("$._embedded.venues[0]._links.self.href", endsWith("api/venues/0")))
				.andExpect(jsonPath("$._embedded.venues[0]._links.events.href", endsWith("api/venues/0/events")));

		verify(venueService).findAll();
	}

	@Test
	public void getVenueNotFound() throws Exception {
		mvc.perform(get("/api/venues/99").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("venue 99"))).andExpect(jsonPath("$.id", equalTo(99)))
				.andExpect(handler().methodName("getVenue"));
	}
	
	@Test
	public void getNextThreeEventsNotFound() throws Exception{
		mvc.perform(get("/api/venues/99/next3events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
		.andExpect(jsonPath("$.error", containsString("venue 99"))).andExpect(jsonPath("$.id", equalTo(99)))
		.andExpect(handler().methodName("getNextThreeEvents"));
	}
	
	@Test
	public void getVenueNextThreeEventsSuccess() throws Exception {
		Venue v = new Venue();
	    v.setId(3);
	    List<Event> events = new ArrayList<>();
	    v.setEvents(events);
	    
	    when(venueService.findById(3)).thenReturn(true);

	    mvc.perform(get("/api/venues/3/next3events").accept(MediaType.APPLICATION_JSON))
	        .andExpect(status().isOk())
	        .andExpect(handler().methodName("getNextThreeEvents"));
	    
	    verify(venueService).getNextThreeEvents(3);
	}

	
	@Test
	public void deleteVenueContainsNoEvent() throws Exception {
		Venue v = new Venue();
		v.setId(1);
		v.setName("Venue");
		List<Event> events = new ArrayList<>();
		v.setEvents(events);
	    
	    when(venueService.updateFindById(1)).thenReturn(Optional.of(v));
	    
	    mvc.perform(delete("/api/venues/1").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent()).andExpect(content().string(""))
				.andExpect(handler().methodName("deleteVenue"));

		verify(venueService).deleteById(1);
	}
	
	@Test
	public void deleteVenueContainsEvents() throws Exception {
	    Venue venue = new Venue();
	    venue.setId(1);
	    venue.setName("Venue");
	    List<Event> events = new ArrayList<>();
	    Event event1 = new Event();
	    event1.setId(1);
	    event1.setName("Event 1");
	    events.add(event1);
	    Event event2 = new Event();
	    event2.setId(2);
	    event2.setName("Event 2");
	    events.add(event2);
	    venue.setEvents(events);

	    when(venueService.updateFindById(1)).thenReturn(Optional.of(venue));

	    mvc.perform(delete("/api/venues/1")
	            .with(user("Oscar").roles(Security.ORGANIZER_ROLE))
	            .accept(MediaType.APPLICATION_JSON))
	            .andExpect(status().isConflict())
	            .andExpect(content().string("Cannot delete venue with more than one event."))
	            .andExpect(handler().methodName("deleteVenue"));

	    verify(venueService, never()).deleteById(1);
	}
	
	@Test
	public void deleteVenueNotFound() throws Exception {
		when(venueService.findById(1)).thenReturn(false);

		mvc.perform(delete("/api/venues/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("venue 1"))).andExpect(jsonPath("$.id", equalTo(1)))
				.andExpect(handler().methodName("deleteVenue"));

		verify(venueService, never()).deleteById(1);
	}
	
	@Test
	public void deleteVenueWithNoAuth() throws Exception{
		when(venueService.findById(1)).thenReturn(true);
		
		mvc.perform(delete("/api/venues/1").accept(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isUnauthorized());
		
		verify(venueService, never()).deleteById(1);
	}
	
	@Test
	public void getVenue() throws Exception {
	    // Mock data for the venue
	    Venue venue = new Venue();
	    venue.setId(1);
	    venue.setName("Test Venue");

	    // Mock the service method to return the venue
	    when(venueService.updateFindById(1)).thenReturn(Optional.of(venue));

	    // Perform the GET request to retrieve the venue
	    mvc.perform(get("/api/venues/1")
	            .accept(MediaType.APPLICATION_JSON))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.id", is(1)))
	            .andExpect(jsonPath("$.name", is("Test Venue")))
	            .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1")));

	    // Verify that the service method updateFindById was called with the correct ID
	    verify(venueService).updateFindById(1);
	}

}
