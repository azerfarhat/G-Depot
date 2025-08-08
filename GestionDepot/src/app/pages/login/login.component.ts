import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router'; 
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service'; // Assurez-vous que le chemin est correct

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], 
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm: FormGroup;
  loginError: string | null = null; 

  constructor() {
    // Initialisation du formulaire réactif avec ses contrôles et validateurs
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  // Raccourcis (getters) pour accéder facilement aux contrôles dans le template HTML
  get email() { return this.loginForm.get('email'); }
  get motDePasse() { return this.loginForm.get('motDePasse'); }

  
  //Méthode appelée lors de la soumission du formulaire.
  onSubmit() {
    this.loginError = null;

    // 2. On vérifie si le formulaire est valide (côté client)
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched(); 
      return;
    }

    // 3. On appelle le service d'authentification avec les données du formulaire
    this.authService.login(this.loginForm.value).subscribe({
      
      // 4. Ce qui se passe si la connexion réussit (réponse 2xx du serveur)
      next: () => {
        console.log('Connexion réussie. Redirection vers le tableau de bord...');
        // On redirige l'utilisateur vers la page principale de l'application
        this.router.navigate(['/dashboard']); 
      },
      
      // 5. Ce qui se passe si la connexion échoue (réponse 4xx ou 5xx du serveur)
      error: (err) => {
        console.error('Échec de la connexion :', err);
        // On affiche un message d'erreur clair à l'utilisateur
        this.loginError = 'L\'email ou le mot de passe est incorrect.';
      }
    });
  }
}