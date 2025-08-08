import { Component, OnInit } from '@angular/core';
import { CommandeService } from '../../services/commande-service.service';
import { UtilisateurService } from '../../services/utilisateur.service';
import { Commande } from '../../models/commande/commande.model';
import { ChauffeurListDto } from '../../models/chauffeur.model';
import { StatutCommande } from '../../models/commande/statut-commande.enum';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router'; // Si utilisé dans le HTML

import { BonDeSortieService } from '../../services/bon-de-sortie.service';
import { RetourProduitPayload } from '../../models/bon-de-sortie/retour-produit-payload.model'; // Assurez-vous d'avoir ce modèle
import { FactureService } from '../../services/facture.service'; // Import pour FactureService
import { BonDeSortie } from '../../models/bon-de-sortie/bon-de-sortie.model';
import { BonDeSortieListItem } from '../../models/bon-de-sortie/bon-de-sortie-list-item.model';
import { LigneBonDeSortieResponse } from '../../models/bon-de-sortie/ligne-bon-de-sortie-response.model';
import { FactureCreateChauffeur, LigneFactureCreateChauffeur } from '../../models/facture/facture-create-chauffeur.model'; // Pour créer facture chauffeur
import { FactureCreateRequest } from '../../models/facture/facture-create-request.model'; // Pour créer facture commande
import { Facture } from '../../models/facture/facture.model'; // Pour le modèle de facture détaillé
import { StatutFacture } from '../../models/facture/statut-facture.enum'; // Pour les statuts de facture
import { StatutBonDeSortie } from '../../models/bon-de-sortie/statut-bon-de-sortie.enum'; // <-- Assurez-vous de cet import

import { Observable } from 'rxjs';

@Component({
  selector: 'app-bon-de-sortie-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CurrencyPipe,
    DatePipe,
    RouterLink // Si utilisé dans le HTML
  ],
  templateUrl: './bon-de-sortie-list.component.html',
  styleUrls: ['./bon-de-sortie-list.component.css']
})
export class BonDeSortieListComponent implements OnInit {
  getNomChauffeurAssocie(): string {
    const commande = this.commandesValidees.find(cmd => cmd.id === this.nouvelleCommandeId);
    return commande?.client?.nom || 'N/A';
  }

  bonsDeSortie$!: Observable<BonDeSortieListItem[]>;
  
  selectedBdsForDetails: BonDeSortie | null = null;
  selectedBdsForReturn: BonDeSortie | null = null;
  selectedBdsForFacturation: BonDeSortie | null = null; // Nouveau pour la modale de facturation

  // Inputs pour la création d'un nouveau BDS
  showCreateForm: boolean = false;
  nouvelleCommandeId: number | undefined;
  nouveauChauffeurId: number | undefined; // ID du chauffeur à l'étape de création de BDS (auto-associé)
  // Associer automatiquement le chauffeur à la commande sélectionnée
  onCommandeChange(): void {
    if (this.nouvelleCommandeId) {
      const commande = this.commandesValidees.find(cmd => cmd.id === this.nouvelleCommandeId);
      // On suppose que le client de la commande est le chauffeur
      this.nouveauChauffeurId = commande?.client?.id;
    } else {
      this.nouveauChauffeurId = undefined;
    }
  }
  commandesValidees: Commande[] = [];
  chauffeurs: ChauffeurListDto[] = [];

  // Inputs pour la création d'une facture par chauffeur
  factureChauffeurProduitId: number | undefined;
  factureChauffeurQuantite: number | undefined;
  
  // Inputs pour la création d'une facture par commande
  factureCommandeDateEcheance: string = new Date().toISOString().split('T')[0]; // Date par défaut

  // Pour la gestion des retours : une map pour stocker les quantités retournées saisies
  quantitesRetourneesMap: Map<number, number> = new Map();

  // Pour la création de facture, vous pourriez avoir besoin de l'enum Facture
  public StatutFacture = StatutFacture; // Expose l'enum au template si nécessaire (pour les class.status-...)
  public StatutBonDeSortie = StatutBonDeSortie; // Expose l'enum au template pour les statuts des badges


  constructor(
    private bdsService: BonDeSortieService,
    private factureService: FactureService,
    private commandeService: CommandeService,
    private utilisateurService: UtilisateurService
  ) { }

  ngOnInit(): void {
    this.loadBonsDeSortie();
    // Charger les commandes VALIDEE pour la création
    this.commandeService.getCommandesParStatut(StatutCommande.VALIDEE).subscribe({
      next: (data) => this.commandesValidees = data,
      error: () => { this.commandesValidees = []; }
    });
    // Charger la liste des chauffeurs
    this.utilisateurService.getChauffeurs().subscribe({
      next: (data) => this.chauffeurs = data,
      error: () => { this.chauffeurs = []; }
    });
  }

  loadBonsDeSortie(): void {
    this.bonsDeSortie$ = this.bdsService.getBonsDeSortie();
  }
  
  openCreateBonDeSortieForm(): void {
    this.showCreateForm = true;
    this.nouvelleCommandeId = undefined;
    this.nouveauChauffeurId = undefined;
  }

