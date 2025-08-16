// src/app/utilisateur/utilisateur.component.ts

import { Component, OnInit } from '@angular/core';
import { UtilisateurService } from '../../services/utilisateur.service';
import { UtilisateurSimpleDto, Utilisateur } from '../../models/utilisateur.model'; // Import Utilisateur for full details
import { UtilisateurRequestDto } from '../../models/utilisateur-request.model';
import { DepotService } from '../../services/depot.service';
import { DepotDto } from '../../models/depot.model';
import { CommonModule, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-utilisateur',
  standalone: true,
  imports: [CommonModule, FormsModule, NgFor, NgIf],
  templateUrl: './utilisateur.component.html',
  styleUrls: ['./utilisateur.component.css']
})
export class UtilisateurComponent implements OnInit {

  users: UtilisateurSimpleDto[] = [];
  searchTerm: string = '';

  totalUsers: number = 0;
  adminCount: number = 0;
  responsableCount: number = 0;
  employeCount: number = 0;
  clientCount: number = 0;
  chauffeurCount: number = 0;

  // Add User Modal properties
  showAddUserModal: boolean = false;
  newUser: UtilisateurRequestDto = this.initializeNewUser();
  roles: string[] = ['ADMIN', 'RESPONSABLE', 'EMPLOYE', 'CLIENT']; // Roles for add modal
  depots: DepotDto[] = [];

  // Edit User Modal properties
  showEditUserModal: boolean = false;
  editingUser: UtilisateurRequestDto | null = null; // Data for the user being edited
  editingUserId: number | null = null;
  editRoles: string[] = ['ADMIN', 'RESPONSABLE', 'EMPLOYE', 'CLIENT', 'CHAUFFEUR']; // Allow changing to/from chauffeur for comprehensive edit

  notificationMessage: string | null = null;
  notificationType: 'success' | 'error' | null = null;
  notificationTimeout: any;

  // Confirmation Modal Properties
  isConfirmModalOpen: boolean = false;
  userToDelete: UtilisateurSimpleDto | null = null;
  confirmMessage: string = '';

  constructor(private utilisateurService: UtilisateurService, private depotService: DepotService) { }

  ngOnInit(): void {
    this.loadUsers();
    this.loadDepots();
  }

  loadDepots(): void {
    this.depotService.getAllDepots().subscribe({
      next: (data) => this.depots = data,
      error: (err) => console.error('Erreur lors du chargement des dépôts:', err)
    });
  }

