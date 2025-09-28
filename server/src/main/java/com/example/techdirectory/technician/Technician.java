package com.example.techdirectory.technician;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Technician {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  private String fullName;
  private String phone;
  private String email;
  private String summary; // about him
  @ManyToOne private Region region;
  @ManyToMany
  @JoinTable(name="technician_fields",
    joinColumns=@JoinColumn(name="technician_id"),
    inverseJoinColumns=@JoinColumn(name="field_id"))
  private Set<Field> fields;
  private Double lat; // for location support
  private Double lng;
  private Boolean active;
}
