import { Routes } from '@angular/router';

// --- Imports de tous vos composants de page (pages/) ---
import { LoginComponent } from './pages/login/login.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { InventoryComponent } from './pages/inventory/inventory.component';
import { ChauffeurListComponent } from './pages/chauffeur-list/chauffeur-list.component';
import { DepotListComponent } from './pages/depot-list/depot-list.component';
import { FournisseurListComponent } from './pages/fournisseur-list/fournisseur-list.component';
import { UtilisateurComponent } from './pages/utilisateur/utilisateur.component';
import { BonDeSortieListComponent } from './pages/bon-de-sortie-list/bon-de-sortie-list.component'; // Liste BDS
import { ChauffeurDashboardComponent } from './components/chauffeur-dashboard/chauffeur-dashboard.component';

// --- Imports de vos nouveaux composants de Commande ---
import { CommandeListComponent } from './components/commande-list/commande-list.component';
import { CommandeDetailComponent } from './components/commande-detail/commande-detail.component';

// --- Imports des nouveaux composants de Bon de Sortie (Détail) ---
import { BonDeSortieDetailComponent } from './components/bon-de-sortie-detail/bon-de-sortie-detail.component'; // Détail BDS

// --- NOUVEAUX IMPORTS POUR LA GESTION DES FACTURES ---
// !!! VOUS DEVEZ CRÉER CES COMPOSANTS !!!
import { FactureListComponent } from './components/facture-list/facture-list.component'; // Ex: Liste des factures
import { FactureDetailComponent } from './components/facture-detail/facture-detail.component'; // Ex: Détails d'une facture


// --- Import de votre garde d'authentification ---
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  // Route par défaut et de connexion
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' }, // Redirige vers le dashboard après connexion
  { path: 'login', component: LoginComponent }, // La page de connexion reste publique

  // === ROUTES PROTÉGÉES (nécessitent l'authentification) ===
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'inventaire',
    component: InventoryComponent,
    canActivate: [authGuard]
  },
  {
    path: 'chauffeurs',
    component: ChauffeurListComponent,
    canActivate: [authGuard]
  },
  {
    path: 'chauffeurs/:id', // Dashboard d'un chauffeur spécifique
    component: ChauffeurDashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'utilisateurs', // Gestion des utilisateurs
    component: UtilisateurComponent,
    canActivate: [authGuard]
  },
  {
    path: 'fournisseurs',
    component: FournisseurListComponent,
    canActivate: [authGuard]
  },
  {
    path: 'depots',
    component: DepotListComponent,
    canActivate: [authGuard]
  },
  {
    path: 'commandes', // Liste des commandes
    component: CommandeListComponent,
    canActivate: [authGuard]
  },
  {
    path: 'commandes/:id', // Détails d'une commande spécifique
    component: CommandeDetailComponent,
    canActivate: [authGuard]
  },
  {
    path: 'bons-de-sortie', // Liste des bons de sortie
    component: BonDeSortieListComponent,
    canActivate: [authGuard]
  },
  {
    path: 'bons-de-sortie/:id', // Détails d'un bon de sortie spécifique
    component: BonDeSortieDetailComponent,
    canActivate: [authGuard]
  },
  // --- NOUVELLES ROUTES POUR LES FACTURES ---
  {
    path: 'factures', // Liste de toutes les factures
    component: FactureListComponent, // Vous devrez créer ce composant
    canActivate: [authGuard]
  },
  {
    path: 'factures/:id', // Détails d'une facture spécifique
    component: FactureDetailComponent, // Vous devrez créer ce composant
    canActivate: [authGuard]
  },
  // La route pour les factures par chauffeur/commande/bds sera gérée via des requêtes API
  // et des modales/navigations depuis FactureListComponent ou CommandeDetail/BonDeSortieDetail

  // Route vers la carte de la Tunisie
  {
    path: 'map',
    loadComponent: () => import('./components/tunisia-map/tunisia-map.component').then(m => m.TunisiaMapComponent),
    canActivate: [authGuard]
  },
  // Route joker (doit être la dernière)
  { path: '**', redirectTo: 'dashboard' } // Si une route n'est pas trouvée, redirige vers le dashboard
];