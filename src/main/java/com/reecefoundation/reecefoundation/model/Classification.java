package com.reecefoundation.reecefoundation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name= "classifications")
public class Classification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "classifications")
    private List<Project> projects;

    @OneToMany(mappedBy = "classification")
    @OrderBy("subClassificationName ASC")
    private List<SubClassification> subClassifications;

    @Column(name="classification", length = 50, unique = true)
    private String classificationName;
}
