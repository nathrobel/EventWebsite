package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public List<Event> findAll();
	
	public List<Event> findUpcomingEvents();
	
	public List<Event> findPastEvents();
	
	public Event save(Event event);

	public List<Event> search(String query);
	
	public List<Event> searchPastEvents(String query);
	
	public boolean findById(long id);
	
	public void delete(Event event);
	
	public void deleteById(long id);

//	public void deleteAll();

	public Optional<Event> updateFindById(long id);
	
	public void updateEvent(Event e);
	
	public Event getEventById(long id);
	
	public ArrayList<Event> next3Events();


	

}