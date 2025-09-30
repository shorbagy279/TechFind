package com.example.techdirectory.technician;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;
    
    private Long userId;
    private String userName;
    
    @Column(nullable = false)
    private Integer rating; // 1-5
    
    @Column(length = 2000)
    private String comment;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    private Boolean verified; // If the user actually hired this technician
}