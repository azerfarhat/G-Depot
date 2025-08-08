import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { UtilisateurService } from '../../services/utilisateur.service';
import { ChauffeurDashboardData } from '../../models/chauffeur-dashboard.model';

@Component({
  selector: 'app-chauffeur-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyPipe, DatePipe, DecimalPipe],
  templateUrl: './chauffeur-dashboard.component.html',
  styleUrls: ['./chauffeur-dashboard.component.css']
})
export class ChauffeurDashboardComponent implements OnInit {


  dashboardData$!: Observable<ChauffeurDashboardData>;
  chauffeurId!: number;


  constructor(
    private route: ActivatedRoute,
    private utilisateurService: UtilisateurService,
  ) { }

  ngOnInit(): void {
    this.dashboardData$ = this.route.paramMap.pipe(
      switchMap(params => {
        this.chauffeurId = +params.get('id')!;
        return this.utilisateurService.getDashboardData(this.chauffeurId);
      })
    );
  }


}