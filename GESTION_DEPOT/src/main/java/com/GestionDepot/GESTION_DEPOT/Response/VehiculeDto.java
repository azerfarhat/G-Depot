package com.GestionDepot.GESTION_DEPOT.Response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeDto {
    private Long id; // Optionnel, mais bonne pratique
    private String marque;
    private String modele;
    private String matricule;
}