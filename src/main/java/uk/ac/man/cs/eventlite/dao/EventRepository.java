package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventRepository extends CrudRepository<Event, Long>{

	
	public Optional<Event> findById(long id);

	public List<Event> findAllByOrderByDateAscTimeAsc();
	
	//current search results
	public List<Event> findByDateAfterAndNameIgnoreCaseContainingOrderByDateAscNameAsc(LocalDate date, String query);
	
	public List<Event> findByDateAndNameIgnoreCaseContainingOrderByDateAscNameAsc(LocalDate date, String query);
	
	//current all results
	public List<Event> findByDateAfterOrderByDateAscTimeAsc(LocalDate date);
	
	public List<Event> findByDateOrderByTimeAsc(LocalDate date);
	
	//past all results
	public List<Event> findByDateBeforeOrderByDateDescTimeDesc(LocalDate date);
	
	//past search results
	public List<Event> findByDateBeforeAndNameIgnoreCaseContainingOrderByDateDescNameAsc(LocalDate date,String query);
}