package com.GestionDepot.GESTION_DEPOT.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "localisations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Localisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double lat;
    private double lng;
    private String type; // 'Client', 'Dépôt', etc.

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Utilisateur client; // Peut être null si type != 'Client'
}