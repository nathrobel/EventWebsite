package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$._links.self.href")
				.value(endsWith("/api/venues")).jsonPath("$._embedded.venues.length()").value(equalTo(3));
	}

	@Test
	public void getVenueNotFound() {
		client.get().uri("/venues/99").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound()
				.expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.error")
				.value(containsString("venue 99")).jsonPath("$.id").isEqualTo(99);
	}
	
	@Test
	public void deleteVenueNoUser() {
		int currentRows = countRowsInTable("venue");

		client.delete().uri("/api/venues/1").accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
				.isUnauthorized();

		// Check nothing is removed from the database.
		assertThat(currentRows, equalTo(countRowsInTable("venue")));
	}

	@Test
	public void deleteVenueBadUser() {
		int currentRows = countRowsInTable("venue");

		client.mutate().filter(basicAuthentication("Bad", "Person")).build().delete().uri("/api/venues/1")
				.accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isUnauthorized();

		// Check nothing is removed from the database.
		assertThat(currentRows, equalTo(countRowsInTable("venue")));
	}
	
//	@Test
//	@DirtiesContext
//	public void deleteVenueWithUser() {
//		Venue venue = new Venue();
//	    venue.setId(1);
//	    venue.setName("Venue");
//	    List<Event> events = new ArrayList<>();
//	    venue.setEvents(events);
//		
//		int currentRows = countRowsInTable("venue");
//
//		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().delete().uri("/api/venues/1")
//				.accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNoContent().expectBody().isEmpty();
//
//		// Check that one row is removed from the database.
//		assertThat(currentRows - 1, equalTo(countRowsInTable("venue")));
//	}

	@Test
	public void deleteVenueNotFound() {
		int currentRows = countRowsInTable("venue");

		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().delete().uri("/api/venues/99")
				.accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound().expectBody()
				.jsonPath("$.error").value(containsString("Not Found"));

		// Check nothing is removed from the database.
		assertThat(currentRows, equalTo(countRowsInTable("venue")));
	}


}
