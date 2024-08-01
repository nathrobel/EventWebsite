package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueRepository extends CrudRepository<Venue, Long>{
//	
//	public long count();
//	
	public Iterable<Venue> findAllByOrderByNameAsc();
//	
	//search by name
	public Iterable<Venue> findByNameIgnoreCaseContainingOrderByNameAsc(String name);
//	public void save();

	public Optional<Venue> findById(long id);
	
	
}
