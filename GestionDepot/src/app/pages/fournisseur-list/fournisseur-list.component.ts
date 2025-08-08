// src/app/fournisseur-list/fournisseur-list.component.ts
// READY TO COPY PASTE

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { FournisseurService } from '../../services/fournisseur.service';
import { FournisseurSummary, Fournisseur } from '../../models/fournisseur.model';

@Component({
  selector: 'app-fournisseur-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './fournisseur-list.component.html',
  styleUrls: ['./fournisseur-list.component.css']
})
export class FournisseurListComponent implements OnInit {
  fournisseurs: FournisseurSummary[] = [];
  searchTerm: string = '';

  totalFournisseurs: number = 0;
  totalStocksGlobal: number = 0;
  totalValeurGlobal: number = 0;

  isModalOpen: boolean = false;
  fournisseurForm: FormGroup;
  editingFournisseurId: number | null = null;
  modalTitle: string = '';

  isConfirmModalOpen: boolean = false;
  fournisseurToDeleteId: number | null = null;
  fournisseurToDeleteName: string = '';

  notification: { message: string, type: 'success' | 'error' | '' } = { message: '', type: '' };
  notificationVisible: boolean = false;
  notificationTimeout: any;

  constructor(
    private fournisseurService: FournisseurService,
    private router: Router
  ) {
    this.fournisseurForm = new FormGroup({
      nom: new FormControl('', Validators.required),
      societe: new FormControl('', Validators.required),
      email: new FormControl('', [Validators.required, Validators.email]),
      // **** MODIFICATION HERE: Removed Validators.pattern(/^\d{10}$/) ****
      telephone: new FormControl('', Validators.required), // Phone number is still required, but no length/digit pattern
      adresse: new FormControl('', Validators.required),
      pays: new FormControl('', Validators.required),
      siteWeb: new FormControl('', Validators.pattern(/^(https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|www\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|https?:\/\/[a-zA-Z0-9]+\.[^\s]{2,}|[a-zA-Z0-9]+\.[^\s]{2,})$/i))
    });
  }

  ngOnInit(): void {
    this.loadFournisseurs();
  }

  loadFournisseurs(): void {
    this.fournisseurService.getFournisseursSummary().subscribe({
      next: (data) => {
        this.fournisseurs = data;
        this.calculateSummaryTotals();
        console.log('Fournisseurs loaded:', this.fournisseurs);
      },
      error: (err) => {
        console.error('Error fetching fournisseurs:', err);
        this.showNotification('Erreur lors du chargement des fournisseurs. Veuillez vérifier la console.', 'error');
      }
    });
  }

  calculateSummaryTotals(): void {
    this.totalFournisseurs = this.fournisseurs.length;
    this.totalStocksGlobal = this.fournisseurs.reduce((sum, f) => sum + f.totalStocksQuantity, 0);
    this.totalValeurGlobal = this.fournisseurs.reduce((sum, f) => sum + f.totalCommandeValue, 0);
  }

  get filteredFournisseurs(): FournisseurSummary[] {
    if (!this.searchTerm) {
      return this.fournisseurs;
    }
    const lowerCaseSearchTerm = this.searchTerm.toLowerCase();
    return this.fournisseurs.filter(f =>
      f.nom.toLowerCase().includes(lowerCaseSearchTerm) ||
      f.email.toLowerCase().includes(lowerCaseSearchTerm) ||
      f.telephone.toLowerCase().includes(lowerCaseSearchTerm) ||
      f.societe.toLowerCase().includes(lowerCaseSearchTerm)
    );
  }

  addFournisseur(): void {
    this.editingFournisseurId = null;
    this.modalTitle = 'Ajouter un nouveau fournisseur';
    this.fournisseurForm.reset();
    this.isModalOpen = true;
  }

  editFournisseur(id: number): void {
    this.editingFournisseurId = id;
    this.modalTitle = 'Modifier le fournisseur';

    this.fournisseurService.getFournisseurById(id).subscribe({
      next: (fournisseur) => {
        this.fournisseurForm.patchValue(fournisseur);
        this.isModalOpen = true;
      },
      error: (err) => {
        console.error('Error fetching fournisseur for edit:', err);
        this.showNotification('Erreur lors du chargement des détails du fournisseur.', 'error');
      }
    });
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.fournisseurForm.reset();
    this.editingFournisseurId = null;
  }

  saveFournisseur(): void {
    if (this.fournisseurForm.invalid) {
      this.fournisseurForm.markAllAsTouched();
      this.showNotification('Veuillez remplir tous les champs obligatoires correctement.', 'error');
      return;
    }

    const fournisseurData: Fournisseur = this.fournisseurForm.value;

    if (this.editingFournisseurId) {
      this.fournisseurService.updateFournisseur(this.editingFournisseurId, fournisseurData).subscribe({
        next: (updatedFournisseur) => {
          console.log('Fournisseur updated successfully:', updatedFournisseur);
          this.showNotification('Fournisseur modifié avec succès !', 'success');
          this.loadFournisseurs();
          this.closeModal();
        },
        error: (err) => {
          console.error('Error updating fournisseur:', err);
          const errorMessage = typeof err.error === 'string' ? err.error : 'Une erreur inconnue est survenue.';
          this.showNotification(`Erreur lors de la modification: ${errorMessage}`, 'error');
        }
      });
    } else {
      this.fournisseurService.addFournisseur(fournisseurData).subscribe({
        next: (response) => {
          this.showNotification(response || 'Fournisseur ajouté avec succès !', 'success');
          this.loadFournisseurs();
          this.closeModal();
        },
        error: (err) => {
          console.error('Error adding fournisseur:', err);
          const errorMessage = typeof err.error === 'string' ? err.error : 'Une erreur inconnue est survenue.';
          this.showNotification(`Erreur lors de l'ajout: ${errorMessage}`, 'error');
        }
      });
    }
  }

  confirmDeleteFournisseur(id: number, nom: string): void {
    this.fournisseurToDeleteId = id;
    this.fournisseurToDeleteName = nom;
    this.isConfirmModalOpen = true;
  }

  deleteConfirmed(): void {
    if (this.fournisseurToDeleteId) {
      this.fournisseurService.deleteFournisseur(this.fournisseurToDeleteId).subscribe({
        next: (response) => {
          this.showNotification(response || 'Fournisseur supprimé avec succès !', 'success');
          this.loadFournisseurs();
          this.cancelDelete();
        },
        error: (err) => {
          console.error('Error deleting fournisseur:', err);
          const errorMessage = typeof err.error === 'string' ? err.error : 'Une erreur inconnue est survenue.';
          this.showNotification(`Erreur lors de la suppression: ${errorMessage}`, 'error');
          this.cancelDelete();
        }
      });
    }
  }

  cancelDelete(): void {
    this.isConfirmModalOpen = false;
    this.fournisseurToDeleteId = null;
    this.fournisseurToDeleteName = '';
  }

  showNotification(message: string, type: 'success' | 'error'): void {
    this.notification.message = message;
    this.notification.type = type;
    this.notificationVisible = true;

    if (this.notificationTimeout) {
      clearTimeout(this.notificationTimeout);
    }

    this.notificationTimeout = setTimeout(() => {
      this.notificationVisible = false;
    }, 5000);
  }

  closeNotification(): void {
    this.notificationVisible = false;
    if (this.notificationTimeout) {
      clearTimeout(this.notificationTimeout);
    }
  }
}