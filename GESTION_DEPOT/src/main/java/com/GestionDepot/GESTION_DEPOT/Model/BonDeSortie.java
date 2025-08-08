package com.GestionDepot.GESTION_DEPOT.Model;

import com.GestionDepot.GESTION_DEPOT.enums.StatutBonDeSortie;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bons_de_sortie")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"commandeOrigine", "chauffeur", "lignes", "factures"})
@EqualsAndHashCode(of = "id")
public class    BonDeSortie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroBDS;
    private LocalDate dateSortie;

    @Enumerated(EnumType.STRING)
    @Column(length = 50) // <--- AJOUTEZ OU MODIFIEZ CETTE LIGNE
    private StatutBonDeSortie statut;

    private BigDecimal valeurTotaleInitialeTTC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chauffeur_id")
    private Utilisateur chauffeur;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_origine_id")
    private Commande commandeOrigine;

    @OneToMany(mappedBy = "bonDeSortie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LigneBonDeSortie> lignes = new HashSet<>();

    @OneToMany(mappedBy = "bonDeSortie")
    private Set<Facture> factures = new HashSet<>();
}