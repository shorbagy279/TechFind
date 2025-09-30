package com.example.techdirectory.technician;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    
    @Query("SELECT DISTINCT t FROM Technician t " +
           "LEFT JOIN t.fields f " +
           "LEFT JOIN t.region r " +
           "WHERE t.active = true " +
           "AND (:governorate IS NULL OR r.governorate = :governorate) " +
           "AND (:city IS NULL OR r.city = :city) " +
           "AND (:fieldName IS NULL OR f.name = :fieldName) " +
           "AND (:minRating IS NULL OR t.averageRating >= :minRating) " +
           "AND (:maxPrice IS NULL OR t.baseServiceFee <= :maxPrice) " +
           "AND (:availableNow IS NULL OR :availableNow = false OR t.available = true) " +
           "AND (:emergencyService IS NULL OR :emergencyService = false OR t.isEmergencyService = true)")
    List<Technician> searchWithFilters(
            @Param("governorate") String governorate,
            @Param("city") String city,
            @Param("fieldName") String fieldName,
            @Param("minRating") Double minRating,
            @Param("maxPrice") Double maxPrice,
            @Param("availableNow") Boolean availableNow,
            @Param("emergencyService") Boolean emergencyService);
    
    @Query("SELECT DISTINCT t FROM Technician t " +
           "WHERE t.active = true " +
           "AND (:minRating IS NULL OR t.averageRating >= :minRating) " +
           "AND (:maxPrice IS NULL OR t.baseServiceFee <= :maxPrice) " +
           "AND (:availableNow IS NULL OR :availableNow = false OR t.available = true) " +
           "AND (:emergencyService IS NULL OR :emergencyService = false OR t.isEmergencyService = true)")
    List<Technician> findAllActiveWithFilters(
            @Param("minRating") Double minRating,
            @Param("maxPrice") Double maxPrice,
            @Param("availableNow") Boolean availableNow,
            @Param("emergencyService") Boolean emergencyService);
}