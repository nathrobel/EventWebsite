package uk.ac.man.cs.eventlite.dao;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;


@Service
public class VenueServiceImpl implements VenueService {
	private final String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoiaDEwIiwiYSI6ImNsdjJieG9nYTBnZHQybG9xOGowNHd6ejMifQ.yM0CR9CYCdzxATuK_qv92w";
	
	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	private final static String DATA = "data/venues.json";

	@Autowired
	private VenueRepository venueRepository;
	
	
	
	
	@Override
	public long count() {

		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {


		return venueRepository.findAllByOrderByNameAsc();
	}

	@Override
	public Venue save(Venue venue) {
		// TODO Auto-generated method stub
		
		return venueRepository.save(venue);
	}

	@Override
	public void updateVenue(Venue v) {
		Optional<Venue> vv = venueRepository.findById(v.getId());
		if(!vv.isEmpty()) {
			Venue v1 = vv.get();
			v1.setCapacity(v.getCapacity());
			v1.setName(v.getName());
			v1.setPostcode(v.getPostcode());
			v1.setRoadName(v.getRoadName());
			
			// call geocoding to update lat/lon and then save
			v1.setLongAndLat();
			mapboxGeocoding(v1);
		}
	}
	
	@Override
	public void mapboxGeocoding(Venue venue) {
		
	    String query = venue.getRoadName() + ", " + venue.getPostcode();    
	    MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
	            .accessToken(MAPBOX_ACCESS_TOKEN)
	            .query(query)
	            .build();

	    mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
	        @Override
	        public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
	            if (response.body() != null) {
	                List<Point> coordinates = response.body().features().stream()
	                    .map(feature -> (Point) feature.geometry())
	                    .collect(Collectors.toList());

	                if (!coordinates.isEmpty()) {
	                    Point location = coordinates.get(0);
	                    venue.setLatitude(location.latitude());
	                    venue.setLongitude(location.longitude());
	                    venueRepository.save(venue);  // Assuming there's a method to save updates to a venue
	                } else {
	                    System.out.println("No coordinates found for this address.");
	                }
	            } else {
	                System.out.println("No response received.");
	            }
	        }

	        @Override
	        public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
	            throwable.printStackTrace();
	        }
	    });
	}


	
	@Override
	public Iterable<Venue> top3popularVenues() {
		Iterable<Venue> venues = venueRepository.findAll();
		venues = (List<Venue>) venues;
		((List<Venue>) venues).sort((v1, v2) -> v2.getEvents().size() - v1.getEvents().size());
		if (((List<Venue>) venues).size() > 3) {
			venues = ((List<Venue>) venues).subList(0, 3);
		}
		return venues;
	}
	
	@Override
	public List<Event> getNextThreeEvents(long id) {
		Optional<Venue> venue = venueRepository.findById(id);
    	if (venue.isPresent()) {
    		Venue v = venue.get();
    		List<Event> events = v.getEvents();
    		events.sort((e1, e2) -> e1.getDate().compareTo(e2.getDate()));
    		if (events.size() > 3) {
    			return events.subList(0, 3);
    		}
    		return events;
    	}
		return null;
	}
	@Override
	public Optional<Venue> updateFindById(long id) {
		// TODO Auto-generated method stub
		return venueRepository.findById(id);
	}
	
	@Override
	public Iterable<Venue> searchByName(String name) {
		return venueRepository.findByNameIgnoreCaseContainingOrderByNameAsc(name);
	}

	@Override
	public boolean findById(long id) {
		return venueRepository.existsById(id);
	}
	
	@Override
	public void deleteById(long id) {
		venueRepository.deleteById(id);
	}
	
	@Override
    public Venue getVenueById(long id) {
    	Optional<Venue> venue = venueRepository.findById(id);
    	if (venue.isPresent()) {
    		return venue.get();
    	
    	}
		return null;
    }

}

