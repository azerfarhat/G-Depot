package com.GestionDepot.GESTION_DEPOT.Request;

import com.GestionDepot.GESTION_DEPOT.enums.RetreiveProductMethode;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProduitUpdateDTO {

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(min = 2, max = 100)
    private String nom;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    private RetreiveProductMethode strategieStock;
}
