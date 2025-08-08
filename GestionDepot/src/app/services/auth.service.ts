import { Injectable, inject, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { Router } from '@angular/router';

interface LoginResponse {
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // On utilise une constante pour éviter les fautes de frappe
  private readonly TOKEN_KEY = 'auth_token'; 

  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = 'http://localhost:9090/api/auth';
  
  private isLoggedInSubject = new BehaviorSubject<boolean>(false);
  public isLoggedIn$ = this.isLoggedInSubject.asObservable();

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    if (isPlatformBrowser(this.platformId)) {
      const token = this.getToken();
      if (token) {
        this.isLoggedInSubject.next(true);
      }
    }
  }
  
  login(credentials: { email: string; motDePasse: string }): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        if (response && response.token) {
          this.saveToken(response.token);
          this.isLoggedInSubject.next(true);
        }
      })
    );
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.removeItem(this.TOKEN_KEY);
    }
    this.isLoggedInSubject.next(false);
    this.router.navigate(['/login']); 
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      // CHANGÉ: On lit depuis sessionStorage
      return sessionStorage.getItem(this.TOKEN_KEY);
    }
    return null;
  }
  
  isAuthenticated(): boolean {
    return this.isLoggedInSubject.value;
  }

  private saveToken(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      // CHANGÉ: On sauvegarde dans sessionStorage
      sessionStorage.setItem(this.TOKEN_KEY, token);
    }
  }
}