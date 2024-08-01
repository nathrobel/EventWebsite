package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";
	
	@Autowired
	private EventRepository eventRepository;
	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public List<Event> findAll() {
		return eventRepository.findAllByOrderByDateAscTimeAsc();
	}
	
	@Override
    public List<Event> search(String query){
		LocalDate date = LocalDate.now();
		//list of todays events
		Iterable<Event> todaysSearchResults = eventRepository.findByDateAndNameIgnoreCaseContainingOrderByDateAscNameAsc(date, query);
		//list of upcoming events after today
		Iterable<Event> upcomingSearchResults = eventRepository.findByDateAfterAndNameIgnoreCaseContainingOrderByDateAscNameAsc(date, query);
		
		//concatenates the two lists and returns them
		ArrayList<Event> events = new ArrayList<Event>();
		for (Event e : todaysSearchResults) {
			events.add(e);
		}
		for (Event e : upcomingSearchResults) {
			events.add(e);
		}
		return events;
	}
	
	@Override
	public List<Event> searchPastEvents(String query){
		LocalDate date = LocalDate.now();
		return eventRepository.findByDateBeforeAndNameIgnoreCaseContainingOrderByDateDescNameAsc(date, query);
	}

	@Override
	public Event save(Event event) {
		// TODO Auto-generated method stub
		return eventRepository.save(event);
	}

	
	@Override
	public boolean findById(long id) {
		return eventRepository.existsById(id);
	}
	
	@Override
	public void delete(Event event) {
		eventRepository.delete(event);
	}
	
	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}

	@Override
	public void updateEvent(Event e) {
		Optional<Event> ee = eventRepository.findById(e.getId());
		if(!ee.isEmpty()) {
			Event e1 = ee.get();
			e1.setDate(e.getDate());
			e1.setDescription(e.getDescription());
			e1.setTime(e.getTime());
			e1.setName(e.getName());
		
			e1.setVenue(e.getVenue());
			eventRepository.save(e1);
		}
	}

	@Override
	public Optional<Event> updateFindById(long id) {
		return eventRepository.findById(id);
	}
    
    @Override
    public Event getEventById(long id) {
    	Optional<Event> event = eventRepository.findById(id);
    	if (event.isPresent()) {
    		return event.get();
    	}
		return null;
    }
	
	@Override
	public ArrayList<Event> findUpcomingEvents(){
		LocalDate date = LocalDate.now();
		//list of todays events
		Iterable<Event> todaysEvents = eventRepository.findByDateOrderByTimeAsc(date);
		//list of upcoming events after today
		Iterable<Event> upcomingEvents = eventRepository.findByDateAfterOrderByDateAscTimeAsc(date);
		
		//concatenates the two lists and returns them
		ArrayList<Event> events = new ArrayList<Event>();
		for (Event e : todaysEvents) {
			events.add(e);
		}
		for (Event e : upcomingEvents) {
			events.add(e);
		}
		
		return events;
	}
	
	@Override
	public ArrayList<Event> next3Events() {
		ArrayList<Event> lst= findUpcomingEvents();
		ArrayList<Event> firstThree = new ArrayList<>(lst.subList(0, Math.min(lst.size(), 3)));
		System.out.println(firstThree);
		return firstThree;
		}
    
	@Override
	public List<Event> findPastEvents() {
		LocalDate date = LocalDate.now();
		return eventRepository.findByDateBeforeOrderByDateDescTimeDesc(date);
	}
    
}


