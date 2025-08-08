// src/app/models/depot.model.ts
import { UtilisateurSimpleDto } from './utilisateur.model';

export interface DepotDto {
  id: number;
  nom: string;
  adresse: string;
  ville: string;        // RÉINTRODUIT
  codePostal: string;   // RÉINTRODUIT
  zone: string;         // RÉINTRODUIT
  telephone: string;
  email: string;        // RÉINTRODUIT
  tva: number;
  chiffreAffaires: number;
  responsable?: UtilisateurSimpleDto;
}

export interface DepotCreateRequest {
  nom: string;
  adresse: string;
  ville: string;        // RÉINTRODUIT
  codePostal: string;   // RÉINTRODUIT
  zone: string;         // RÉINTRODUIT
  telephone: string;
  email: string;        // RÉINTRODUIT
  tva: number;
}

export interface Depot {
  id: number;
  nom: string;
}