  loadUsers(): void {
    this.utilisateurService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.calculateDashboardCounts();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des utilisateurs:', err);
        this.showNotification(`Erreur lors du chargement des utilisateurs: ${err.message}`, 'error');
      }
    });
  }

  calculateDashboardCounts(): void {
    this.totalUsers = this.users.length;
    this.adminCount = this.users.filter(u => u.role === 'ADMIN').length;
    this.responsableCount = this.users.filter(u => u.role === 'RESPONSABLE').length;
    this.employeCount = this.users.filter(u => u.role === 'EMPLOYE').length;
    this.clientCount = this.users.filter(u => u.role === 'CLIENT').length;
    this.chauffeurCount = this.users.filter(u => u.role === 'CHAUFFEUR').length;
  }

  get filteredUsers(): UtilisateurSimpleDto[] {
    if (!this.searchTerm) {
      return this.users;
    }
    const lowerCaseSearchTerm = this.searchTerm.toLowerCase();
    return this.users.filter(user =>
      user.nom.toLowerCase().includes(lowerCaseSearchTerm) ||
      user.email.toLowerCase().includes(lowerCaseSearchTerm) ||
      this.getDisplayRole(user.role).toLowerCase().includes(lowerCaseSearchTerm)
    );
  }

  getDisplayRole(role: string): string {
    switch (role) {
      case 'ADMIN': return 'ADMIN';
      case 'RESPONSABLE': return 'Responsable';
      case 'EMPLOYE': return 'Employe ';
      case 'CLIENT': return 'Client';
      case 'CHAUFFEUR': return 'Chauffeur';
      default: return role;
    }
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'ADMIN': return 'role-badge admin-badge';
      case 'RESPONSABLE': return 'role-badge responsable-badge';
      case 'EMPLOYE': return 'role-badge employe-badge';
      case 'CLIENT': return 'role-badge client-badge';
      case 'CHAUFFEUR': return 'role-badge chauffeur-badge';
      default: return 'role-badge';
    }
  }

  // --- Delete User Methods ---
  deleteUser(user: UtilisateurSimpleDto): void {
    this.userToDelete = user;
    this.confirmMessage = `Êtes-vous sûr de vouloir supprimer l'utilisateur "${user.nom}" ?`;
    this.isConfirmModalOpen = true;
  }

  confirmAction(confirmed: boolean): void {
    this.isConfirmModalOpen = false;

    if (confirmed && this.userToDelete) {
      const userId = this.userToDelete.id;
      const userName = this.userToDelete.nom;

      this.utilisateurService.deleteChauffeur(userId).subscribe({
        next: () => {
          this.showNotification(`Utilisateur ${userName} supprimé avec succès.`, 'success');
          this.loadUsers();
          this.userToDelete = null;
        },
        error: (err) => {
          console.error('Erreur lors de la suppression de l\'utilisateur:', err);
          this.showNotification(`Erreur lors de la suppression de l'utilisateur: ${err.message}`, 'error');
          this.userToDelete = null;
        }
      });
    } else {
      this.userToDelete = null;
    }
    this.confirmMessage = '';
  }
  // ---------------------------------

  // --- Add User Modal Methods ---
  openAddUserModal(): void {
    this.showAddUserModal = true;
    this.newUser = this.initializeNewUser();
  }

  closeAddUserModal(): void {
    this.showAddUserModal = false;
    this.newUser = this.initializeNewUser();
  }

  initializeNewUser(): UtilisateurRequestDto {
    return {
      nom: '',
      email: '',
      motDePasse: '',
      role: '',
      depotId: undefined,
      telephone: undefined,
      numeroPermis: undefined,
      marqueVehicule: undefined,
      modeleVehicule: undefined,
      matriculeVehicule: undefined
    };
  }

 // Part of src/app/utilisateur/utilisateur.component.ts

