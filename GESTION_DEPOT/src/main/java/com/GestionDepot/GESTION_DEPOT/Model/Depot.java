package com.GestionDepot.GESTION_DEPOT.Model;

import com.fasterxml.jackson.annotation.JsonIgnore; // <-- IMPORT THIS
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "depots")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"factures", "responsable", "stocks"})
@EqualsAndHashCode(of = "id")
public class Depot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(name = "adresse", columnDefinition = "TEXT")
    private String adresse;

    @Column(length = 100)
    private String ville;

    @Column(length = 10)
    private String codePostal;

    @Column(length = 100)
    private String zone;

    @Column(name = "telephone", length = 50)
    private String telephone;

    @Column(length = 255)
    private String email;

    @OneToMany(mappedBy = "depot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore // <-- ADD THIS ANNOTATION HERE
    private Set<Facture> factures = new HashSet<>();

    @Column(name = "tva", nullable = false, precision = 5, scale = 2)
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal tva = BigDecimal.valueOf(19);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Utilisateur responsable;

    @OneToMany(mappedBy = "depot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore // <-- ADD THIS ANNOTATION HERE
    private Set<Stock> stocks = new HashSet<>();

    @OneToMany(mappedBy = "depot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("depot-utilisateurs") // Correspond à @JsonBackReference dans Utilisateur
    private Set<Utilisateur> utilisateurs = new HashSet<>();
}