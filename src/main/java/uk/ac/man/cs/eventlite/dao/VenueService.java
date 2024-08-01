package uk.ac.man.cs.eventlite.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.PathVariable;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService {
	
	public long count();

	public Iterable<Venue> findAll();
	
	public Venue save(Venue venue);
	
	public void updateVenue(Venue v);
	
	public Optional<Venue> updateFindById(long id);
	
	public Iterable<Venue> searchByName(String name);

	public boolean findById(long id);
	
	public void deleteById(long id);
	
	public Venue getVenueById(long id);
	
	public Iterable<Venue> top3popularVenues();
	
	public List<Event> getNextThreeEvents(long id);

	public void mapboxGeocoding(Venue venue);
	
	
	
	

}
