package com.example.techdirectory.technician;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    
    List<Technician> findByRegion_GovernorateAndRegion_City(String governorate, String city);
    
    @Query("SELECT t FROM Technician t JOIN t.fields f WHERE t.region.governorate = :gov " +
           "AND (:city IS NULL OR t.region.city = :city) " +
           "AND (:fieldName IS NULL OR f.name = :fieldName)")
    List<Technician> search(@Param("gov") String gov, 
                           @Param("city") String city, 
                           @Param("fieldName") String fieldName);
    
    @Query("SELECT t FROM Technician t LEFT JOIN t.fields f WHERE " +
           "(:governorate IS NULL OR t.region.governorate = :governorate) " +
           "AND (:city IS NULL OR t.region.city = :city) " +
           "AND (:fieldName IS NULL OR f.name = :fieldName) " +
           "AND (:minRating IS NULL OR t.averageRating >= :minRating) " +
           "AND (:maxPrice IS NULL OR t.baseServiceFee <= :maxPrice) " +
           "AND (:availableNow IS NULL OR :availableNow = false OR (t.active = true AND t.available = true AND t.availabilityStatus = 'AVAILABLE')) " +
           "AND (:emergencyService IS NULL OR :emergencyService = false OR t.isEmergencyService = true) " +
           "AND t.active = true")
    List<Technician> searchWithFilters(@Param("governorate") String governorate,
                                       @Param("city") String city,
                                       @Param("fieldName") String fieldName,
                                       @Param("minRating") Double minRating,
                                       @Param("maxPrice") Double maxPrice,
                                       @Param("availableNow") Boolean availableNow,
                                       @Param("emergencyService") Boolean emergencyService);
    
    @Query("SELECT t FROM Technician t WHERE " +
           "(:minRating IS NULL OR t.averageRating >= :minRating) " +
           "AND (:maxPrice IS NULL OR t.baseServiceFee <= :maxPrice) " +
           "AND (:availableNow IS NULL OR :availableNow = false OR (t.active = true AND t.available = true AND t.availabilityStatus = 'AVAILABLE')) " +
           "AND (:emergencyService IS NULL OR :emergencyService = false OR t.isEmergencyService = true) " +
           "AND t.active = true")
    List<Technician> findAllActiveWithFilters(@Param("minRating") Double minRating,
                                              @Param("maxPrice") Double maxPrice,
                                              @Param("availableNow") Boolean availableNow,
                                              @Param("emergencyService") Boolean emergencyService);
}