import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common'; // Assurez-vous que CommonModule est là
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router'; // Si utilisé dans le HTML

import { UtilisateurService } from '../../services/utilisateur.service';
import { FactureService } from '../../services/facture.service'; // <--- NOUVEL IMPORT
import { ChauffeurListDto } from '../../models/chauffeur.model';
import { UtilisateurRequestDto } from '../../models/utilisateur-request.model';
import { DepotDto } from '../../models/depot.model';
import { DepotService } from '../../services/depot.service';
import { Facture } from '../../models/facture/facture.model'; // Pour le type de la réponse


@Component({
  selector: 'app-chauffeur-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink], // RouterLink si utilisé dans le HTML
  templateUrl: './chauffeur-list.component.html',
  styleUrls: ['./chauffeur-list.component.css']
})
export class ChauffeurListComponent implements OnInit {
  depots: DepotDto[] = [];
  private utilisateurService = inject(UtilisateurService);
  private factureService = inject(FactureService); // <--- INJECTION DU SERVICE FACTURE
  private fb = inject(FormBuilder);

  chauffeurs: ChauffeurListDto[] = [];
  isLoading = true;
  errorMessage: string | null = null;
  notificationMessage: string | null = null;
  notificationType: 'success' | 'error' = 'success';

  isModalOpen = false;
  chauffeurForm!: FormGroup;
  isSubmitting = false;
  editingChauffeurId: number | null = null;

  isConfirmModalOpen = false;
  confirmMessage = '';
  private actionToConfirm: (() => void) | null = null;

  // Modale pour afficher les factures du chauffeur
  showFacturesChauffeurModal: boolean = false;
  chauffeurFactures: Facture[] = [];
  selectedChauffeurNom: string | null = null;

  private depotService = inject(DepotService);

  ngOnInit(): void {
    this.loadChauffeurs();
    this.initChauffeurForm();
    this.loadDepots();
  }

  loadDepots(): void {
    this.depotService.getAllDepots().subscribe({
      next: (data: DepotDto[]) => this.depots = data,
      error: (err: any) => console.error('Erreur lors du chargement des dépôts:', err)
    });
  }

  loadChauffeurs(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.utilisateurService.getChauffeurs().subscribe({
      next: (data) => { this.chauffeurs = data; this.isLoading = false; },
      error: (err) => { this.errorMessage = err.message; this.isLoading = false; }
    });
  }

  initChauffeurForm(): void {
    this.chauffeurForm = this.fb.group({
      nom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [Validators.minLength(6)]],
      role: ['CHAUFFEUR', Validators.required],
      depotId: [null, Validators.required],
      telephone: [''],
      numeroPermis: ['', Validators.required],
      marqueVehicule: ['', Validators.required],
      modeleVehicule: ['', Validators.required],
      matriculeVehicule: ['', Validators.required]
    });
  }
  
  openAddModal(): void {
    this.editingChauffeurId = null;
    this.chauffeurForm.reset({ role: 'CHAUFFEUR' });
    this.chauffeurForm.get('motDePasse')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.chauffeurForm.get('motDePasse')?.updateValueAndValidity();
    this.isModalOpen = true;
  }

  openEditModal(chauffeur: ChauffeurListDto): void {
  this.editingChauffeurId = chauffeur.id;
  this.chauffeurForm.get('motDePasse')?.clearValidators();
  this.chauffeurForm.get('motDePasse')?.updateValueAndValidity();
  this.chauffeurForm.get('motDePasse')?.reset();
  this.utilisateurService.getUtilisateurById(chauffeur.id).subscribe(data => {
    this.chauffeurForm.patchValue({
      nom: data.nom,
      email: data.email,
      telephone: data.telephone,
      numeroPermis: data.numeroPermis,
      marqueVehicule: data.vehicule?.marque,
      modeleVehicule: data.vehicule?.modele,
      matriculeVehicule: data.vehicule?.matricule
    });
    // Reset form state so it is pristine after patchValue
    this.chauffeurForm.markAsPristine();
    this.chauffeurForm.markAsUntouched();
    this.isModalOpen = true;
  });
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.editingChauffeurId = null;
  }

  onSubmit(): void {
    if (this.chauffeurForm.invalid) {
      this.chauffeurForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formData = this.chauffeurForm.value as UtilisateurRequestDto;
    
    if (this.editingChauffeurId) {
      this.utilisateurService.updateUtilisateur(this.editingChauffeurId, formData).subscribe({
        next: () => this.handleSuccess('Chauffeur modifié avec succès !'),
        error: (err) => this.handleError(err)
      });
    } else {
      this.utilisateurService.ajouterUtilisateur(formData).subscribe({
        next: () => this.handleSuccess('Chauffeur ajouté avec succès !'),
        error: (err) => this.handleError(err)
      });
    }
  }

  deleteChauffeur(id: number, nom: string): void {
    this.confirmMessage = `Êtes-vous sûr de vouloir supprimer le chauffeur "${nom}" ?`;
    this.actionToConfirm = () => {
      this.utilisateurService.deleteChauffeur(id).subscribe({
        next: () => this.handleSuccess('Chauffeur supprimé avec succès.'),
        error: (err) => this.handleError(err)
      });
    };
    this.isConfirmModalOpen = true;
  }

  confirmAction(isConfirmed: boolean): void {
    if (isConfirmed && this.actionToConfirm) {
      this.actionToConfirm();
    }
    this.isConfirmModalOpen = false;
    this.actionToConfirm = null;
  }

  private handleSuccess(message: string): void {
    this.isSubmitting = false;
    this.closeModal();
    this.loadChauffeurs();
    this.showNotification(message, 'success');
  }

  private handleError(err: Error): void {
    this.isSubmitting = false;
    this.showNotification(err.message, 'error');
  }
  
  showNotification(message: string, type: 'success' | 'error'): void {
    this.notificationMessage = message;
    this.notificationType = type;
    setTimeout(() => this.clearNotification(), 5000);
  }

  clearNotification(): void {
    this.notificationMessage = null;
  }

  // --- NOUVELLE LOGIQUE POUR LES FACTURES DU CHAUFFEUR ---
  openFacturesChauffeurModal(chauffeurId: number, chauffeurNom: string): void {
    this.selectedChauffeurNom = chauffeurNom;
    this.factureService.getFacturesParChauffeur(chauffeurId).subscribe(
      (factures: Facture[]) => {
        this.chauffeurFactures = factures;
        this.showFacturesChauffeurModal = true;
      },
      error => {
        console.error(`Erreur lors du chargement des factures du chauffeur ${chauffeurNom}:`, error);
        alert(`Erreur lors du chargement des factures: ${error.message}`);
      }
    );
  }

  closeFacturesChauffeurModal(): void {
    this.showFacturesChauffeurModal = false;
    this.chauffeurFactures = [];
    this.selectedChauffeurNom = null;
  }
}