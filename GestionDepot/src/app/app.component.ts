import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { Observable } from 'rxjs';

import { AuthService } from './services/auth.service';
import { SidebarComponent } from './layout/sidebar/sidebar.component'; // Assurez-vous que le chemin est correct

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  public title: string = 'GestionDepot';
  private authService = inject(AuthService);

  // Une variable observable qui sera directement utilisée dans le template
  public isLoggedIn$!: Observable<boolean>;

  ngOnInit(): void {
    // On connecte notre variable à l'observable du service
    this.isLoggedIn$ = this.authService.isLoggedIn$;
  }
}