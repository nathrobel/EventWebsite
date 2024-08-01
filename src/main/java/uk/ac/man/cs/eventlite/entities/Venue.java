package uk.ac.man.cs.eventlite.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.mapbox.geojson.Point;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import uk.ac.man.cs.eventlite.config.Container;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Entity
@Table(name="venue")
public class Venue {
	@Id
	@GeneratedValue
	private long id;
	@NotBlank(message = "Name is required")
	@Size(max = 255, message = "Name must be less than 256 characters")
	private String name;
	@Positive(message = "Capacity must be a positive integer")
	private int capacity;
	@NotBlank(message = "Postcode is required")
	private String postcode;
	@NotBlank(message = "Road name is required")
	@Size(max = 299, message = "Road name must be less than 256 characters")
	private String roadName;

	@OneToMany(mappedBy = "venue")
	private List<Event> events;

	private double latitude;
	private double longitude;

	private String address;
	private final String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoiaDEwIiwiYSI6ImNsdjJieG9nYTBnZHQybG9xOGowNHd6ejMifQ.yM0CR9CYCdzxATuK_qv92w";
	private final static Logger log = LoggerFactory.getLogger(Venue.class);

	public Venue() {
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getRoadName() {
		return roadName;
	}

	public void setRoadName(String roadName) {
		this.roadName = roadName;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}


	@JsonIgnore
	public List<Event> getEvents(){
		return events;
	}
	
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	
	public int getNumberOfEvents(List<Event> events) {
		return events.size();
		}

	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	
	public void setLongAndLat() {
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
    			.accessToken(MAPBOX_ACCESS_TOKEN)
    			.query(getRoadName() + " " + getPostcode())
    			.build();
    		try {
    			Thread.sleep(1000L);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>(){
			@Override
			public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
				List<CarmenFeature> results = response.body().features();
				if(results.size()>0) {
					Point firstResultPoint = results.get(0).center();
					log.info("onResponse:"+firstResultPoint.toString());
					setLatitude(firstResultPoint.latitude());
					setLongitude(firstResultPoint.longitude());
				}else {
					log.error("onResponse: No result found");
				}
			}
			@Override
			public void onFailure(Call<GeocodingResponse> call, Throwable t) {
				t.printStackTrace();
			}
		});

	}
}
