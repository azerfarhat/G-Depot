// Correspond à ChauffeurListDto.java
export interface ChauffeurListDto {
  id: number;
  nom: string; 
  email: string;
  telephone: string;
  permis: string;
  vehiculeInfo: string;
  livraisons: number;
  // statut: string; <<< SUPPRIMÉ
}