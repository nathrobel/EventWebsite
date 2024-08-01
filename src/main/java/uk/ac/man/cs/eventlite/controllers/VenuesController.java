package uk.ac.man.cs.eventlite.controllers;

import com.mapbox.geojson.Point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;

import jakarta.validation.Valid;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.man.cs.eventlite.config.Container;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {
	private final String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoiaDEwIiwiYSI6ImNsdjJieG9nYTBnZHQybG9xOGowNHd6ejMifQ.yM0CR9CYCdzxATuK_qv92w";
	private final static Logger log = LoggerFactory.getLogger(VenuesController.class);
	@Autowired
	private VenueService venueService;
	@ExceptionHandler(VenueNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String VenueNotFoundHandler(VenueNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());
		return "venues/not_found";
	}
	

	@GetMapping("/{id}")
	public String getVenue(@PathVariable("id") long id, Model model) {
		if(!venueService.findById(id)){
			throw new VenueNotFoundException(id);
		}
	
		Venue venue = venueService.getVenueById(id);
		model.addAttribute("venue", venue);
		return "venues/seperate_venuePage";
	}
	
	@GetMapping
	public String getAllVenues(Model model) {
		model.addAttribute("venues", venueService.findAll());
		return "venues/index";
	}
    @GetMapping("/new_venue")
    public String newVenue(Model model) {
        if (!model.containsAttribute("venue")) {
            model.addAttribute("venue", new Venue());
        }

        return "venues/new_venue";
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String createVenue(@Valid @ModelAttribute("venue") Venue venue, BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
        if (errors.hasErrors()) {
            model.addAttribute("venue", venue);
            return "venues/new_venue";
        }
        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken(MAPBOX_ACCESS_TOKEN)
				.query(venue.getRoadName() + " " + venue.getPostcode())
				.build();
			mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
				@Override
				public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

					assert response.body() != null;
					List<CarmenFeature> results = response.body().features();
				
					if (results.size() > 0) {
						// Log the first results Point.
						Point firstResultPoint = results.get(0).center();
						assert firstResultPoint != null;
						log.info("onResponse: " + firstResultPoint.toString());
						venue.setLatitude(firstResultPoint.latitude());
						venue.setLongitude(firstResultPoint.longitude());
						venueService.save(venue);
						redirectAttrs.addFlashAttribute("ok_message", "New venue added.");
					} else {
						// No result for your request were found.
						log.error("onResponse: No result found");
					}
				}
				@Override
				public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
					throwable.printStackTrace();
				}
			});
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return "redirect:/venues";
    }

    @GetMapping("/update")
	public String showVenueInfo(@RequestParam("venueId") long venueId,Model m) {
		Optional<Venue> uvenue = venueService.updateFindById(venueId);
		if(!uvenue.isEmpty()) {
			m.addAttribute("venue",uvenue.get());
			return "venues/update";
		}
		throw new VenueNotFoundException(venueId);
	}
	
	@PostMapping("/update")
    public String updatedVenue(@Valid @ModelAttribute Venue v, BindingResult result,RedirectAttributes redirectAttrs,Model m) {
		if (result.hasErrors()) {
            return "venues/update";
        }
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken(MAPBOX_ACCESS_TOKEN)
				.query(v.getRoadName() + " " + v.getPostcode())
				.build();
			mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
				@Override
				public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

					assert response.body() != null;
					List<CarmenFeature> results = response.body().features();
				
					if (results.size() > 0) {
						// Log the first results Point.
						Point firstResultPoint = results.get(0).center();
						assert firstResultPoint != null;
						log.info("onResponse: " + firstResultPoint.toString());
						v.setLatitude(firstResultPoint.latitude());
						v.setLongitude(firstResultPoint.longitude());
						venueService.save(v);
						redirectAttrs.addFlashAttribute("ok_message", "New venue updated.");
					} else {
						// No result for your request were found.
						log.error("onResponse: No result found");
					}
				}
				@Override
				public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
					throwable.printStackTrace();
				}
			});
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return "redirect:/venues";
    }

	@GetMapping("/search")
	public String searchVenues(@RequestParam("query") String query, Model model) {
		//updating the venue list in the model
		//uses the input query to search for venues via service via repository
		Iterable<Venue> venues = venueService.searchByName(query);
		model.addAttribute("venues", venues);
		return "venues/index";
	}
	
	@DeleteMapping("/{id}")
	public String deleteVenue(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		Optional<Venue> venueOptional = venueService.updateFindById(id);
		if(venueOptional.isEmpty()) {
			throw new VenueNotFoundException(id);
		}
		
		Venue venue = venueOptional.get();
		List<Event> events = venue.getEvents();
		
		if(!events.isEmpty()) {
			redirectAttrs.addFlashAttribute("error_message", "Cannot delete venue with more than one event.");
			return "redirect:/venues";
		}
		
		venueService.deleteById(id);
		redirectAttrs.addFlashAttribute("ok_message", "Delete venue successfully.");
		
		return "redirect:/venues";
	}

}

