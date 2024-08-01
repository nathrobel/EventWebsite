package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;

import uk.ac.man.cs.eventlite.entities.Venue;

import uk.ac.man.cs.eventlite.entities.Event;

@Configuration
@Profile("default")
public class InitialDataLoader {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			if (venueService.count() > 0) {
				log.info("Database already populated with venues. Skipping venue initialization.");
			} else {
				// Build and save initial venues here.
				Venue new_venue = new Venue();
				new_venue.setName("Sexy Fish");
				new_venue.setCapacity(100);
				new_venue.setPostcode("M3 3AP");
				new_venue.setRoadName("1 The Avenue");
				new_venue.setLongAndLat();
				Thread.sleep(1000);
				venueService.save(new_venue);

				Venue new_venue2 = new Venue();
				new_venue2.setName("Kilburn");
				new_venue2.setCapacity(300);
				new_venue2.setPostcode("M13 9PL");
				new_venue2.setRoadName("Oxford Rd");
				new_venue2.setLongAndLat();
				Thread.sleep(1000);
				venueService.save(new_venue2);
			}

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
			} else {
				// Build and save initial events here.
				log.info("successful");
				Event e1 = new Event();
				e1.setName("Event-1");
				e1.setTime(LocalTime.of(22, 55));
				e1.setDate(LocalDate.of(2024, 2, 16));
				Venue venue_1 = venueService.findAll().iterator().next();
				e1.setVenue(venue_1);				
				eventService.save(e1);
				Event e2 = new Event();
				e2.setName("Event-2");
				e2.setTime(LocalTime.of(10, 55));
				e2.setDate(LocalDate.of(2025, 7, 16));
				e2.setVenue(venue_1);				
				eventService.save(e2);
				Event e3 = new Event();
				e3.setName("Event-3");
				e3.setTime(LocalTime.of(4, 55));
				e3.setDate(LocalDate.of(1988, 12, 16));
				e3.setVenue(venue_1);				
				eventService.save(e3);
				
				//random data to test the sorting
				Event e4 = new Event();
				e4.setName("Event-4");
				e4.setTime(LocalTime.of(4, 55));
				e4.setDate(LocalDate.of(1988, 12, 16));
				e4.setVenue(venue_1);				
				eventService.save(e4);
				
				Event e5 = new Event();
				e5.setName("Event-4");
				e5.setTime(LocalTime.of(1, 23));
				e5.setDate(LocalDate.of(2000, 01, 01));
				e5.setVenue(venue_1);				
				eventService.save(e5);
				
				Event e6 = new Event();
				e6.setName("Event-4");
				e6.setTime(LocalTime.of(1, 24));
				e6.setDate(LocalDate.of(2000, 01, 01));
				e6.setVenue(venue_1);				
				eventService.save(e6);
				
				Event e7 = new Event();
				e7.setName("Event-4");
				e7.setTime(LocalTime.of(4, 50));
				e7.setDate(LocalDate.of(1988, 12, 16));
				e7.setVenue(venue_1);				
				eventService.save(e7);

				Event e8 = new Event();
				e8.setName("Event Apple");
				e8.setDate(LocalDate.of(2024, 07, 12));
				e8.setVenue(venue_1);				
				eventService.save(e8);
				
				Event e9 = new Event();
				e9.setName("Event Alpha");
				e9.setTime(LocalTime.of(12, 30));
				e9.setDate(LocalDate.of(2024, 07, 11));
				e9.setVenue(venue_1);				
				eventService.save(e9);
				
				Event e10 = new Event();
				e10.setName("Event Beta");
				e10.setTime(LocalTime.of(10, 00));
				e10.setDate(LocalDate.of(2024, 07, 11));
				e10.setVenue(venue_1);				
				eventService.save(e10);
				
				Event e11 = new Event();
				e11.setName("Event Former");
				e11.setTime(LocalTime.of(11, 00));
				e11.setDate(LocalDate.of(2024, 01, 11));
				e11.setVenue(venue_1);				
				eventService.save(e11);
				
				Event e12 = new Event();
				e12.setName("Event Previous");
				e12.setTime(LocalTime.of(18, 30));
				e12.setDate(LocalDate.of(2024, 01, 11));
				e12.setVenue(venue_1);				
				eventService.save(e12);
				
				Event e13 = new Event();
				e13.setName("Event Past");
				e13.setTime(LocalTime.of(17, 00));
				e13.setDate(LocalDate.of(2024, 01, 10));
				e13.setVenue(venue_1);				
				eventService.save(e13);
				
				Event e14 = new Event();
				e14.setName("Event Present");
				e14.setTime(LocalTime.of(12, 00));
				e14.setDate(LocalDate.of(2024, 03, 05));
				e14.setVenue(venue_1);				
				eventService.save(e14);

			}
			
		};
	}
}
