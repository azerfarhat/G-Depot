import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router'; // Si RouterLink est utilisé dans le HTML

import { FactureService } from '../../services/facture.service';
import { Facture } from '../../models/facture/facture.model';
import { StatutFacture } from '../../models/facture/statut-facture.enum'; // Import de l'enum

@Component({
  selector: 'app-facture-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CurrencyPipe,
    DatePipe,
    RouterLink // Garder RouterLink si [routerLink] est utilisé dans le HTML
  ],
  templateUrl: './facture-list.component.html',
  styleUrls: ['./facture-list.component.css']
})
export class FactureListComponent implements OnInit {
  factures: Facture[] = [];
  statuts = Object.values(StatutFacture);
  filtreStatut: StatutFacture | '' = '';

  public StatutFacture = StatutFacture; // <--- AJOUT CRUCIAL : Expose l'enum au template

  constructor(private factureService: FactureService) { }

  ngOnInit(): void {
    this.loadFactures();
  }

  loadFactures(): void {
    this.factureService.getToutesFactures().subscribe(
      data => {
        if (this.filtreStatut) {
          this.factures = data.filter(f => f.statut === this.filtreStatut);
        } else {
          this.factures = data;
        }
      },
      error => console.error('Erreur lors du chargement des factures:', error)
    );
  }

  updateStatut(factureId: number, nouveauStatut: StatutFacture): void {
    if (confirm(`Voulez-vous marquer la facture ${factureId} comme ${nouveauStatut} ?`)) {
      this.factureService.updateFactureStatut(factureId, nouveauStatut).subscribe(
        updatedFacture => {
          alert(`Facture ${factureId} mise à jour au statut ${updatedFacture.statut}.`);
          this.loadFactures();
        },
        error => {
          console.error('Erreur lors de la mise à jour du statut:', error);
          alert(`Erreur: ${error.message}`);
        }
      );
    }
  }
  telechargerPdf(factureId: number): void {
    this.factureService.telechargerFacturePdf(factureId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `facture_${factureId}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        alert('Erreur lors du téléchargement du PDF de la facture.');
        console.error(err);
      }
    });
  }
}