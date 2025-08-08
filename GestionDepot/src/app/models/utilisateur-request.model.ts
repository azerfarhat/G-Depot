// src/app/models/utilisateur-request.model.ts (CORRECTED version)
// This interface should match the structure of your Backend's UtilisateurDto.java
export interface UtilisateurRequestDto {
  nom: string;
  email: string;
  motDePasse: string;
  role: string;

  // These fields correspond to the backend's 'Vehicule' and 'Chauffeur' specific fields.
  // They are optional here because this specific modal (for UtilisateurComponent)
  // will *not* collect them, as per your requirement to handle chauffeurs separately.
  // The backend will apply validation (e.g., 'numeroPermis' required if role is CHAUFFEUR).
  telephone?: string; // Backend assigns if present.
  numeroPermis?: string; // Matched to backend's 'getNumeroPermis'
  marqueVehicule?: string; // Corrected from 'vehiculeMarque' to match backend's 'getMarqueVehicule'
  modeleVehicule?: string; // Corrected from 'vehiculeModele' to match backend's 'getModeleVehicule'
  matriculeVehicule?: string; // Corrected from 'vehiculeMatricule' to match backend's 'getMatriculeVehicule'
}