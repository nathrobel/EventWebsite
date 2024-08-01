package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static String CSRF_HEADER = "X-CSRF-TOKEN";
	private static String SESSION_KEY = "JSESSIONID";
	
	@LocalServerPort
	private int port;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}
	


	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

	@Test
	public void getVenueNotFound() {
		client.get().uri("/venues/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}
	
	@Test
	@DirtiesContext
	public void deleteVenueNoUser() {
		int initialEventCount = countRowsInTable("venue");

	    client.delete().uri("/venues/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
	            .expectHeader().value("Location", containsString("/sign-in"));

	    int finalEventCount = countRowsInTable("venue");

	    assertThat(initialEventCount, equalTo(finalEventCount));
	}

	@Test
	@DirtiesContext
	public void deleteVenueContainsNoEvent() {
		int initialCount = countRowsInTable("venue");
		String[] tokens = login();

		client.delete().uri("/venues/1").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/venues"));
		
		int finalCount = countRowsInTable("venue");
		
		assertThat(initialCount - 1, equalTo(finalCount));
	}
	
//	@Test
//	@DirtiesContext
//	public void deleteVenueContainsEvents() {
//		String[] tokens = login();
//		
//		Venue venue = new Venue();
//	    venue.setId(1);
//	    List<Event> events = new ArrayList<>();
//	    events.add(new Event());
//	    venue.setEvents(events);
//		
//		int initialCount = countRowsInTable("venue");
//
//		client.delete().uri("/venues/1").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
//				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().is3xxRedirection().expectHeader()
//				.value("Location", endsWith("/venues"));
//		
//		int finalCount = countRowsInTable("venue");
//		
//		assertThat(initialCount, equalTo(finalCount));
//	}

	
	@Test
	public void deleteEventNotFound() {
		int initialCount = countRowsInTable("venue");
		String[] tokens = login();

		client.delete().uri("/venues/99").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isNotFound();
		
		int finalCount = countRowsInTable("venue");

		assertThat(initialCount, equalTo(finalCount));
	}
	
	

	// create a new venue with valid data
	
	@Test
	public void createVenue() {
		int initialCount = countRowsInTable("venue");
		String[] tokens = login();
		
        client.post()
	        .uri("/venues")    
	        .accept(MediaType.TEXT_HTML)
	        .header(CSRF_HEADER, tokens[0])
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	        .bodyValue("name=Prime Ministers House&capacity=100&postcode=SW1A 2AB&roadName=Downing Street")
	        .cookie(SESSION_KEY, tokens[1])
	        .exchange()
	        .expectStatus()
	        .isFound()
	        .expectHeader()
			.value("Location", endsWith("/venues"));
        
        int newCount = countRowsInTable("venue");
        assertThat(newCount -1 ,equalTo(initialCount));


	}
	
	// create a new Venue with invalid data
	
	@Test
	public void createVenueBadData() {
		int initialCount = countRowsInTable("venue");
		String[] tokens = login();
		
        client.post()
	        .uri("/venues")    
	        .accept(MediaType.TEXT_HTML)
	        .header(CSRF_HEADER, tokens[0])
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	        .bodyValue("name=&capacity=-69&postcode=&roadName=")
	        .cookie(SESSION_KEY, tokens[1])
	        .exchange()
	        .expectStatus()
	        .isOk();
        
        int newCount = countRowsInTable("venue");
        assertThat(newCount, equalTo(initialCount));
	}
	@Test
	public void createVenueNoData() {
		int initialCount = countRowsInTable("venue");
		String[] tokens = login();
		
        client.post()
	        .uri("/venues")    
	        .accept(MediaType.TEXT_HTML)
	        .header(CSRF_HEADER, tokens[0])
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	        .bodyValue("name=&capacity=&postcode=&roadName=")
	        .cookie(SESSION_KEY, tokens[1])
	        .exchange()
	        .expectStatus()
	        .isOk();
        
        int newCount = countRowsInTable("venue");
        assertThat(newCount, equalTo(initialCount));
	}




	private String[] login() {
		String[] tokens = new String[2];

		EntityExchangeResult<String> result = client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get()
				.uri("/events").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
		tokens[0] = getCsrfToken(result.getResponseBody());
		tokens[1] = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		return tokens;
	}
	
	private String getCsrfToken(String body) {
		Matcher matcher = CSRF.matcher(body);

		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}


}
