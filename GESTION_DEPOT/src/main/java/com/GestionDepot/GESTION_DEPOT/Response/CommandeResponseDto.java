package com.GestionDepot.GESTION_DEPOT.Response;

import com.GestionDepot.GESTION_DEPOT.enums.StatutCommande;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CommandeResponseDto {
    private Long id;
    private LocalDate dateCommande;
    private StatutCommande statutCommande;
    private BigDecimal totaleCommandeTTC;
    private UtilisateurSimpleDto client; // Utilise le DTO sécurisé
    private List<LigneCommandeResponseDto> lignes; // Liste de DTOs
}