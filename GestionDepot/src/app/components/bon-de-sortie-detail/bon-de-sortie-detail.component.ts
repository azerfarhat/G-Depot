import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router'; // RouterLink est maintenant nécessaire ici
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { BonDeSortieService } from '../../services/bon-de-sortie.service';
import { BonDeSortie } from '../../models/bon-de-sortie/bon-de-sortie.model';
import { CommandeService } from '../../services/commande-service.service';
import { UtilisateurService } from '../../services/utilisateur.service';
import { ChauffeurListDto } from '../../models/chauffeur.model';
import { Commande } from '../../models/commande/commande.model';
import { StatutCommande } from '../../models/commande/statut-commande.enum';

@Component({
  selector: 'app-bon-de-sortie-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, CurrencyPipe, DatePipe], // RouterLink ajouté ici
  templateUrl: './bon-de-sortie-detail.component.html',
  styleUrls: ['./bon-de-sortie-detail.component.css']
})
export class BonDeSortieDetailComponent implements OnInit {
  bonDeSortie: BonDeSortie | undefined;
  bonDeSortieId: number | undefined;

  // Pour la création modale
  showNouveauBds = false;
  commandesEnCours: Commande[] = [];
  chauffeurs: ChauffeurListDto[] = [];
  selectedCommandeId: number|null = null;
  selectedChauffeurId: number|null = null;

  // Ajout méthode pour ouvrir la page de création
  ouvrirNouveauBonDeSortie() {
    this.router.navigate(['/bons-de-sortie/nouveau']);
  }

  constructor(
    private route: ActivatedRoute,
    private bonDeSortieService: BonDeSortieService,
    private commandeService: CommandeService,
    private utilisateurService: UtilisateurService,
    public router: Router
  ) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.bonDeSortieId = +idParam;
        this.loadBonDeSortieDetails();
      }
    });
    // Précharger les listes pour la modale
    this.commandeService.getCommandesParStatut(StatutCommande.EN_COURS).subscribe({
      next: (data) => this.commandesEnCours = data,
      error: () => {}
    });
    this.utilisateurService.getChauffeurs().subscribe({
      next: (data) => this.chauffeurs = data,
      error: () => {}
    });
  }

  loadBonDeSortieDetails(): void {
    if (this.bonDeSortieId) {
      this.bonDeSortieService.getBonDeSortieById(this.bonDeSortieId).subscribe(
        data => this.bonDeSortie = data,
        error => {
          console.error('Erreur lors du chargement des détails du Bon de Sortie:', error);
          alert(`Erreur: ${error.message}`);
          this.router.navigate(['/bons-de-sortie']);
        }
      );
    }
  }

  creerBonDeSortie(): void {
    if (!this.selectedCommandeId || !this.selectedChauffeurId) {
      alert('Veuillez sélectionner une commande et un chauffeur.');
      return;
    }
    this.bonDeSortieService.creerBonDeSortie(this.selectedCommandeId, this.selectedChauffeurId).subscribe({
      next: (bds) => {
        alert('Bon de sortie créé avec succès !');
        this.showNouveauBds = false;
        this.selectedCommandeId = null;
        this.selectedChauffeurId = null;
        this.router.navigate(['/bons-de-sortie', bds.id]);
      },
      error: () => alert('Erreur lors de la création du bon de sortie')
    });
  }
}