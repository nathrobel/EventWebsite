package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doCallRealMethod;
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

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import java.util.*;
import java.util.Arrays;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import java.util.List;




@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)


public class VenuesControllerTest {

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
    public void testSearchForVenueWithNoMatch() throws Exception {
    	//testing the search for a venue with no match
        when(venueService.searchByName("nonexistent")).thenReturn(Collections.emptyList());

        mvc.perform(get("/venues/search").param("query", "nonexistent"))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/index"))
            .andExpect(model().attributeExists("venues"))
            .andExpect(model().attribute("venues", is(empty()))); // Use `empty()` for any Collection, including List and Set.

        verify(venueService).searchByName("nonexistent");
    }

	@Test
    public void testSearchForVenueWithPartialMatch() throws Exception {
		//testing the search for a venue with a partial match
        Venue mockVenue = new Venue();
        mockVenue.setName("Test Venue");
        List<Venue> venues = Collections.singletonList(mockVenue);
        
        when(venueService.searchByName("Test")).thenReturn(venues);

        mvc.perform(get("/venues/search").param("query", "Test"))
            .andExpect(status().isOk())
            .andExpect(view().name("venues/index"))
            .andExpect(model().attributeExists("venues"))
            .andExpect(model().attribute("venues", hasItem(hasProperty("name", containsString("Test Venue")))));

        verify(venueService).searchByName("Test");
    }
	
	@Test
	public void testSearchForVenueWithExactMatch() throws Exception {
		//testing the search for a venue with an exact match
		Venue mockVenue = new Venue();
		mockVenue.setName("Test Venue");
		List<Venue> venues = Collections.singletonList(mockVenue);

		when(venueService.searchByName("Test Venue")).thenReturn(venues);

		mvc.perform(get("/venues/search").param("query", "Test Venue")).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(model().attributeExists("venues"))
				.andExpect(model().attribute("venues", hasItem(hasProperty("name", containsString("Test Venue")))));

		verify(venueService).searchByName("Test Venue");
	}
	
	@Test
	public void testSearchForVenueWithExactMatchIgnoreCase() throws Exception {
		//testing the search for a venue with an exact match but ignoring case
		Venue mockVenue = new Venue();
		mockVenue.setName("Test Venue");
		List<Venue> venues = Collections.singletonList(mockVenue);

		when(venueService.searchByName("test venue")).thenReturn(venues);

		mvc.perform(get("/venues/search").param("query", "test venue")).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(model().attributeExists("venues"))
				.andExpect(model().attribute("venues", hasItem(hasProperty("name", containsString("Test Venue")))));

		verify(venueService).searchByName("test venue");
	}
	
	@Test
	public void testSearchedVenueHasCorrectValues() throws Exception {
		//testing that the searched venue has the correct values
        Venue mockVenue = new Venue();
        mockVenue.setName("Test Venue");
        mockVenue.setCapacity(100);
        mockVenue.setRoadName("Test Road");
        mockVenue.setPostcode("M13 9PL");
        List<Venue> venues = Collections.singletonList(mockVenue);

        when(venueService.searchByName("Test Venue")).thenReturn(venues);

        mvc.perform(get("/venues/search").param("query", "Test Venue")).andExpect(status().isOk())
                .andExpect(view().name("venues/index")).andExpect(model().attributeExists("venues"))
                .andExpect(model().attribute("venues", hasItem(hasProperty("name", containsString("Test Venue")))))
                .andExpect(model().attribute("venues", hasItem(hasProperty("capacity", is(100)))))
                .andExpect(model().attribute("venues", hasItem(hasProperty("roadName", containsString("Test Road")))))
                .andExpect(model().attribute("venues", hasItem(hasProperty("postcode", containsString("M13 9PL")))));

        verify(venueService).searchByName("Test Venue");
    }

//	@Test
//	public void getIndexWhenNoVenues() throws Exception {
//		
//		/*
//		 * This test will ensure the index page will work as intended when no Venues or
//		 * events are present. Verification will be used to ensure the findAll() function
//		 * works as intended.
//		 */
//		
//		// Set eventService and VenueService to return empty lists when findAll() is called
//		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
//		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());
//
//		// Get the index
//		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
//			.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));
//
//		// Verify all events can be found
//		verify(venueService).findAll();
//	}
	
	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));
