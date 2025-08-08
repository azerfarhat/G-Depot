export enum StatutBonDeSortie {
  CREE = 'CREE',
  EN_COURS = 'EN_COURS', // Ajouté si votre backend l'utilise comme statut initial
  LIVRE = 'LIVRE',
  PARTIELLEMENT_LIVRE = 'PARTIELLEMENT_LIVRE',
  ANNULE = 'ANNULE',
  FACTURE = 'FACTURE',
  PARTIELLEMENT_FACTURE = 'PARTIELLEMENT_FACTURE',
  RETOURNE = 'RETOURNE' // Ajouté pour le statut RETOURNE
}