import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * L'INTERCEPTEUR UNIQUE QUI GÈRE TOUT.
 * Il est exécuté pour chaque requête HTTP sortante.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();
  const apiUrl = 'http://localhost:9090'; // L'URL de base est définie ici

  // --- LOGIQUE DE MODIFICATION DE LA REQUÊTE ---
  // On clone la requête une seule fois pour appliquer toutes les modifications.
  let modifiedReq = req;

  // 1. AJOUT DE L'URL DE BASE (si l'URL n'est pas déjà complète)
  if (!req.url.startsWith('http')) {
      modifiedReq = modifiedReq.clone({
          url: `${apiUrl}${req.url}`
      });
  }

  // 2. AJOUT DU TOKEN (si un token existe et que ce n'est pas une route de login)
  const isAuthRoute = req.url.includes('/api/auth');
  if (token && !isAuthRoute) {
    modifiedReq = modifiedReq.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
    
  // 3. ON ENVOIE LA REQUÊTE MODIFIÉE ET ON GÈRE LES ERREURS
  return next(modifiedReq).pipe(
    catchError((error: any) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        console.error("Erreur 401: Token invalide ou expiré. Déconnexion.", error);
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};