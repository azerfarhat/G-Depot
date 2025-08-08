package com.GestionDepot.GESTION_DEPOT.Request;

import com.GestionDepot.GESTION_DEPOT.enums.StatutFacture;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FactureStatutUpdateDto {

    @NotNull(message = "Le nouveau statut de la facture ne peut pas être nul.")
    private StatutFacture nouveauStatut;
}