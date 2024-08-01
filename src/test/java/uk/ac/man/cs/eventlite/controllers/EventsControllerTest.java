package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findPastEvents()).thenReturn(Collections.<Event>emptyList());
		when(eventService.findUpcomingEvents()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findPastEvents();
		verify(eventService).findUpcomingEvents();
//		verify(venueService).findAll();
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

		when(event.getVenue()).thenReturn(new Venue());
		when(eventService.findPastEvents()).thenReturn(Collections.<Event>singletonList(event));
		when(eventService.findUpcomingEvents()).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findPastEvents();
		verify(eventService).findUpcomingEvents();
		
	}

	@Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/events/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found")).andExpect(handler().methodName("getEvent"));
	}

	@Test
	public void getEventFound() throws Exception {
        when(eventService.findById(2L)).thenReturn(true);
        when(eventService.getEventById(2L)).thenReturn(event);
        when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));
        when(venue.getName()).thenReturn("Kilburn");
        when(event.getVenue()).thenReturn(venue);
        mvc.perform(get("/events/2").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("events/seperate_eventPage")).andExpect(handler().methodName("getEvent"));
        verify(eventService).getEventById(2L);
    }
	
	@Test
	public void getNewEvent() throws Exception {
		mvc.perform(get("/events/new").with(user("Oscar").roles(Security.ORGANIZER_ROLE)).accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk()).andExpect(view().name("events/new"))
		.andExpect(handler().methodName("newEvent"));
	}

	@Test
	public void testCreateEventNoAuth() throws Exception {
		mvc.perform(post("/events").contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.param("name", "test event")
		.param("date", LocalDate.now().plusDays(1).toString())
		.param("time", LocalTime.now().toString())
		.param("venue.id", "1000")
		.param("id", "1000")
		.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(header().string("Location", "http://localhost/sign-in"));
	
		verify(eventService, never()).save(any(Event.class));
	}

	@Test
	public void testCreateEvent() throws Exception {
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());
		mvc.perform(post("/events").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "test event")
		.param("date", LocalDate.now().plusDays(1).toString()).param("time", LocalTime.now().toString())
		.param("venue.id", "1000").param("id", "1000").accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(model().hasNoErrors())
		.andExpect(status().isFound())
		.andExpect(content().string(""))
		.andExpect(view().name("redirect:/events"))
		.andExpect(handler().methodName("createEvent"))
		.andExpect(flash().attributeExists("ok_message"));
		
		verify(eventService).save(arg.capture());
		assertThat("test event", equalTo(arg.getValue().getName()));
	}

	@Test
	public void testCreateEventInvalid() throws Exception {
		mvc.perform(post("/events").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "test event")
		.param("date", LocalDate.now().minusDays(1).toString()).param("time", LocalTime.now().toString())
		.param("venue.id", "1000").param("id", "1000").accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(model().hasErrors()).andExpect(status().isOk()).andExpect(view().name("events/new"))
		.andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(any(Event.class));
	}

	@Test
	public void deleteEventNotFound() throws Exception{
		when(eventService.findById(1)).thenReturn(false);
		
		mvc.perform(delete("/events/1").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isNotFound()).andExpect(view().name("events/not_found")).andExpect(handler().methodName("deleteEvent"));
		
		verify(eventService, never()).deleteById(1);
	}
	
	@Test
	public void deleteEventWithAdminRole() throws Exception{
		when(eventService.findById(1)).thenReturn(true);
		
		mvc.perform(delete("/events/1").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound()).andExpect(view().name("redirect:/events")).andExpect(handler().methodName("deleteEvent")).andExpect(flash().attributeExists("ok_message"));
		
		verify(eventService).deleteById(1);
	}
	
	@Test
	public void deleteEventWithOrganizerRole() throws Exception{
		when(eventService.findById(1)).thenReturn(true);
		
		mvc.perform(delete("/events/1").with(user("Oscar").roles(Security.ORGANIZER_ROLE)).accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound()).andExpect(view().name("redirect:/events")).andExpect(handler().methodName("deleteEvent")).andExpect(flash().attributeExists("ok_message"));
		
		verify(eventService).deleteById(1);
	}
	
	@Test
	public void deleteEventWithNoAuth() throws Exception{
		when(eventService.findById(1)).thenReturn(false);
		
		mvc.perform(delete("/events/1").accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound()).andExpect(header().string("Location", endsWith("/sign-in")));
		
		verify(eventService, never()).deleteById(1);
	}

	/**
	 * Test qualified data and successfully update data(sensible data)
	 */
	@Test
	public void testUpdateEvent() throws Exception{
		when(eventService.updateFindById(3L)).thenReturn(Optional.of(event));
		mvc.perform(post("/events/update").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "eventTest")
				.param("date", LocalDate.now().plusDays(1).toString())
				.param("time", LocalTime.now().toString())
				.param("venue.id", "1000")
				.param("id", "1000")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(handler().methodName("updatedEvent"))
				.andExpect(view().name("redirect:/events"));
		verify(eventService).updateEvent(any(Event.class));
	}
	/**
	 * Test unqualified data and stay on the same page without being able to perform update operation(bad data)
	 * This case is test the date should be the future
	 */
	@Test
	public void testUpdateEventBadDataFailed() throws Exception{
		when(eventService.updateFindById(1)).thenReturn(Optional.of(event));
		mvc.perform(post("/events/update").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "eventTest")
				.param("date", LocalDate.now().toString())
				.param("time", LocalTime.now().toString())
				.param("venue.id", "1000")
				.param("id", "1000")
				.param("description", "testDescription")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(handler().methodName("updatedEvent"))
				.andExpect(view().name("events/update"));
		verify(eventService, never()).updateEvent(any(Event.class));
	}
	/**
	 * Test no data and stay on the same page without being able to perform update operation(no data)
	 * name,data,time are empty
	 */
	@Test
	public void testUpdateEventNoDataFailed() throws Exception{
		when(eventService.updateFindById(1)).thenReturn(Optional.of(event));
		mvc.perform(post("/events/update").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "")
				.param("date", "")
				.param("time", "")
				.param("venue.id", "1000")
				.param("id", "1000")
				.param("description", "testDescription")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(handler().methodName("updatedEvent"))
				.andExpect(view().name("events/update"));
		verify(eventService, never()).updateEvent(any(Event.class));
	}
	/**
	 * Test that users without authority cannot use the update functionality(unauthorised user)
	 * Not Login
	 */
	@Test
	public void testUpdateNoAuth() throws Exception{
		when(eventService.updateFindById(1)).thenReturn(Optional.of(event));
		mvc.perform(post("/events/update")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));
		verify(eventService, never()).updateEvent(any(Event.class));
	}
	/**
	 * Test a non-existing event and redirect to a page indicating that the event does not exist(no data)
	 * @throws Exception EventNotFoundException
	 */
	@Test
	public void showSingleEventInfoNotFound() throws Exception {
		when(eventService.updateFindById(300)).thenReturn(Optional.empty());
		mvc.perform(get("/events/update").param("eventId", "300").with(user("Rob").roles(Security.ADMIN_ROLE)))
				.andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found"));

		verify(eventService, never()).updateEvent(any(Event.class));
	}
	
	/**
	 * Test an existing event and successfully display it on the update page(sensible data)
	 */
	@Test
	public void showSingleEventInfoFound() throws Exception {
		when(eventService.updateFindById(1)).thenReturn(Optional.of(event));
		when(event.getVenue()).thenReturn(new Venue());
		
		mvc.perform(get("/events/update").param("eventId", "1").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isOk()).andExpect(view().name("events/update"))
				.andExpect(handler().methodName("showInfo"));

		verify(eventService, never()).updateEvent(any(Event.class));
	}
	@Test
	//Test to check if all events are being retrieved
    public void testGetAllEvents() throws Exception {
        List<Event> pastEvents = List.of(); // Empty list for simplicity
        List<Event> upcomingEvents = List.of(); // Empty list for simplicity
        when(eventService.findPastEvents()).thenReturn(pastEvents);
        when(eventService.findUpcomingEvents()).thenReturn(upcomingEvents);

        
        mvc.perform(MockMvcRequestBuilders.get("/events"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("past_event", pastEvents))
                .andExpect(MockMvcResultMatchers.model().attribute("event", upcomingEvents))
                .andExpect(MockMvcResultMatchers.view().name("events/index"));

        // Verify that the methods of the mocked services are called
        verify(eventService).findPastEvents();
        verify(eventService).findUpcomingEvents();
    }
	
	 @Test
	    public void testSearchEventsByName() throws Exception {
	        // Arrange
	        String query = "test";
	        List<Event> searchResults = List.of(); // Empty list for simplicity
	        List<Event> pastSearchResults = List.of(); // Empty list for simplicity
	        when(eventService.search(query)).thenReturn(searchResults);
	        when(eventService.searchPastEvents(query)).thenReturn(pastSearchResults);

	        
	    }
	 @Test
	// Test to verify that event details are correctly displayed on the event page
	public void testGetEventDetails() throws Exception {
	    // Arrange
	    long eventId = 1L;
	    Event event = new Event(); // Mock event
	    event.setId(eventId);
	    when(eventService.findById(eventId)).thenReturn(true);
	    when(eventService.getEventById(eventId)).thenReturn(event);
	    Venue venue = new Venue(); //
	    venue.setName("Kilburn");
	    when(venueService.findAll()).thenReturn(Collections.singletonList(venue));

//	    
	}
}
