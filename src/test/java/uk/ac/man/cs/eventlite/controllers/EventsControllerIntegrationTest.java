package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

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
	public void getEventNotFound() {
		client.get().uri("/events/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}
	
	@Test
	@DirtiesContext
	public void deleteEventNoUser() {
		int initialEventCount = countRowsInTable("events");

	    client.delete().uri("/events/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
	            .expectHeader().value("Location", containsString("/sign-in"));

	    int finalEventCount = countRowsInTable("events");

	    assertThat(initialEventCount, equalTo(finalEventCount));
	}

	@Test
	@DirtiesContext
	public void deleteEventWithAdmin() {
		int initialCount = countRowsInTable("events");
		String[] tokens = loginForAdmin();

		client.delete().uri("/events/1").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/events"));
		
		int finalCount = countRowsInTable("events");
		
		assertThat(initialCount - 1, equalTo(finalCount));
	}
	
	@Test
	@DirtiesContext
	public void deleteEventWithOrganizer() {
		int initialCount = countRowsInTable("events");
		String[] tokens = loginForOrganizer();

		client.delete().uri("/events/1").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/events"));
		
		int finalCount = countRowsInTable("events");
		
		assertThat(initialCount - 1, equalTo(finalCount));
	}
	
	@Test
	public void deleteEventNotFound() {
		int initialCount = countRowsInTable("events");
		String[] tokens = loginForAdmin();

		client.delete().uri("/events/99").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isNotFound();
		
		int finalCount = countRowsInTable("events");

		assertThat(initialCount, equalTo(finalCount));
	}
	
	// create a new event with valid data
	
	@Test
	public void createEvent() {
		int initialCount = countRowsInTable("events");
		String[] tokens = loginForAdmin();
		
        client.post()
	        .uri("/events")
	        .accept(MediaType.TEXT_HTML)
	        .header(CSRF_HEADER, tokens[0])
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	        .bodyValue("name=coolevent&date=2025-04-24&time=13:27&venue=1&description=cooldescription")
	        .cookie(SESSION_KEY, tokens[1])
	        .exchange()
	        .expectStatus()
	        .isFound()
	        .expectHeader()
			.value("Location", endsWith("/events"));
        
        int newCount = countRowsInTable("events");
        assertThat(newCount - 1, equalTo(initialCount));
	}
	
	// create a new event with invalid data
	
	@Test
	public void createEventBadData() {
		int initialCount = countRowsInTable("events");
		String[] tokens = loginForAdmin();
		
        client.post()
	        .uri("/events")
	        .accept(MediaType.TEXT_HTML)
	        .header(CSRF_HEADER, tokens[0])
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	        .bodyValue("name=&date=1742-04-24&time=25:61&venue=999&description=")
	        .cookie(SESSION_KEY, tokens[1])
	        .exchange()
	        .expectStatus()
	        .isOk();
        
        int newCount = countRowsInTable("events");
        assertThat(newCount, equalTo(initialCount));
	}
	
	@Test
	public void createEventNoData() {
		int initialCount = countRowsInTable("events");
		String[] tokens = loginForAdmin();
		
        client.post()
	        .uri("/events")
	        .accept(MediaType.TEXT_HTML)
	        .header(CSRF_HEADER, tokens[0])
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	        .bodyValue("name=&date=&time=&venue=&description=")
	        .cookie(SESSION_KEY, tokens[1])
	        .exchange()
	        .expectStatus()
	        .isOk();
        
        int newCount = countRowsInTable("events");
        assertThat(newCount, equalTo(initialCount));
	}
	


	private String[] loginForAdmin() {
		String[] tokens = new String[2];

		EntityExchangeResult<String> result = client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get()
				.uri("/events").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
		tokens[0] = getCsrfToken(result.getResponseBody());
		tokens[1] = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		return tokens;
	}
	
	private String[] loginForOrganizer() {
		String[] tokens = new String[2];

		EntityExchangeResult<String> result = client.mutate().filter(basicAuthentication("Oscar", "Sewell")).build().get()
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
