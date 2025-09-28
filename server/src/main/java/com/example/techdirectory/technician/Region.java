package com.example.techdirectory.technician;
import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Region {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  private String governorate;
  private String city;
  private Double lat; // optional
  private Double lng; // optional
}
