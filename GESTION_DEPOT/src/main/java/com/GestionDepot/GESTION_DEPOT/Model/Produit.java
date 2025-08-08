package com.GestionDepot.GESTION_DEPOT.Model;

import com.GestionDepot.GESTION_DEPOT.enums.RetreiveProductMethode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Ajout d'un constructeur avec tous les arguments
@ToString(exclude = "stocks")
@EqualsAndHashCode(of = "id")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 2, max = 100)
    @Column(unique = true)
    private String nom;

    @NotBlank(message = "La description du produit est obligatoire")
    @Column(columnDefinition = "TEXT")
    private String description;

    private int stockMinimum;
    private String categorie;

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("produit-stocks")
    private Set<Stock> stocks = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private RetreiveProductMethode strategieStock;
}