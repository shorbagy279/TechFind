package com.example.techdirectory.technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TechnicianRepository extends JpaRepository<Technician,Long> {
  List<Technician> findByRegion_GovernorateAndRegion_City(String governorate, String city);
  @Query("select t from Technician t join t.fields f where t.region.governorate = :gov and (:city is null or t.region.city = :city) and (:fieldName is null or f.name = :fieldName)")
  List<Technician> search(String gov, String city, String fieldName);
}
