package com.GestionDepot.GESTION_DEPOT.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "fournisseurs")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "stocks")
@EqualsAndHashCode(of = "id")
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du fournisseur est obligatoire")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "Le nom de la société est obligatoire")
    private String societe;

    @Email(message = "Le format de l'email est invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String telephone;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @NotBlank(message = "Le pays est obligatoire")
    private String pays;

    @NotBlank(message = "Le site web est obligatoire")
    private String siteWeb;

    @JsonIgnore
    @OneToMany(mappedBy = "fournisseur", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("fournisseur-stocks")
    private Set<Stock> stocks = new HashSet<>();
}