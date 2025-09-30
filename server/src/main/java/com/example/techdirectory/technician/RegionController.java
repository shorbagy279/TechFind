package com.example.techdirectory.technician;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class RegionController {
    
    private final RegionRepository regionRepository;
    
    public RegionController(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }
    
    @GetMapping("/regions")
    public List<Region> getAllRegions() {
        return regionRepository.findAll();
    }
    
    @PostMapping("/admin/regions")
    public Region createRegion(@RequestBody RegionRequest request) {
        Region region = Region.builder()
            .governorate(request.getGovernorate())
            .city(request.getCity())
            .lat(request.getLat())
            .lng(request.getLng())
            .build();
        return regionRepository.save(region);
    }
    
    @PutMapping("/admin/regions/{id}")
    public Region updateRegion(@PathVariable Long id, @RequestBody RegionRequest request) {
        Region region = regionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Region not found"));
        region.setGovernorate(request.getGovernorate());
        region.setCity(request.getCity());
        region.setLat(request.getLat());
        region.setLng(request.getLng());
        return regionRepository.save(region);
    }
    
    @DeleteMapping("/admin/regions/{id}")
    public void deleteRegion(@PathVariable Long id) {
        regionRepository.deleteById(id);
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