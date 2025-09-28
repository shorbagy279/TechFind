package com.example.techdirectory.technician;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TechnicianController {
  private final TechnicianRepository techRepo;
  private final RegionRepository regionRepo;
  private final FieldRepository fieldRepo;
  public TechnicianController(TechnicianRepository t, RegionRepository r, FieldRepository f){ this.techRepo=t; this.regionRepo=r; this.fieldRepo=f; }

  // Public search
  @GetMapping("/public/technicians")
  public List<Technician> search(@RequestParam String governorate, @RequestParam(required=false) String city, @RequestParam(required=false) String field){
    return techRepo.search(governorate, city, field);
  }

  @GetMapping("/public/fields") public List<Field> fields(){ return fieldRepo.findAll(); }
  @GetMapping("/public/regions") public List<Region> regions(){ return regionRepo.findAll(); }

  // Admin CRUD
  @PostMapping("/admin/technicians") public Technician create(@RequestBody Technician t){ return techRepo.save(t); }
  @PutMapping("/admin/technicians/{id}") public Technician update(@PathVariable Long id, @RequestBody Technician t){ t.setId(id); return techRepo.save(t); }
  @DeleteMapping("/admin/technicians/{id}") public void delete(@PathVariable Long id){ techRepo.deleteById(id); }

  @PostMapping("/admin/fields") public Field createField(@RequestBody Field f){ return fieldRepo.save(f); }
  @PostMapping("/admin/regions") public Region createRegion(@RequestBody Region r){ return regionRepo.save(r); }
}
