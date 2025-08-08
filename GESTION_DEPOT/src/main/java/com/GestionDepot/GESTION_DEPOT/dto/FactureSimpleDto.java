package com.GestionDepot.GESTION_DEPOT.dto;

import com.GestionDepot.GESTION_DEPOT.enums.StatutFacture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactureSimpleDto {
    private Long id;
    private String numeroFacture;
    private LocalDate dateFacturation;
    private BigDecimal totalTTC;
    private StatutFacture statut;
}