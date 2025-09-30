package com.example.techdirectory.technician;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTechnicianIdOrderByCreatedAtDesc(Long technicianId);
    List<Review> findByUserId(Long userId);
    Double findAverageRatingByTechnicianId(Long technicianId);
}