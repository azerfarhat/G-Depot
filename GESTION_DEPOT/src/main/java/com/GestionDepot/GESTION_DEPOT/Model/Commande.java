package com.GestionDepot.GESTION_DEPOT.Model;

import com.GestionDepot.GESTION_DEPOT.enums.StatutCommande;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "commandes")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"lignes", "client", "depot"})
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCommande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommande statutCommande;

    @Column(name = "totale_commande_ttc", precision = 12, scale = 2)
    private BigDecimal totaleCommandeTTC;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference("commande-lignes") // *** Indique la partie "maîtresse" de la relation ***
    private Set<LigneCommande> lignes = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonBackReference("client-commandes") // Si Client a une liste de Commandes
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    @JsonBackReference("depot-commandes") // Si Depot a une liste de Commandes
    private Depot depot;

    public void addLigne(LigneCommande ligne) {
        if (ligne != null) {
            this.lignes.add(ligne);
            ligne.setCommande(this);
        }
    }
}