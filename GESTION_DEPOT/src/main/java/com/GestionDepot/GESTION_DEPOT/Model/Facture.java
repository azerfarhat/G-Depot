package com.GestionDepot.GESTION_DEPOT.Model;

import com.GestionDepot.GESTION_DEPOT.enums.StatutFacture;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "factures")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"commande", "bonDeSortie", "depot", "lignes"})
@EqualsAndHashCode(of = "id")
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numeroFacture;

    @Column(nullable = false)
    private LocalDate dateFacturation;

    private LocalDate dateEcheance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutFacture statut;

    @Column(name = "total_ht", precision = 10, scale = 2)
    private BigDecimal totalHT;

    @Column(name = "montant_tva", precision = 10, scale = 2)
    private BigDecimal montantTVA;

    @Column(name = "total_ttc", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalTTC;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", referencedColumnName = "id", nullable = true, unique = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_de_sortie_id", nullable = true)
    @JsonIgnore
    private BonDeSortie bonDeSortie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    @JsonIgnore
    private Depot depot;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LigneFacture> lignes = new HashSet<>();
}