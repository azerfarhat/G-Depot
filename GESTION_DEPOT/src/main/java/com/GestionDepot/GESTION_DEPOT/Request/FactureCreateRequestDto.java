package com.GestionDepot.GESTION_DEPOT.Request;

import java.time.LocalDate;
import lombok.Data;

@Data
public class FactureCreateRequestDto {
    private LocalDate dateEcheance;
}