import { Component,Input} from '@angular/core';
import { CommonModule } from '@angular/common'; // ⬅️ à importer


import { Alert } from '../../services/dashboard.service';

@Component({
  selector: 'app-alerts-list',
  standalone: true,
  imports: [CommonModule], // ⬅️ ajoute CommonModule ici
  templateUrl: './alerts-list.component.html',
  styleUrl: './alerts-list.component.css'
})
export class AlertsListComponent {
    @Input() alerts: Alert[] = [];

  getIconForAlert(alertType: 'RUPTURE' | 'ALERTE' | 'EXPIRE' | string): string {
    switch (alertType) {
      case 'RUPTURE':
        return 'fa-solid fa-circle-xmark'; // Icône d'erreur/arrêt
      case 'ALERTE':
        return 'fa-solid fa-triangle-exclamation'; // Icône d'avertissement
      case 'EXPIRE':
        return 'fa-solid fa-hourglass-half'; // Icône de temps/expiration
      default:
        return 'fa-solid fa-circle-info'; // Icône d'information par défaut
    }
  }
}