submitNewUser(): void {
    if (!this.newUser.nom || !this.newUser.email || !this.newUser.motDePasse || !this.newUser.role || !this.newUser.depotId) {
      this.showNotification('Veuillez remplir tous les champs obligatoires (Nom, Email, Mot de passe, Rôle, Dépôt).', 'error');
      return;
    }

    // Ensure chauffeur-specific fields are cleared/not sent for non-chauffeur roles added here
    this.newUser.telephone = undefined;
    this.newUser.numeroPermis = undefined;
    this.newUser.marqueVehicule = undefined;
    this.newUser.modeleVehicule = undefined;
    this.newUser.matriculeVehicule = undefined;

    this.utilisateurService.ajouterUtilisateur(this.newUser).subscribe({
      next: (response) => {
        // MODIFICATION ICI : Ajout de l'information sur l'email de bienvenue
        this.showNotification('Utilisateur ajouté avec succès! Un email de bienvenue a été envoyé.', 'success');
        this.closeAddUserModal();
        this.loadUsers();
      },
      error: (err) => {
        console.error('Erreur lors de l\'ajout de l\'utilisateur:', err);
        const errorMessage = err.message || 'Une erreur est survenue lors de l\'ajout de l\'utilisateur.';
        this.showNotification(errorMessage, 'error');
      }
    });
}
  // ---------------------------------

  // --- Edit User Modal Methods ---
  openEditModal(user: UtilisateurSimpleDto): void {
    this.showEditUserModal = true;
    this.editingUserId = user.id;

    // Fetch full user details to ensure all modifiable fields are present
    this.utilisateurService.getUtilisateurById(user.id).subscribe({
      next: (fullUser: Utilisateur) => {
        // Map the full Utilisateur object to UtilisateurRequestDto
        this.editingUser = {
          nom: fullUser.nom,
          email: fullUser.email,
          motDePasse: '', // Password not pre-filled for security, user can leave empty to not change
          role: fullUser.role,
          depotId: fullUser.depot?.id || undefined,
          // Chauffeur specific fields - populate if they exist, otherwise undefined
          telephone: fullUser.telephone || undefined,
          numeroPermis: fullUser.numeroPermis || undefined,
          marqueVehicule: fullUser.vehicule?.marque || undefined,
          modeleVehicule: fullUser.vehicule?.modele || undefined,
          matriculeVehicule: fullUser.vehicule?.matricule || undefined,
        };
      },
      error: (err) => {
        console.error('Erreur lors du chargement des détails de l\'utilisateur pour modification:', err);
        this.showNotification(`Erreur lors du chargement des détails de l'utilisateur: ${err.message}`, 'error');
        this.closeEditUserModal(); // Close modal if details cannot be loaded
      }
    });
  }

  closeEditUserModal(): void {
    this.showEditUserModal = false;
    this.editingUser = null;
    this.editingUserId = null;
  }

  submitEditUser(): void {
    if (!this.editingUser || !this.editingUserId) {
      this.showNotification('Erreur: Aucune information utilisateur à modifier.', 'error');
      return;
    }

    if (!this.editingUser.nom || !this.editingUser.email || !this.editingUser.role || !this.editingUser.depotId) {
      this.showNotification('Veuillez remplir le nom, l\'email, le rôle et le dépôt.', 'error');
      return;
    }

    // Validation for CHAUFFEUR role
    if (this.editingUser.role === 'CHAUFFEUR') {
      if (!this.editingUser.telephone || !this.editingUser.numeroPermis || !this.editingUser.marqueVehicule || !this.editingUser.modeleVehicule || !this.editingUser.matriculeVehicule) {
        this.showNotification('Pour le rôle CHAUFFEUR, le téléphone, le numéro de permis et les informations de véhicule sont obligatoires.', 'error');
        return;
      }
    } else {
      // Clear chauffeur specific fields if role is changed from CHAUFFEUR or is not CHAUFFEUR
      this.editingUser.telephone = undefined;
      this.editingUser.numeroPermis = undefined;
      this.editingUser.marqueVehicule = undefined;
      this.editingUser.modeleVehicule = undefined;
      this.editingUser.matriculeVehicule = undefined;
    }

    this.utilisateurService.updateUtilisateur(this.editingUserId, this.editingUser).subscribe({
      next: (response) => {
        this.showNotification(`Utilisateur ${response.nom} mis à jour avec succès!`, 'success');
        this.closeEditUserModal();
        this.loadUsers(); // Reload list to reflect changes
      },
      error: (err) => {
        console.error('Erreur lors de la mise à jour de l\'utilisateur:', err);
        const errorMessage = err.message || 'Une erreur est survenue lors de la mise à jour de l\'utilisateur.';
        this.showNotification(errorMessage, 'error');
      }
    });
  }
  // ---------------------------------

  // --- Notification Methods ---
  showNotification(message: string, type: 'success' | 'error'): void {
    this.notificationMessage = message;
    this.notificationType = type;

    if (this.notificationTimeout) {
      clearTimeout(this.notificationTimeout);
    }

    this.notificationTimeout = setTimeout(() => {
      this.clearNotification();
    }, 5000);
  }

  clearNotification(): void {
    this.notificationMessage = null;
    this.notificationType = null;
    if (this.notificationTimeout) {
      clearTimeout(this.notificationTimeout);
      this.notificationTimeout = null;
    }
  }
}