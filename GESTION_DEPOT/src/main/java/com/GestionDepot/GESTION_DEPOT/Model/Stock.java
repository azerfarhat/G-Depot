package com.GestionDepot.GESTION_DEPOT.Model;

import com.GestionDepot.GESTION_DEPOT.enums.StatutStock;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "stocks", indexes = {
        @Index(name = "idx_stock_produit", columnList = "produit_id"),
        @Index(name = "idx_stock_statut", columnList = "statut")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"produit", "depot", "fournisseur", "retraits"})
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "code_barre", nullable = false, unique = true, length = 50)
    private String codeBarre;

    @NotNull
    @Min(0)
    @Column(name = "quantite_produit", nullable = false)
    private Integer quantiteProduit;

    @NotNull
    @Min(0)
    @Column(name = "seuil_min", nullable = false)
    private Integer seuilMin;

    @NotNull
    @Column(name = "date_entree", nullable = false)
    private LocalDate dateEntree;

    @NotNull
    @Column(name = "date_expiration", nullable = false)
    private LocalDate dateExpiration;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "prix_achat", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixAchat;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "prix_vente_htva", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixVenteHTVA;

    @Column(name = "prix_vente_ttc", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixVenteTTC;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private StatutStock statut = StatutStock.DISPONIBLE;

    // --- RELATIONS ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    @JsonBackReference("produit-stocks")
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = false)
    @JsonBackReference("depot-stocks") // Cette référence correspond bien à JsonManagedReference dans Depot
    private Depot depot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    @JsonBackReference("fournisseur-stocks")
    private Fournisseur fournisseur;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("stock-retraits")
    private Set<RetraitStock> retraits = new HashSet<>();


    // --- LOGIQUE MÉTIER ---

    public void calculerEtSetPrixTTC() {
        if (this.prixVenteHTVA != null && this.depot != null && this.depot.getTva() != null) {
            BigDecimal tvaDecimal = this.depot.getTva().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            BigDecimal multiplicateur = BigDecimal.ONE.add(tvaDecimal);
            this.prixVenteTTC = this.prixVenteHTVA.multiply(multiplicateur).setScale(2, RoundingMode.HALF_UP);
        } else {
            this.prixVenteTTC = null;
        }
    }

    public void updateStatut() {
        if (this.dateExpiration != null && LocalDate.now().isAfter(this.dateExpiration)) {
            this.statut = StatutStock.EXPIRE;
        } else if (this.quantiteProduit != null && this.seuilMin != null && this.quantiteProduit <= 0) {
            this.statut = StatutStock.RUPTURE;
        } else if (this.quantiteProduit != null && this.seuilMin != null && this.quantiteProduit <= this.seuilMin) {
            this.statut = StatutStock.ALERTE;
        } else {
            this.statut = StatutStock.DISPONIBLE;
        }
    }

    public void setQuantiteProduit(Integer quantiteProduit) {
        this.quantiteProduit = quantiteProduit;
        updateStatut();
    }

    public void setPrixVenteHTVA(BigDecimal prixVenteHTVA) {
        this.prixVenteHTVA = prixVenteHTVA;
        calculerEtSetPrixTTC();
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
        calculerEtSetPrixTTC();
    }
}