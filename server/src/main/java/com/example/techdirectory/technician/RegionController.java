package com.example.techdirectory.technician;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class RegionController {
    
    private static final Logger logger = LoggerFactory.getLogger(RegionController.class);
    
    private final RegionRepository regionRepository;
    
    public RegionController(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }
    
    @GetMapping("/regions")
    public ResponseEntity<?> getAllRegions() {
        try {
            List<Region> regions = regionRepository.findAll();
            logger.debug("Retrieved {} regions", regions.size());
            return ResponseEntity.ok(regions);
        } catch (Exception e) {
            logger.error("Error retrieving regions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to retrieve regions"));
        }
    }
    
    @PostMapping("/admin/regions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRegion(@Valid @RequestBody RegionRequest request) {
        try {
            Region region = Region.builder()
                .governorate(request.getGovernorate().trim())
                .city(request.getCity() != null ? request.getCity().trim() : null)
                .lat(request.getLat())
                .lng(request.getLng())
                .build();
            
            region = regionRepository.save(region);
            
            logger.info("Region created: ID={}, Governorate={}, City={}", 
                region.getId(), region.getGovernorate(), region.getCity());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(region);
            
        } catch (Exception e) {
            logger.error("Error creating region", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to create region"));
        }
    }
    
    @PutMapping("/admin/regions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRegion(
            @PathVariable Long id, 
            @Valid @RequestBody RegionRequest request) {
        try {
            Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found"));
            
            if (request.getGovernorate() != null) {
                region.setGovernorate(request.getGovernorate().trim());
            }
            if (request.getCity() != null) {
                region.setCity(request.getCity().trim());
            }
            if (request.getLat() != null) {
                region.setLat(request.getLat());
            }
            if (request.getLng() != null) {
                region.setLng(request.getLng());
            }
            
            region = regionRepository.save(region);
            
            logger.info("Region updated: ID={}", id);
            
            return ResponseEntity.ok(region);
            
        } catch (RuntimeException e) {
            logger.warn("Region not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Region not found"));
        } catch (Exception e) {
            logger.error("Error updating region: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to update region"));
        }
    }
    
    @DeleteMapping("/admin/regions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRegion(@PathVariable Long id) {
        try {
            if (!regionRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Region not found"));
            }
            
            // TODO: Check if region is in use by any technicians
            // If so, prevent deletion or handle gracefully
            
            regionRepository.deleteById(id);
            
            logger.info("Region deleted: ID={}", id);
            
            return ResponseEntity.ok(new SuccessResponse("Region deleted successfully"));
            
        } catch (Exception e) {
            logger.error("Error deleting region: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to delete region"));
        }
    }
    
    public static class RegionRequest {
        @NotBlank(message = "Governorate is required")
        @Size(min = 2, max = 100, message = "Governorate must be between 2 and 100 characters")
        private String governorate;
        
        @Size(max = 100, message = "City name too long")
        private String city;
        
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        private Double lat;
        
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
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
    
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
    }
    
    public static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
    }
}