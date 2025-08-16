// src/app/models/utilisateur-request.model.ts (CORRECTED version)
// This interface should match the structure of your Backend's UtilisateurDto.java
export interface UtilisateurRequestDto {
  nom: string;
  email: string;
  motDePasse: string;
  role: string;
  depotId?: number; // Rendu optionnel pour permettre undefined

  // Champs optionnels pour chauffeur
  telephone?: string;
  numeroPermis?: string;
  marqueVehicule?: string;
  modeleVehicule?: string;
  matriculeVehicule?: string;
}