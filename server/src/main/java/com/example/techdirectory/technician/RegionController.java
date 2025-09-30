package com.example.techdirectory.technician;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/regions")
public class RegionController {


private final RegionRepository regionRepository;


public RegionController(RegionRepository regionRepository) {
this.regionRepository = regionRepository;
}


// GET /api/regions
@GetMapping
public List<Region> getAllRegions() {
return regionRepository.findAll();
}


// POST /api/regions
@PostMapping
public ResponseEntity<Region> createRegion(@Valid @RequestBody RegionRequest request) {
Region region = Region.builder()
.governorate(request.getGovernorate())
.city(request.getCity())
.lat(request.getLat())
.lng(request.getLng())
.build();
Region saved = regionRepository.save(region);
return ResponseEntity.created(URI.create("/api/regions/" + saved.getId())).body(saved);
}


// PUT /api/regions/{id}
@PutMapping("/{id}")
public ResponseEntity<Region> updateRegion(@PathVariable Long id, @Valid @RequestBody RegionRequest request) {
Region region = regionRepository.findById(id)
.orElseThrow(() -> new RuntimeException("Region not found"));
region.setGovernorate(request.getGovernorate());
region.setCity(request.getCity());
region.setLat(request.getLat());
region.setLng(request.getLng());
return ResponseEntity.ok(regionRepository.save(region));
}


// DELETE /api/regions/{id}
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteRegion(@PathVariable Long id) {
regionRepository.deleteById(id);
return ResponseEntity.noContent().build();
}


public static class RegionRequest {
private String governorate;
private String city;
private Double lat;
private Double lng;


public String getGovernorate() { return governorate; }
public void setGovernorate(String governorate) { this.governorate = governorate; }
public String getCity() { return city; }
public void setCity(String city) { this.city = city; }
public Double getLat() { return lat; }
public void setLat(Double lat) { this.lat = lat; }
public Double getLng() { return lng; }
public void setLng(Double lng) { this.lng = lng; }
}
}