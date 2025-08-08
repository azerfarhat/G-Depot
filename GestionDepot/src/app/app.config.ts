import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';

// Importez tout ce dont vous avez besoin depuis @angular/common/http
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './interceptors/auth.interceptor'; 

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideCharts(withDefaultRegisterables()),
    
    // === LA CONFIGURATION FINALE ET CORRECTE ===
    // On fait un seul appel à provideHttpClient et on lui passe toutes nos options.
    provideHttpClient(
      withFetch(),
      withInterceptors([authInterceptor]) // On ajoute notre intercepteur ici
    )
  ]
};