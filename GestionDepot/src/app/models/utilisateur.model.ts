// src/app/models/utilisateur.model.ts
// (Votre code existant - Je n'y touche pas, juste pour référence)

export interface Utilisateur {
  id: number;
  nom: string;
  email: string;
  role: string; // This will be the string representation of RoleUtilisateur (e.g., "ADMIN")
  telephone?: string;
  numeroPermis?: string;
  vehicule?: { // Le véhicule est un objet imbriqué
    id: number;
    marque: string;
    modele: string;
    matricule: string;
  };
  depot?: { id: number; nom: string; ville?: string }; // Ajouté pour accès au dépôt
}

export interface UtilisateurSimpleDto {
  id: number;
  nom: string;
  email: string;
  telephone?: string;
  numeroPermis?: string;
  vehicule?: VehiculeDto; // Utilisera le nouveau VehiculeDto
  role: string; // IMPORTANT: Ensure this is 'string' to match backend enum name
  depot?: { id: number; nom: string; ville?: string }; // Ajouté pour affichage du dépôt
}

export interface VehiculeDto {
  id?: number; // Optionnel car peut ne pas être toujours envoyé
  marque: string;
  modele: string;
  matricule: string;
}
export interface UtilisateurSimple {
  id: number;
  nom: string;
  email: string;
}