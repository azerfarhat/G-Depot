// src/app/pages/depot-list/depot-list.component.ts
import { Component, OnInit } from '@angular/core';
import { DepotService } from '../../services/depot.service';
import { UtilisateurService } from '../../services/utilisateur.service';
import { DepotDto, DepotCreateRequest } from '../../models/depot.model';
import { UtilisateurSimpleDto } from '../../models/utilisateur.model';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs'; // Assurez-vous d'importer Observable

@Component({
  selector: 'app-depot-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './depot-list.component.html',
  styleUrls: ['./depot-list.component.css']
})
export class DepotListComponent implements OnInit {
  depots: DepotDto[] = [];
  filteredDepots: DepotDto[] = [];
  isLoading: boolean = true;
  errorMessage: string = '';

  isEditModalOpen: boolean = false;
  isDetailsModalOpen: boolean = false;
  isNewDepotMode: boolean = false;
  selectedDepot: DepotDto | null = null;

  // NOUVEAU: Pour la modale de confirmation de suppression
  isConfirmDeleteModalOpen: boolean = false;
  depotToDeleteId: number | null = null;

  depotForm: FormGroup;
  searchQuery: string = '';

  responsables: UtilisateurSimpleDto[] = [];
  isLoadingResponsables: boolean = true;
  responsablesErrorMessage: string = '';

