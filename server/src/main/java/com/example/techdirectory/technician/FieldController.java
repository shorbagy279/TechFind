package com.example.techdirectory.technician;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FieldController {
    
    private final FieldRepository fieldRepository;
    
    public FieldController(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }
    
    @GetMapping("/public/fields")
    public List<Field> getAllFields() {
        return fieldRepository.findAll();
    }
    
    @PostMapping("/admin/fields")
    public Field createField(@RequestBody FieldRequest request) {
        if (fieldRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Field already exists");
        }
        Field field = Field.builder()
            .name(request.getName())
            .build();
        return fieldRepository.save(field);
    }
    
    @PutMapping("/admin/fields/{id}")
    public Field updateField(@PathVariable Long id, @RequestBody FieldRequest request) {
        Field field = fieldRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Field not found"));
        field.setName(request.getName());
        return fieldRepository.save(field);
    }
    
    @DeleteMapping("/admin/fields/{id}")
    public void deleteField(@PathVariable Long id) {
        fieldRepository.deleteById(id);
    }
    
    public static class FieldRequest {
        private String name;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}