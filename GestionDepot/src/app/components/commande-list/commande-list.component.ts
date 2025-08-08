import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { CommandeService } from '../../services/commande-service.service';
import { Commande } from '../../models/commande/commande.model';
import { StatutCommande } from '../../models/commande/statut-commande.enum';
import { UtilisateurService } from '../../services/utilisateur.service';
import { ChauffeurListDto } from '../../models/chauffeur.model';

@Component({
  selector: 'app-commande-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './commande-list.component.html',
  styleUrls: ['./commande-list.component.css']
})
export class CommandeListComponent implements OnInit {

  commandes: Commande[] = [];
  chauffeurs: ChauffeurListDto[] = [];
  nouveauChauffeurId: number|null = null;
  statuts = Object.values(StatutCommande);
  filtreStatut: StatutCommande | '' = '';

  constructor(
    private commandeService: CommandeService,
    private utilisateurService: UtilisateurService,
    public router: Router
  ) { }

  ngOnInit(): void {
    this.loadCommandes();
    this.utilisateurService.getChauffeurs().subscribe({
      next: (data) => this.chauffeurs = data,
      error: (err) => console.error('Erreur lors du chargement des chauffeurs:', err)
    });
  }

  loadCommandes(): void {
    if (this.filtreStatut) {
      this.commandeService.getCommandesParStatut(this.filtreStatut).subscribe(
        data => this.commandes = data,
        error => console.error('Erreur lors du chargement des commandes par statut:', error)
      );
    } else {
      this.commandeService.getToutesCommandes().subscribe(
        data => this.commandes = data,
        error => console.error('Erreur lors du chargement de toutes les commandes:', error)
      );
    }
  }

  public creerNouvelleCommande(): void {
    if (!this.nouveauChauffeurId) {
      alert('Veuillez sélectionner un chauffeur pour créer une commande.');
      return;
    }
    this.commandeService.creerCommandePourClient(this.nouveauChauffeurId).subscribe(
      nouvelleCommande => {
        console.log('Nouvelle commande créée:', nouvelleCommande);
        alert(`Commande ${nouvelleCommande.id} créée en statut EN_COURS.`);
        this.loadCommandes();
      },
      error => {
        console.error('Erreur lors de la création de la commande:', error);
        alert(`Erreur: ${error.error || error.message}`);
      }
    );
  }

  public validerCommande(idCommande: number): void {
    // 1. Trouver la commande dans la liste locale chargée par le composant
    const commandeToValidate = this.commandes.find(c => c.id === idCommande);

    // 2. Vérifier si la commande a été trouvée localement
    if (!commandeToValidate) {
      alert(`Erreur: La commande avec l'ID ${idCommande} n'a pas été trouvée localement. Veuillez recharger la liste.`);
      return; // Arrête l'exécution si la commande n'est pas trouvée
    }

    // 3. Vérifier si la commande contient des lignes
    // Comme votre DTO Commande a "lignes: LigneCommandeResponse[]", il sera toujours un tableau,
    // même s'il est vide. Donc on vérifie simplement sa longueur.
    if (commandeToValidate.lignes.length === 0) {
      alert(`Erreur: La commande ${idCommande} ne peut pas être validée car elle ne contient aucune ligne de commande.`);
      return; // Arrête l'exécution ici, n'affiche pas la confirmation et n'appelle pas le service
    }

    // 4. Si la commande a des lignes, demander confirmation et appeler le service
    if (confirm(`Voulez-vous vraiment valider la commande ${idCommande} ?`)) {
      this.commandeService.validerCommande(idCommande).subscribe(
        commandeValidee => {
          console.log('Commande validée:', commandeValidee);
          alert(`Commande ${idCommande} validée avec succès.`); // Ajouté "avec succès" pour plus de clarté
          this.loadCommandes();
        },
        error => {
          console.error('Erreur lors de la validation de la commande:', error);
          // Amélioration de l'affichage de l'erreur du backend si disponible
          alert(`Erreur lors de la validation: ${error.error?.message || error.message || 'Une erreur inconnue est survenue.'}`);
        }
      );
    }
  }

  public annulerCommande(idCommande: number): void {
    if (confirm(`Voulez-vous vraiment annuler la commande ${idCommande} ? Le stock sera restitué.`)) {
      this.commandeService.annulerCommande(idCommande).subscribe(
        message => {
          console.log('Commande annulée:', message);
          alert(`Commande ${idCommande} annulée.`);
          this.loadCommandes();
        },
        error => {
          console.error('Erreur lors de l\'annulation de la commande:', error);
          alert(`Erreur: ${error.error || error.message}`);
        }
      );
    }
  }

  public viewDetails(id: number): void {
    this.router.navigate(['/commandes', id]);
  }
}