  constructor(
    private depotService: DepotService,
    private utilisateurService: UtilisateurService,
    private fb: FormBuilder
  ) {
    this.depotForm = this.fb.group({
      nom: ['', Validators.required],
      adresse: ['', Validators.required],
      ville: ['', Validators.required],
      codePostal: ['', [Validators.required, Validators.pattern(/^\d{5}$/)]],
      zone: ['', Validators.required],
      responsableId: [null, Validators.required],
      telephone: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      tva: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.loadDepots();
    this.loadResponsables();
  }

  loadDepots(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.depotService.getAllDepots().subscribe({
      next: (data) => {
        this.depots = data;
        this.applyFilter();
        this.isLoading = false;
        console.log('Liste des dépôts chargée :', data);
      },
      error: (err) => {
        this.errorMessage = 'Impossible de charger les dépôts : ' + err.message;
        this.isLoading = false;
        console.error('Erreur lors du chargement des dépôts :', err);
      }
    });
  }

  loadResponsables(): void {
    this.isLoadingResponsables = true;
    this.responsablesErrorMessage = '';
    this.utilisateurService.getAllResponsables().subscribe({
      next: (data) => {
        this.responsables = data;
        this.isLoadingResponsables = false;
        console.log('Liste des responsables chargée :', data);
      },
      error: (err) => {
        this.responsablesErrorMessage = 'Impossible de charger les responsables : ' + err.message;
        console.error('Erreur lors du chargement des responsables :', err);
      }
    });
  }

  getTotalDepots(): number {
    return this.depots.length;
  }

  getResponsableTotal(): number {
    return this.responsables.length;
  }

  onSearch(): void {
    this.applyFilter();
  }

  applyFilter(): void {
    if (!this.searchQuery) {
      this.filteredDepots = [...this.depots];
    } else {
      const lowerCaseQuery = this.searchQuery.toLowerCase();
      this.filteredDepots = this.depots.filter(depot =>
        depot.nom.toLowerCase().includes(lowerCaseQuery) ||
        depot.ville.toLowerCase().includes(lowerCaseQuery) ||
        (depot.responsable && depot.responsable.nom.toLowerCase().includes(lowerCaseQuery))
      );
    }
  }

  openNewDepotModal(): void {
    this.isNewDepotMode = true;
    this.selectedDepot = null;
    this.depotForm.reset({
      adresse: '',
      ville: '',
      codePostal: '',
      zone: '',
      email: '',
      tva: 20
    });
    this.depotForm.get('responsableId')?.enable();
    this.isEditModalOpen = true;
  }

  openEditModal(depot: DepotDto): void {
    this.isNewDepotMode = false;
    this.selectedDepot = depot;
    this.depotForm.patchValue({
      nom: depot.nom,
      adresse: depot.adresse,
      ville: depot.ville,
      codePostal: depot.codePostal,
      zone: depot.zone,
      telephone: depot.telephone,
      email: depot.email,
      tva: depot.tva,
      responsableId: depot.responsable?.id || null
    });
    this.depotForm.get('responsableId')?.enable();
    this.isEditModalOpen = true;
  }

  openDetailsModal(depot: DepotDto): void {
    this.selectedDepot = depot;
    this.isDetailsModalOpen = true;
  }

  closeModals(): void {
    this.isEditModalOpen = false;
    this.isDetailsModalOpen = false;
    this.selectedDepot = null;
    this.depotForm.reset();
    this.errorMessage = '';
  }

  saveDepot(): void {
    if (this.depotForm.invalid) {
      this.depotForm.markAllAsTouched();
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires correctement.';
      return;
    }

    const depotData: DepotCreateRequest = {
      nom: this.depotForm.value.nom,
      adresse: this.depotForm.value.adresse,
      ville: this.depotForm.value.ville,
      codePostal: this.depotForm.value.codePostal,
      zone: this.depotForm.value.zone,
      telephone: this.depotForm.value.telephone,
      email: this.depotForm.value.email,
      tva: this.depotForm.value.tva
    };

    const newResponsableId = this.depotForm.value.responsableId;

    if (this.isNewDepotMode) {
      if (!newResponsableId) {
        this.errorMessage = 'Veuillez sélectionner un responsable pour le nouveau dépôt.';
        return;
      }
      this.depotService.ajouterDepot(depotData, newResponsableId).subscribe({
        next: (response) => {
          alert('Dépôt ajouté avec succès !');
          this.closeModals();
          this.loadDepots();
        },
        error: (err) => {
          this.errorMessage = 'Erreur lors de l\'ajout du dépôt : ' + err.message;
          console.error('Erreur d\'ajout :', err);
        }
      });
    } else if (this.selectedDepot) {
      const depotId = this.selectedDepot.id;
      const currentResponsableId = this.selectedDepot.responsable?.id || null;

      this.depotService.updateDepot(depotId, depotData).subscribe({
        next: () => {
          if (newResponsableId !== currentResponsableId) {
            this.depotService.assignerResponsable(depotId, newResponsableId).subscribe({
              next: () => {
                alert('Dépôt et responsable mis à jour avec succès !');
                this.closeModals();
                this.loadDepots();
              },
              error: (err) => {
                this.errorMessage = 'Erreur lors de la mise à jour du responsable : ' + err.message;
                console.error('Erreur mise à jour responsable :', err);
              }
            });
          } else {
            alert('Dépôt mis à jour avec succès !');
            this.closeModals();
            this.loadDepots();
          }
        },
        error: (err) => {
          this.errorMessage = 'Erreur lors de la mise à jour du dépôt : ' + err.message;
          console.error('Erreur de mise à jour du dépôt :', err);
        }
      });
    }
  }

  // MODIFIÉ : Ouvre la modale de confirmation au lieu de la confirmation native
  supprimerDepot(id: number): void {
    this.depotToDeleteId = id; // Stocke l'ID du dépôt à supprimer
    this.isConfirmDeleteModalOpen = true; // Ouvre la modale de confirmation
  }

  // NOUVEAU: Méthode appelée quand la suppression est confirmée
  confirmDelete(): void {
    if (this.depotToDeleteId !== null) {
      this.depotService.supprimerDepot(this.depotToDeleteId).subscribe({
        next: (response) => {
          alert('Dépôt supprimé avec succès !');
          this.closeConfirmDeleteModal(); // Ferme la modale après succès
          this.loadDepots(); // Recharge la liste des dépôts
        },
        error: (err) => {
          this.errorMessage = 'Erreur lors de la suppression du dépôt : ' + err.message;
          console.error('Erreur de suppression :', err);
          this.closeConfirmDeleteModal(); // Ferme la modale même en cas d'erreur
        }
      });
    }
  }

  // NOUVEAU: Méthode pour fermer la modale de confirmation
  closeConfirmDeleteModal(): void {
    this.isConfirmDeleteModalOpen = false;
    this.depotToDeleteId = null; // Réinitialise l'ID
    this.errorMessage = ''; // Efface les messages d'erreur si présents
  }
}