//		verify(venueService).findAll();
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		
		/*
		 * This test will ensure the index page will work as intended when there is a venue
		 * containing an event. Verification will be used to ensure the findAll() function
		 * works as intended.
		 */
		
		// Set an ID to be used by Venues and Events
		long mockID = 1L;
		
		// Mock a Venue
		when(venue.getName()).thenReturn("My Amazing Venue");
		when(venue.getId()).thenReturn(mockID);
		when(venue.getEvents()).thenReturn(Collections.singletonList(event));
		
		// Mock a Venue Service
		when(venueService.findAll()).thenReturn(Collections.singletonList(venue));
		when(venueService.findById(mockID)).thenReturn(true);
		when(venueService.updateFindById(mockID)).thenReturn(Optional.of(venue));
		when(venueService.count()).thenReturn(mockID);
		
		// Mock an Event
		when(event.getName()).thenReturn("My Amazing Event");
		when(event.getId()).thenReturn(mockID);
		when(event.getVenue()).thenReturn(venue);
		
		// Mock and Event Service
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));

		// Get the index
		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
			.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		// Verify all events can be found
		verify(venueService).findAll();
	}

	@Test
	public void getVenueNotFound() throws Exception {
		
		/*
		 * This test will attempt to get a Venue that doesn't exist. This should return a 404
		 * error which means the code is working as intended.
		 */
		
		// Set an ID to be used by the Venue
		long mockID = 99L;
		
		// Ensure the Event and Venue services are completely empty
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());
		
		// Attempt to get the Venue, since it doesn't exist it should return 404
		mvc.perform(get("/venues/{mockID}", mockID).accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
			.andExpect(view().name("venues/not_found")).andExpect(handler().methodName("getVenue"));
	}
	
	@Test
	public void deleteVenueNotFound() throws Exception{
		
		/*
		 * This test will attempt to delete a Venue that doesn't exist. This should return a
		 * 404 error which means the code is working as intended.
		 */
		
		// Set an ID to be used by the Venue
		long mockID = 99L;
		
		// Ensure the Event and Venue services are completely empty
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		// Try to delete Venue
		mvc.perform(delete("/venues/{mockID}", mockID).with(user("Mr. Delete").roles(Security.ADMIN_ROLE))
			.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isNotFound())
			.andExpect(view().name("venues/not_found")).andExpect(handler().methodName("deleteVenue"));
		
		// Make sure nothing got deleted
		verify(venueService, never()).deleteById(mockID);
	}
	

	@Test
	public void deleteVenueHavingZeroEventWithAdminRole() throws Exception{
		
		/*
		 * This test will attempt to delete a Venue containing a single event. This starts
		 * by getting the Venue which should return 200, this makes sure the Venue definitely
		 * exists. Then the event will be deleted by a user with admin privileges, this should
		 * return 302. Finally the deletion is verified to make sure the Venue definitely got
		 * deleted.
		 */
		
		// Set an ID to be used by Venues and Events
		long mockID = 1L;
		List<Event> eventLists = new ArrayList<>();
		
		// Mock a Venue
		when(venue.getName()).thenReturn("My Cool Venue");
		when(venue.getId()).thenReturn(mockID);
		when(venue.getEvents()).thenReturn(eventLists);
		
		
		// Mock a Venue Service
		when(venueService.findAll()).thenReturn(Collections.singletonList(venue));
		when(venueService.findById(mockID)).thenReturn(true);
		when(venueService.updateFindById(mockID)).thenReturn(Optional.of(venue));
		when(venueService.count()).thenReturn(mockID);
		
		// Mock an Event
		when(event.getName()).thenReturn("My Cool Event");
		when(event.getId()).thenReturn(mockID);
		when(event.getVenue()).thenReturn(venue);
		
		// Mock and Event Service
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));


		// Get the Venue, it should return status 200
		mvc.perform(get("/venues/{mockID}", mockID).accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk()).andExpect(handler().methodName("getVenue"));

		// Now delete the Venue!
		mvc.perform(delete("/venues/{mockID}", mockID).with(user("Mr. Authorised")
			.roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML).with(csrf()))
			.andExpect(status().isFound()).andExpect(view().name("redirect:/venues"))
			.andExpect(handler().methodName("deleteVenue")).andExpect(flash().attributeExists("ok_message"));
		
		// Verify the deletion!
		verify(venueService).deleteById(mockID);
	}
	
	@Test
	public void deleteVenueHavingZeroEventWithOrganizerRole() throws Exception{
		
		/*
		 * This test will attempt to delete a Venue containing a single event. This starts
		 * by getting the Venue which should return 200, this makes sure the Venue definitely
		 * exists. Then the event will be deleted by a user with organizer privileges, this
		 * should return 302. Finally the deletion is verified to make sure the Venue
		 * definitely got deleted.
		 */
		
		// Set an ID to be used by Venues and Events
		long mockID = 1L;
		List<Event> eventList = new ArrayList<>();
		
		// Mock a Venue
		when(venue.getName()).thenReturn("My Awesome Venue");
		when(venue.getId()).thenReturn(mockID);
		when(venue.getEvents()).thenReturn(eventList);
		
		
		// Mock a Venue Service
		when(venueService.findAll()).thenReturn(Collections.singletonList(venue));
		when(venueService.findById(mockID)).thenReturn(true);
		when(venueService.updateFindById(mockID)).thenReturn(Optional.of(venue));
		when(venueService.count()).thenReturn(mockID);
		
		// Mock an Event
		when(event.getName()).thenReturn("My Awesome Event");
		when(event.getId()).thenReturn(mockID);
		when(event.getVenue()).thenReturn(venue);
		
		// Mock and Event Service
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));


		// Get the Venue, it should return status 200
		mvc.perform(get("/venues/{mockID}", mockID).accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk()).andExpect(handler().methodName("getVenue"));

		// Now delete the Venue!
		mvc.perform(delete("/venues/{mockID}", mockID).with(user("Mr. Authorised")
			.roles(Security.ORGANIZER_ROLE)).accept(MediaType.TEXT_HTML).with(csrf()))
			.andExpect(status().isFound()).andExpect(view().name("redirect:/venues"))
			.andExpect(handler().methodName("deleteVenue")).andExpect(flash().attributeExists("ok_message"));
		
		// Verify the deletion!
		verify(venueService).deleteById(mockID);
	}
	
	@Test
	public void deleteVenueHavingMoreThanZeroEventWithAdminRole() throws Exception{
		/*
		 * This test will attempt to delete a Venue containing a two events. This starts by
		 * getting the Venue which should return 200, this makes sure the Venue definitely
		 * exists. Then the event will be deleted by a user with admin privileges, this should
		 * return 302 with an error message. Finally a verification is used to ensure the venue
		 * wasn't actually deleted.
		 */
		
		// Two ID values, one for the Venue and first event, one for the second event
		long mockID = 1L;
		long mockID_2 = 2L;
		
		// Mock a Venue
		when(venue.getName()).thenReturn("My Cool Venue");
		when(venue.getId()).thenReturn(mockID);
		when(venue.getEvents()).thenReturn(Arrays.asList(event, event));
		
		
		// Mock a Venue Service
		when(venueService.findAll()).thenReturn(Collections.singletonList(venue));
		when(venueService.findById(mockID)).thenReturn(true);
		when(venueService.findById(mockID_2)).thenReturn(true);
		when(venueService.updateFindById(mockID)).thenReturn(Optional.of(venue));
		when(venueService.updateFindById(mockID_2)).thenReturn(Optional.of(venue));
		when(venueService.count()).thenReturn(mockID_2);
		
		// Mock an Event
		when(event.getName()).thenReturn("My Cool Event");
		when(event.getId()).thenReturn(mockID);
		when(event.getVenue()).thenReturn(venue);
		
		// Mock and Event Service
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));


		// Get the Venue, it should return status 200
		mvc.perform(get("/venues/{mockID}", mockID).accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk()).andExpect(handler().methodName("getVenue"));

		// Try to delete the Venue with a fake role, this should fail and return 302 with an error message
		mvc.perform(delete("/venues/{mockID}", mockID).with(user("Mr. Authorised")
			.roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML).with(csrf()))
			.andExpect(status().isFound()).andExpect(view().name("redirect:/venues"))
			.andExpect(handler().methodName("deleteVenue")).andExpect(flash().attributeExists("error_message"));
		
		// Make sure nothing got deleted
		verify(venueService, never()).deleteById(mockID);
	}
	
	@Test
	public void deleteVenueHavingMoreThanZeroEventWithOrganizerRole() throws Exception{
		/*
		 * This test will attempt to delete a Venue containing a two events. This starts by
		 * getting the Venue which should return 200, this makes sure the Venue definitely
		 * exists. Then the event will be deleted by a user with admin privileges, this should
		 * return 302 with an error message. Finally a verification is used to ensure the venue
		 * wasn't actually deleted.
		 */
		
		// Two ID values, one for the Venue and first event, one for the second event
		long mockID = 1L;
		long mockID_2 = 2L;
		
		// Mock a Venue
		when(venue.getName()).thenReturn("My Cool Venue");
		when(venue.getId()).thenReturn(mockID);
		when(venue.getEvents()).thenReturn(Arrays.asList(event, event));
		
		
		// Mock a Venue Service
		when(venueService.findAll()).thenReturn(Collections.singletonList(venue));
		when(venueService.findById(mockID)).thenReturn(true);
		when(venueService.findById(mockID_2)).thenReturn(true);
		when(venueService.updateFindById(mockID)).thenReturn(Optional.of(venue));
		when(venueService.updateFindById(mockID_2)).thenReturn(Optional.of(venue));
		when(venueService.count()).thenReturn(mockID_2);
		
		// Mock an Event
		when(event.getName()).thenReturn("My Cool Event");
		when(event.getId()).thenReturn(mockID);
		when(event.getVenue()).thenReturn(venue);
		
		// Mock and Event Service
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));


		// Get the Venue, it should return status 200
		mvc.perform(get("/venues/{mockID}", mockID).accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk()).andExpect(handler().methodName("getVenue"));

		// Try to delete the Venue with a fake role, this should fail and return 302 with an error message
		mvc.perform(delete("/venues/{mockID}", mockID).with(user("Mr. Authorised")
			.roles(Security.ORGANIZER_ROLE)).accept(MediaType.TEXT_HTML).with(csrf()))
			.andExpect(status().isFound()).andExpect(view().name("redirect:/venues"))
			.andExpect(handler().methodName("deleteVenue")).andExpect(flash().attributeExists("error_message"));
		
		// Make sure nothing got deleted
		verify(venueService, never()).deleteById(mockID);
	}
	
	@Test
	public void deleteVenueWithNoAuth() throws Exception{

		/*
		 * This test will attempt to delete a Venue containing no events. This starts by
		 * getting the Venue which should return 200, this makes sure the Venue definitely
		 * exists. Then an attempt will be made to delete the Venue by a user with an invalid
		 * role. This should return 403. Finally a verification is used to ensure the Venue
		 * didn't get deleted.
		 */
		
		// Set an ID to be used by the Venue
		long mockID = 1L;
		
		// String for a role that shouldn't be allowed
		String mockRole = "Trust me I have admin";
		
		// Mock a Venue
		when(venue.getName()).thenReturn("My Badass Venue");
		when(venue.getId()).thenReturn(mockID);
		when(venue.getEvents()).thenReturn(Collections.singletonList(event));
		
		// Mock a Venue Service
		when(venueService.findAll()).thenReturn(Collections.singletonList(venue));
		when(venueService.findById(mockID)).thenReturn(true);
		when(venueService.updateFindById(mockID)).thenReturn(Optional.of(venue));
		when(venueService.count()).thenReturn(mockID);

		// Get the Venue, it should return status 200
		mvc.perform(get("/venues/{mockID}", mockID).accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk()).andExpect(handler().methodName("getVenue"));
		
		// Try to delete the Venue with a fake role, this should fail and return 403
		mvc.perform(delete("/venues/{mockID}", mockID).with(user("Mr. Unauthorised")
			.roles(mockRole)).accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isForbidden());
		
		// Make sure nothing got deleted
		verify(venueService, never()).deleteById(mockID);
	}
	/**
	 * Test qualified data and successfully update data(sensible data)
	 * Venue
	 */
	@Test
	public void testUpdateVenue() throws Exception{
		when(venueService.updateFindById(3L)).thenReturn(Optional.of(venue));
		mvc.perform(post("/venues/update").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "venueTest")
				.param("capacity", "30000")
				.param("postcode", "M22 5RT")
				.param("roadName", "12 Hello Street")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(handler().methodName("updatedVenue"))
				.andExpect(view().name("redirect:/venues"));
		verify(venueService).save(any(Venue.class));
	}
	/**
	 * Test unqualified data and stay on the same page without being able to perform update operation(bad data)
	 * This case is test the postcode should not be null
	 */
	@Test
	public void testUpdateEventBadDataFailed() throws Exception{
		when(venueService.updateFindById(3L)).thenReturn(Optional.of(venue));
		mvc.perform(post("/venues/update").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "venueTest")
				.param("capacity", "30000")
				.param("postcode", "")
				.param("roadName", "12 Hello Street")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(handler().methodName("updatedVenue"))
				.andExpect(view().name("venues/update"));
		verify(venueService,never()).updateVenue(any(Venue.class));
	}
	
	/**
	 * Test no data and stay on the same page without being able to perform update operation(no data)
	 * name,capacity,postcode and roadName are empty
	 */
	@Test
	public void testUpdateEventNoDataFailed() throws Exception{
		when(venueService.updateFindById(3L)).thenReturn(Optional.of(venue));
		mvc.perform(post("/venues/update").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "")
				.param("capacity", "")
				.param("postcode", "")
				.param("roadName", "")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(handler().methodName("updatedVenue"))
				.andExpect(view().name("venues/update"));
		verify(venueService,never()).updateVenue(any(Venue.class));
	}
	/**
	 * Test that users without authority cannot use the update functionality(unauthorised user)
	 * Not Login
	 */
	@Test
	public void testUpdateNoAuth() throws Exception{
		when(venueService.updateFindById(3L)).thenReturn(Optional.of(venue));
		mvc.perform(post("/venues/update")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));
		verify(venueService,never()).updateVenue(any(Venue.class));
	}
	/**
	 * Test a non-existing event and redirect to a page indicating that the event does not exist(no data)
	 * @throws Exception EventNotFoundException
	 */
	@Test
	public void showSingleVenueInfoNotFound() throws Exception {
		when(venueService.updateFindById(3000)).thenReturn(Optional.empty());
		mvc.perform(get("/venues/update").param("venueId", "3000").with(user("Rob").roles(Security.ADMIN_ROLE)))
				.andExpect(status().isNotFound())
				.andExpect(view().name("venues/not_found"));

		verify(venueService,never()).updateVenue(any(Venue.class));
	}
	
	/**
	 * Test an existing event and successfully display it on the update page(sensible data)
	 */
	@Test
	public void showSingleVenueInfoFound() throws Exception {
		when(venueService.updateFindById(1)).thenReturn(Optional.of(venue));
		
		mvc.perform(get("/venues/update").param("venueId", "1").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isOk()).andExpect(view().name("venues/update"))
				.andExpect(handler().methodName("showVenueInfo"));

		verify(venueService,never()).updateVenue(any(Venue.class));
	}
	
	@Test
	public void getNewVenue() throws Exception {
		mvc.perform(get("/venues/new_venue").with(user("Oscar").roles(Security.ORGANIZER_ROLE)).accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk())
		.andExpect(view().name("venues/new_venue"))
		.andExpect(handler().methodName("newVenue"));
	}
	
	@Test
	public void testCreateVenue() throws Exception {
		ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);
		when(venueService.save(any(Venue.class))).then(returnsFirstArg());
		mvc.perform(post("/venues").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "venueTest")
				.param("capacity", "30000")
				.param("postcode", "M22 5RT")
				.param("roadName", "12 Hello Street")
				.accept(MediaType.TEXT_HTML).with(csrf()))
		        .andExpect(status().isFound())
		        .andExpect(handler().methodName("createVenue"))
		        .andExpect(view().name("redirect:/venues"))
		        .andExpect(model().hasNoErrors())
		        .andExpect(content().string(""))
		        .andExpect(flash().attributeExists("ok_message"));
		
		verify(venueService).save(arg.capture());
		assertThat("venueTest", equalTo(arg.getValue().getName()));
	}
	
	@Test
	public void testCreateVenueInvalid() throws Exception {
		mvc.perform(post("/venues").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "")
				.param("capacity", "30000")
				.param("postcode", "M22 5RT")
				.param("roadName", "12 Hello Street")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(handler().methodName("createVenue"))
				.andExpect(view().name("venues/new_venue"))
				.andExpect(model().hasErrors());
		
		verify(venueService, never()).save(any(Venue.class));
	}
	
	@Test
	public void testVenueGeocoding() throws Exception {
		
		// Make a countdown to give the async reasonable time to finish
		CountDownLatch latch = new CountDownLatch(1);

		// Mock the address, in this case, Downing Street
	    when(venue.getPostcode()).thenReturn("SW1A 2AB");
	    when(venue.getRoadName()).thenReturn("Downing St");
	    
	    // Use real calls for getting longitude and latitude
	    doCallRealMethod().when(venue).getLatitude();
	    doCallRealMethod().when(venue).getLongitude();
	    
	    // Use real calls for setting longitude and latitude
	    doCallRealMethod().when(venue).setLatitude(anyDouble());
	    doCallRealMethod().when(venue).setLongitude(anyDouble());
	    
	    // Use real call for set longitude and latitude function
	    doCallRealMethod().when(venue).setLongAndLat();

	    // Perform geocoding
	    venue.setLongAndLat();

	    // Give it 3 seconds to finish async function
	    latch.await(3, TimeUnit.SECONDS);
	    
	    // Make sure Mapbox set the right coordinates from the address
	    assertEquals(venue.getLatitude(), 51.50333875);
	    assertEquals(venue.getLongitude(), -0.127557);
	}
	
	@Test
	public void testVenueServiceGeocoding() throws Exception {
		
		// Make a countdown to give the async reasonable time to finish
		CountDownLatch latch = new CountDownLatch(1);
		
		// Mock the address, in this case, Downing Street
	    when(venue.getPostcode()).thenReturn("SW1A 2AB");
	    when(venue.getRoadName()).thenReturn("Downing St");
	    
	    // Use real calls for getting longitude and latitude
	    doCallRealMethod().when(venue).getLatitude();
	    doCallRealMethod().when(venue).getLongitude();
	    
	    // Use real calls for setting longitude and latitude
	    doCallRealMethod().when(venue).setLatitude(anyDouble());
	    doCallRealMethod().when(venue).setLongitude(anyDouble());
	    
	    // Use real call for set longitude and latitude function
	    doCallRealMethod().when(venue).setLongAndLat();
		
		mvc.perform(post("/venues/update").with(user("Oscar").roles(Security.ORGANIZER_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "downingst")
				.param("capacity", "100")
				.param("postcode", "SW1A 2AB")
				.param("roadName", "Downing St")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(handler().methodName("updatedVenue"))
				.andExpect(view().name("redirect:/venues"));


		// Mock the calling of setLongAndLat from the /venues/update function
	    venue.setLongAndLat();
	    
	    // Mock the returning of the correct venue when we use updateFindById
		when(venueService.updateFindById(3L)).thenReturn(Optional.of(venue));

	    // Give it 3 seconds to finish async function
	    latch.await(3, TimeUnit.SECONDS);
		
	    // Make sure Mapbox set the right coordinates from the address
	    assertEquals(venueService.updateFindById(3L).orElseThrow().getLatitude(), 51.50333875);
	    assertEquals(venueService.updateFindById(3L).orElseThrow().getLongitude(), -0.127557);
		
	}
}

