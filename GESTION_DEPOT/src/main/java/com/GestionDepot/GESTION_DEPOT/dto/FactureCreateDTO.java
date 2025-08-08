package com.GestionDepot.GESTION_DEPOT.dto;

import com.GestionDepot.GESTION_DEPOT.Request.LigneFactureCreateDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class FactureCreateDTO {
    @NotNull(message = "L'ID du bon de sortie ne peut pas être nul.")
    private Long bonDeSortieId;

    @Valid
    @NotEmpty(message = "La facture doit contenir au moins une ligne.")
    private List<LigneFactureCreateDTO> lignes;
}