import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service'; // Assurez-vous que ce chemin est correct

export const authGuard: CanActivateFn = (route, state) => {

  const authService = inject(AuthService);
  const router = inject(Router);

  // On utilise la méthode de votre service pour savoir si l'utilisateur est connecté
  if (authService.isAuthenticated()) {
    // Si oui, on autorise l'accès à la route. L'utilisateur peut continuer.
    return true;
  } else {
    // Si non, on bloque la navigation et on redirige l'utilisateur vers la page de connexion.
    console.warn('Accès non autorisé, redirection vers /login');
    router.navigate(['/login']);
    return false;
  }
};