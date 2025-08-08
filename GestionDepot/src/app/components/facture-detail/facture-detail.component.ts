import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router'; // RouterLink est ajouté ici
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { FactureService } from '../../services/facture.service';
import { Facture } from '../../models/facture/facture.model'; // Modèle détaillé de Facture
import { StatutFacture } from '../../models/facture/statut-facture.enum'; // Import de l'enum

@Component({
  selector: 'app-facture-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CurrencyPipe,
    DatePipe,
    RouterLink // Garder RouterLink si [routerLink] est utilisé dans le HTML
  ],
  templateUrl: './facture-detail.component.html',
  styleUrls: ['./facture-detail.component.css']
})
export class FactureDetailComponent implements OnInit {
  facture: Facture | undefined;
  factureId: number | undefined;

  public StatutFacture = StatutFacture; // <--- AJOUT CRUCIAL : Expose l'enum au template

  constructor(
    private route: ActivatedRoute,
    private factureService: FactureService,
    public router: Router
  ) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.factureId = +idParam;
        this.loadFactureDetails();
      }
    });
  }

  loadFactureDetails(): void {
    if (this.factureId) {
      this.factureService.getFactureById(this.factureId).subscribe(
        data => this.facture = data,
        error => {
          console.error('Erreur lors du chargement des détails de la facture:', error);
          alert(`Erreur: ${error.message}`);
          this.router.navigate(['/factures']);
        }
      );
    }
  }

  updateStatut(nouveauStatut: StatutFacture): void {
    if (!this.factureId) return;

    if (confirm(`Voulez-vous marquer la facture ${this.factureId} comme ${nouveauStatut} ?`)) {
      this.factureService.updateFactureStatut(this.factureId, nouveauStatut).subscribe(
        updatedFacture => {
          alert(`Facture ${this.factureId} mise à jour au statut ${updatedFacture.statut}.`);
          this.facture = updatedFacture;
        },
        error => {
          console.error('Erreur lors de la mise à jour du statut:', error);
          alert(`Erreur: ${error.message}`);
        }
      );
    }
  }
}