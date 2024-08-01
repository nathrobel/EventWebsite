package uk.ac.man.cs.eventlite.controllers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.thymeleaf.spring6.expression.Mvc;

import uk.ac.man.cs.eventlite.controllers.HomePageController;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;


@ExtendWith(MockitoExtension.class)
public class HomePageControllerTest {

    @Mock
    private EventService eventService;

    @Mock
    private VenueService venueService;

    @InjectMocks
    private HomePageController homePageController;
    
    @Autowired
    private MockMvc mvc;

    @Test
    public void testHomePageController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(homePageController).build();
        
        mockMvc.perform(get("/home"))
               .andExpect(status().isOk())
               .andExpect(view().name("home/index"));
    }

    @Test
    public void testGettop3Events() {
        // Mock top 3 events
        List<Event> mockEvents = new ArrayList<>();
        // Populate mockEvents with some dummy Event objects
        when(eventService.next3Events()).thenReturn((ArrayList<Event>) mockEvents);

        // Mock top 3 venues
        List<Venue> mockVenues = new ArrayList<>();
        when(venueService.top3popularVenues()).thenReturn(mockVenues);

        Model model = mock(Model.class);
        String viewName = homePageController.gettop3Events(model);

        // Verify that the model contains the expected attributes
        verify(model).addAttribute("home_events", mockEvents);
        verify(model).addAttribute("home_venues", mockVenues);
        assertEquals("home/index", viewName);
    }
    
 

}
