package com.GestionDepot.GESTION_DEPOT.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La marque est obligatoire.")
    @Size(max = 50)
    private String marque;

    @NotBlank(message = "Le modèle est obligatoire.")
    @Size(max = 50)
    private String modele;

    @NotBlank(message = "La matricule est obligatoire.")
    @Size(max = 20)
    @Column(unique = true, nullable = false)
    private String matricule;
}