  creerNouveauBonDeSortie(): void {
    if (this.nouvelleCommandeId && this.nouveauChauffeurId) {
      this.bdsService.creerBonDeSortie(this.nouvelleCommandeId, this.nouveauChauffeurId).subscribe(
        (bdsCree: BonDeSortieListItem) => {
          alert(`Bon de Sortie ${bdsCree.numeroBDS} créé avec succès!`);
          this.loadBonsDeSortie();
          this.showCreateForm = false; // Fermer le formulaire après succès
          this.nouvelleCommandeId = undefined;
          this.nouveauChauffeurId = undefined;
        },
        error => {
          console.error('Erreur lors de la création du Bon de Sortie:', error);
          alert(`Erreur: ${error.message}`);
        }
      );
    } else {
      alert('Veuillez sélectionner une commande validée. Le chauffeur sera associé automatiquement.');
    }
  }

  openDetailsModal(bdsListItem: BonDeSortieListItem): void {
    this.bdsService.getBonDeSortieById(bdsListItem.id).subscribe(
      (details: BonDeSortie) => {
        this.selectedBdsForDetails = details;
      },
      error => {
        console.error('Erreur lors du chargement des détails pour la modale:', error);
        alert(`Erreur: ${error.message}`);
      }
    );
  }

  closeDetailsModal(): void {
    this.selectedBdsForDetails = null;
  }
  
  openRetourModal(bdsListItem: BonDeSortieListItem): void {
    this.bdsService.getBonDeSortieById(bdsListItem.id).subscribe(
      (details: BonDeSortie) => {
        this.selectedBdsForReturn = details;
        this.quantitesRetourneesMap.clear();
        if (details.lignes) {
          details.lignes.forEach(ligne => {
            // Initialiser avec 0 ou la quantité disponible si on veut proposer par défaut
            this.quantitesRetourneesMap.set(ligne.id, 0); 
          });
        }
      },
      error => {
        console.error('Erreur lors du chargement des détails pour les retours:', error);
        alert(`Erreur: ${error.message}`);
      }
    );
  }

  closeRetourModal(): void {
    this.selectedBdsForReturn = null;
    this.quantitesRetourneesMap.clear();
  }

  onQuantiteRetourneeChange(ligneId: number, event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    const value = parseInt(inputElement.value, 10);
    this.quantitesRetourneesMap.set(ligneId, isNaN(value) ? 0 : value);
  }

  enregistrerRetours(): void {
    if (!this.selectedBdsForReturn || !this.selectedBdsForReturn.id) {
      alert('Aucun Bon de Sortie sélectionné pour enregistrer les retours.');
      return;
    }

    const retoursPayload: RetourProduitPayload[] = [];
    let hasValidReturn = false;

    this.selectedBdsForReturn.lignes.forEach(ligne => {
      const quantiteSaisie = this.quantitesRetourneesMap.get(ligne.id) || 0;
      
      if (quantiteSaisie > 0) {
        const maxQuantitePossibleRetour = ligne.quantiteSortie - ligne.quantiteRetournee;
        if (quantiteSaisie > maxQuantitePossibleRetour) {
          alert(`Erreur: La quantité à retourner pour ${ligne.nomProduit} (${quantiteSaisie}) dépasse la quantité maximale possible (${maxQuantitePossibleRetour}).`);
          return; 
        }
        
        retoursPayload.push({
          ligneBonDeSortieId: ligne.id,
          quantiteRetournee: quantiteSaisie
        });
        hasValidReturn = true;
      }
    });

    if (!hasValidReturn) {
      alert('Veuillez saisir au moins une quantité à retourner pour un produit.');
      return;
    }

    this.bdsService.enregistrerRetours(this.selectedBdsForReturn.id, retoursPayload).subscribe(
      () => {
        alert('Retours enregistrés avec succès!');
        this.closeRetourModal();
        this.loadBonsDeSortie();
      },
      error => {
        console.error('Erreur lors de l\'enregistrement des retours:', error);
        alert(`Erreur lors de l'enregistrement des retours: ${error.message}`);
      }
    );
  }

  // --- Logique pour la facturation ---

  openFacturationModal(bdsListItem: BonDeSortieListItem): void {
    this.bdsService.getBonDeSortieById(bdsListItem.id).subscribe(
      (details: BonDeSortie) => {
        this.selectedBdsForFacturation = details;
        this.factureChauffeurProduitId = undefined;
        this.factureChauffeurQuantite = undefined;
      },
      error => {
        console.error('Erreur lors du chargement des détails pour facturation:', error);
        alert(`Erreur: ${error.message}`);
      }
    );
  }

  closeFacturationModal(): void {
    this.selectedBdsForFacturation = null;
    this.factureChauffeurProduitId = undefined;
    this.factureChauffeurQuantite = undefined;
  }

  creerFactureChauffeur(): void {
    if (!this.selectedBdsForFacturation || !this.selectedBdsForFacturation.id || !this.factureChauffeurProduitId || !this.factureChauffeurQuantite || this.factureChauffeurQuantite <= 0) {
      alert('Veuillez sélectionner un Bon de Sortie, un produit et une quantité positive.');
      return;
    }

    const lignePourFacture: LigneFactureCreateChauffeur = {
      produitId: this.factureChauffeurProduitId,
      quantite: this.factureChauffeurQuantite
    };

    const payload: FactureCreateChauffeur = {
      bonDeSortieId: this.selectedBdsForFacturation.id,
      lignes: [lignePourFacture]
    };

    this.factureService.creerFactureChauffeur(payload).subscribe(
      (factureCreee: Facture) => {
        alert(`Facture ${factureCreee.numeroFacture} créée avec succès!`);
        this.closeFacturationModal();
        this.loadBonsDeSortie();
      },
      error => {
        console.error('Erreur lors de la création de la facture:', error);
        alert(`Erreur: ${error.message}`);
      }
    );
  }
}