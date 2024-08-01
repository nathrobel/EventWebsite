package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.assertj.core.api.Assertions.assertThat;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;



import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Collections;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsControllerApi.class)
@Import({ Security.class, EventModelAssembler.class })
public class EventsControllerApiTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private EventService eventService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

		verify(eventService).findAll();
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		Event e = new Event();
		e.setId(0);
		e.setName("Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		e.setVenue(new Venue());
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)))
				.andExpect(jsonPath("$._embedded.events[0]._links.venue.href", not(empty())))
				.andExpect(jsonPath("$._embedded.events[0]._links.venue.href", endsWith("events/0/venue")));

		verify(eventService).findAll();
	}

	@Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/api/events/99").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("event 99"))).andExpect(jsonPath("$.id", equalTo(99)))
				.andExpect(handler().methodName("getEvent"));
	}
	
	@Test
	public void deleteEventWithAdmin() throws Exception {
		when(eventService.findById(1)).thenReturn(true);

		mvc.perform(delete("/api/events/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent()).andExpect(content().string(""))
				.andExpect(handler().methodName("deleteEvent"));

		verify(eventService).deleteById(1);
	}
	
	@Test
	public void deleteEventWithOrganizer() throws Exception {
		when(eventService.findById(1)).thenReturn(true);

		mvc.perform(delete("/api/events/1").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent()).andExpect(content().string(""))
				.andExpect(handler().methodName("deleteEvent"));

		verify(eventService).deleteById(1);
	}

	@Test
	public void deleteEventNotFound() throws Exception {
		when(eventService.findById(1)).thenReturn(false);

		mvc.perform(delete("/api/events/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("event 1"))).andExpect(jsonPath("$.id", equalTo(1)))
				.andExpect(handler().methodName("deleteEvent"));

		verify(eventService, never()).deleteById(1);
	}

	@Test
	public void deleteEventWithNoAuth() throws Exception{
		when(eventService.findById(1)).thenReturn(true);
		
		mvc.perform(delete("/api/events/1").accept(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isUnauthorized());
		
		verify(eventService, never()).deleteById(1);
	}
	
	//to test the creation of a new event
	@Test
	
	public void createEvent() throws Exception {
	    // Mock data for the new event
	    Event event = new Event();
	    event.setId(1);
	    event.setName("Test Event");
	    event.setDate(LocalDate.of(2024, 4, 25)); 
	    event.setTime(LocalTime.of(10, 0)); 
	    event.setVenue(new Venue()); 

	   
	    String jsonEvent = "{\"id\":1,\"name\":\"Test Event\",\"date\":\"2024-04-25\",\"time\":\"10:00\"}";

	    // Perform the POST request to create a new event
	    MvcResult result = mvc.perform(post("/api/events")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(jsonEvent))
	  
	            .andReturn();

	 
	}
	
	@Test
	public void getEvent() throws Exception {
	    // Mock data for the event
	    Event event = new Event();
	    event.setId(1);
	    event.setName("Test Event");
	    event.setDate(LocalDate.of(2024, 4, 25));
	    event.setTime(LocalTime.of(10, 0));
	    event.setVenue(new Venue());

	    // Mock the service method to return the event
	    when(eventService.updateFindById(1)).thenReturn(Optional.of(event));

	    // Perform the GET request to retrieve the event
	    mvc.perform(get("/api/events/1")
	            .accept(MediaType.APPLICATION_JSON))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.id", is(1)))
	            .andExpect(jsonPath("$.name", is("Test Event")))
	            .andExpect(jsonPath("$.date", is("2024-04-25")))
	          
	            .andExpect(jsonPath("$._links.self.href", endsWith("/api/events/1")));

	    // Verify that the service method updateFindById was called with the correct ID
	    verify(eventService).updateFindById(1);
	}

	@Test
	public void newEvent() throws Exception {
	   
	    mvc.perform(get("/api/events/new")
	            .accept(MediaType.TEXT_HTML))
	            .andExpect(status().isNotAcceptable());
	}


	}
