package com.example.techdirectory.technician;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class FieldController {
    
    private static final Logger logger = LoggerFactory.getLogger(FieldController.class);
    
    private final FieldRepository fieldRepository;
    
    public FieldController(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }
    
    @GetMapping("/public/technician-fields")
    public ResponseEntity<List<Field>> getAllFields() {
        try {
            List<Field> fields = fieldRepository.findAll();
            logger.debug("Retrieved {} fields", fields.size());
            return ResponseEntity.ok(fields);
        } catch (Exception e) {
            logger.error("Error retrieving fields", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/admin/fields")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createField(@Valid @RequestBody FieldRequest request) {
        try {
            // Check if field already exists
            if (fieldRepository.findByName(request.getName().trim()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Field already exists with this name"));
            }
            
            Field field = Field.builder()
                .name(request.getName().trim())
                .build();
            
            field = fieldRepository.save(field);
            
            logger.info("Field created: ID={}, Name={}", field.getId(), field.getName());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(field);
            
        } catch (Exception e) {
            logger.error("Error creating field", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to create field"));
        }
    }
    
    @PutMapping("/admin/fields/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateField(
            @PathVariable Long id, 
            @Valid @RequestBody FieldRequest request) {
        try {
            Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Field not found"));
            
            // Check if name is already taken by another field
            String newName = request.getName().trim();
            fieldRepository.findByName(newName).ifPresent(existingField -> {
                if (!existingField.getId().equals(id)) {
                    throw new RuntimeException("Another field already has this name");
                }
            });
            
            field.setName(newName);
            field = fieldRepository.save(field);
            
            logger.info("Field updated: ID={}, Name={}", field.getId(), field.getName());
            
            return ResponseEntity.ok(field);
            
        } catch (RuntimeException e) {
            logger.warn("Field update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating field: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to update field"));
        }
    }
    
    @DeleteMapping("/admin/fields/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteField(@PathVariable Long id) {
        try {
            if (!fieldRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Field not found"));
            }
            
            // TODO: Check if field is in use by any technicians
            // If so, prevent deletion or handle gracefully
            
            fieldRepository.deleteById(id);
            
            logger.info("Field deleted: ID={}", id);
            
            return ResponseEntity.ok(new SuccessResponse("Field deleted successfully"));
            
        } catch (Exception e) {
            logger.error("Error deleting field: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to delete field"));
        }
    }
    
    public static class FieldRequest {
        @NotBlank(message = "Field name is required")
        @Size(min = 2, max = 100, message = "Field name must be between 2 and 100 characters")
        private String name;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
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