import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; // <-- NOUVEL IMPORT OBLIGATOIRE POUR STANDALONE
import { FormsModule } from '@angular/forms';   // <-- NOUVEL IMPORT OBLIGATOIRE POUR STANDALONE
import { ActivatedRoute, Router, RouterLink } from '@angular/router'; // <-- NOUVEL IMPORT pour RouterLink
import { ProductService } from '../../services/product.service';
import { ProduitList } from '../../models/produit-list.model';

import { CommandeService } from '../../services/commande-service.service';
import { Commande } from '../../models/commande/commande.model';
import { LigneCommandeRequest } from '../../models/commande/ligne-commande-request.model';

@Component({
  selector: 'app-commande-detail',
  standalone: true, // <--- AJOUT CRUCIAL : Déclare le composant comme autonome
  imports: [CommonModule, FormsModule, RouterLink], // <--- AJOUT CRUCIAL : Importe les dépendances du template ici
  templateUrl: './commande-detail.component.html',
  styleUrls: ['./commande-detail.component.css']
})
export class CommandeDetailComponent implements OnInit {
  commande: Commande | undefined;
  commandeId: number | undefined;

  nouvelleLigneProduitId: number | undefined;
  nouvelleLigneQuantite: number | undefined;

  ligneAModifierId: number | undefined;
  nouvelleQuantiteLigne: number | undefined;

  produitsList: ProduitList[] = [];


  constructor(
    private route: ActivatedRoute,
    private commandeService: CommandeService,
    private productService: ProductService,
    public router: Router // Garde 'public' pour l'accès dans le template
  ) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.commandeId = +idParam;
        this.loadCommandeDetails();
      }
    });
    this.loadProduitsList();
  }

  loadProduitsList(): void {
    this.productService.getProductsForList().subscribe({
      next: (produits) => this.produitsList = produits,
      error: (err) => {
        console.error('Erreur lors du chargement des produits:', err);
        this.produitsList = [];
      }
    });
  }

  public loadCommandeDetails(): void {
    if (this.commandeId) {
      this.commandeService.getCommandeById(this.commandeId).subscribe(
        data => this.commande = data,
        error => {
          console.error('Erreur lors du chargement des détails de la commande:', error);
          alert(`Erreur: ${error.error || error.message}`);
          this.router.navigate(['/commandes']);
        }
      );
    }
  }

  public ajouterLigne(): void {
    if (this.commandeId && this.nouvelleLigneProduitId && this.nouvelleLigneQuantite && this.nouvelleLigneQuantite > 0) {
      const ligneRequest: LigneCommandeRequest = {
        produitId: this.nouvelleLigneProduitId,
        quantite: this.nouvelleLigneQuantite
      };
      this.commandeService.ajouterLigneACommande(this.commandeId, ligneRequest).subscribe(
        commandeMiseAJour => {
          console.log('Ligne ajoutée avec succès:', commandeMiseAJour);
          this.commande = commandeMiseAJour;
          alert('Ligne ajoutée.');
          this.resetLigneForm();
        },
        error => {
          console.error('Erreur lors de l\'ajout de la ligne:', error);
          alert(`Erreur: ${error.error || error.message}`);
        }
      );
    } else {
      alert('Veuillez remplir le produit ID et une quantité positive pour ajouter une ligne.');
    }
  }

  public initModifierLigne(ligneId: number, currentQuantite: number): void {
    this.ligneAModifierId = ligneId;
    this.nouvelleQuantiteLigne = currentQuantite;
  }

  public mettreAJourQuantiteLigne(): void {
    if (this.commandeId && this.ligneAModifierId && this.nouvelleQuantiteLigne !== undefined) {
      if (this.nouvelleQuantiteLigne <= 0) {
        if (!confirm(`Voulez-vous supprimer cette ligne de commande (quantité 0 ou négative) ?`)) {
          return;
        }
      }

      this.commandeService.mettreAJourQuantiteLigne(this.commandeId, this.ligneAModifierId, this.nouvelleQuantiteLigne).subscribe(
        commandeMiseAJour => {
          console.log('Quantité de ligne mise à jour:', commandeMiseAJour);
          this.commande = commandeMiseAJour;
          alert('Quantité de ligne mise à jour.');
          this.resetModifierLigneForm();
        },
        error => {
          console.error('Erreur lors de la mise à jour de la quantité:', error);
          alert(`Erreur: ${error.error || error.message}`);
        }
      );
    } else {
      alert('Veuillez sélectionner une ligne et saisir une nouvelle quantité.');
    }
  }

  public supprimerLigne(ligneId: number): void {
    if (this.commandeId && confirm(`Voulez-vous vraiment supprimer la ligne ${ligneId} ?`)) {
      this.commandeService.supprimerLigneDeCommande(this.commandeId, ligneId).subscribe(
        commandeMiseAJour => {
          console.log('Ligne supprimée avec succès:', commandeMiseAJour);
          this.commande = commandeMiseAJour;
          alert('Ligne supprimée.');
        },
        error => {
          console.error('Erreur lors de la suppression de la ligne:', error);
          alert(`Erreur: ${error.error || error.message}`);
        }
      );
    }
  }

  public resetLigneForm(): void {
    this.nouvelleLigneProduitId = undefined;
    this.nouvelleLigneQuantite = undefined;
  }

  public resetModifierLigneForm(): void {
    this.ligneAModifierId = undefined;
    this.nouvelleQuantiteLigne = undefined;
  }
}