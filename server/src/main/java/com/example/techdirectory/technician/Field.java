package com.example.techdirectory.technician;
import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Field {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(unique=true) private String name; // e.g., Plumbing, AC, Electrical
}
