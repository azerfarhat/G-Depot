// --- InventoryComponent.ts ---
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; // Already correctly imported for standalone
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { debounceTime, distinctUntilChanged, startWith, switchMap, tap } from 'rxjs/operators';
import { catchError } from 'rxjs/operators'; 
import { Observable, throwError, Subject } from 'rxjs'; 

import { ProductService, Produit, ProduitUpdateDTO } from '../../services/product.service';
import { ProduitList } from '../../models/produit-list.model'; // Assuming this path
import { StockService, StockLot, NewStockData } from '../../services/stock.service'; 
import { DepotService } from '../../services/depot.service'; 
import { Depot } from '../../models/depot.model'; // Assuming this path
// Corrected import path for Fournisseur model
import { Fournisseur, FournisseurSummary } from '../../models/fournisseur.model'; // Corrected import
import { FournisseurService } from '../../services/fournisseur.service'; // Keep service import separate

@Component({
  selector: 'app-inventory-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], // CommonModule covers *ngFor, *ngIf, and 'number' pipe. ReactiveFormsModule covers form directives.
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.css']
})
export class InventoryComponent implements OnInit {

  // =============================================
  // ===== PROPRIÉTÉS DU COMPOSANT
  // =============================================

  public products: ProduitList[] = [];
  public depots: Depot[] = [];
  public fournisseurs: Fournisseur[] = []; // This will hold the full Fournisseur objects for dropdowns
  public strategieStockOptions = ['FIFO', 'LIFO'];

  public isLoading: boolean = true;
  public isLoadingStock: boolean = false;
  public isAddModalOpen = false;
  public isEditModalOpen = false;
  public isStockModalOpen = false;
  public isAddingStock = false; 

  public addForm!: FormGroup; 
  public editForm!: FormGroup;
  public addStockForm!: FormGroup;
  public searchControl = new FormControl('');

  public currentEditingProduct: ProduitList | null = null;
  public currentProductForStock: ProduitList | null = null;
  public stockDetails: StockLot[] = [];
  
  // =========================================================
  // === DÉCLARATIONS CORRIGÉES : MESSAGES AU NIVEAU DE LA PAGE PRINCIPALE ===
  // =========================================================
  public pageSuccessMessage: string | null = null;
  public pageErrorMessage: string | null = null;

  // =========================================================
  // === DÉCLARATIONS CORRIGÉES : MESSAGES POUR LES MODALES ===
  // =========================================================
  public addModalMessage: string | null = null; 
  public editModalMessage: string | null = null; 
  public stockModalMessage: string | null = null; 

  // =========================================================
  // === DÉCLARATIONS CORRIGÉES : Propriétés pour la modale de CONFIRMATION ===
  // =========================================================
  public isConfirmModalOpen: boolean = false;
  public confirmMessage: string = '';
  private confirmSubject: Subject<boolean> = new Subject<boolean>();


  // =============================================
  // ===== CONSTRUCTEUR ET CYCLE DE VIE
  // =============================================
  constructor(
    private productService: ProductService,
    private stockService: StockService, 
    private depotService: DepotService,
    private fournisseurService: FournisseurService,
    private router: Router,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initializeForms();
    this.setupSearch();
    this.loadDropdownData();
  }

  // =============================================
  // ===== MÉTHODES D'INITIALISATION
  // =============================================

  private initializeForms(): void {
    this.addForm = this.fb.group({
      nom: ['', Validators.required],
      description: ['', Validators.required],
      stockMinimum: [10, [Validators.required, Validators.min(0)]],
      categorie: [''], 
      strategieStock: [this.strategieStockOptions[0], Validators.required]
    });

    this.editForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      strategieStock: ['', Validators.required]
    });
    
    this.addStockForm = this.fb.group({
      quantite: [1, [Validators.required, Validators.min(1)]], 
      prixVenteHTVA: [0.01, [Validators.required, Validators.min(0.01)]], 
      prixAchat: [0.01, [Validators.required, Validators.min(0.01)]], 
      dateExpiration: [this.getTodayDateString(), Validators.required], 
      codeBarre: ['', Validators.required],
      seuilMin: [10, [Validators.required, Validators.min(0)]],
      depotId: [null, Validators.required], 
      fournisseurid: [null, Validators.required] 
    });
  }

  private getTodayDateString(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = (today.getMonth() + 1).toString().padStart(2, '0');
    const day = today.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }


  private setupSearch(): void {
    this.searchControl.valueChanges.pipe(
      startWith(''), 
      debounceTime(400), 
      distinctUntilChanged(), 
      tap(() => {
        this.isLoading = true;
        this.clearPageMessages(); 
      }), 
      switchMap(term => this.productService.getProductsForList(term || '')) 
    ).subscribe({
      next: (data: ProduitList[]) => { // Explicitly type 'data'
        this.products = data;
        this.isLoading = false;
      },
      error: (err: HttpErrorResponse) => { // Explicitly type 'err'
        console.error('Erreur lors de la recherche:', err);
        this.isLoading = false;
        this.products = [];
        this.pageErrorMessage = err.message || 'Erreur lors du chargement des produits.';
        this.scheduleMessageClear(5000);
      }
    });
  }

  private loadDropdownData(): void {
    this.depotService.getAllDepots().pipe(
      catchError((err: HttpErrorResponse) => { // Explicitly type 'err'
        console.error('Erreur chargement dépôts:', err);
        this.pageErrorMessage = 'Erreur lors du chargement des dépôts.';
        this.scheduleMessageClear(5000);
        return throwError(() => new Error(err.message));
      })
    ).subscribe((data: Depot[]) => this.depots = data); // Explicitly type 'data'

    // Corrected to use getAllFournisseurs() as per FournisseurService definition
    this.fournisseurService.getAllFournisseurs().pipe( // Changed getFournisseurs() to getAllFournisseurs()
      catchError((err: HttpErrorResponse) => { // Explicitly type 'err'
        console.error('Erreur chargement fournisseurs:', err);
        this.pageErrorMessage = 'Erreur lors du chargement des fournisseurs.';
        this.scheduleMessageClear(5000);
        return throwError(() => new Error(err.message));
      })
    ).subscribe((data: Fournisseur[]) => this.fournisseurs = data); // Explicitly type 'data'
  }

  // =============================================
  // ===== GESTION DE LA MODALE D'AJOUT DE PRODUIT
  // =============================================
  openAddModal(): void { 
    this.isAddModalOpen = true; 
    // Réinitialise le formulaire à des valeurs par défaut valides
    this.addForm.reset({ 
      nom: '', 
      description: '', 
      stockMinimum: 10, 
      categorie: '', 
      strategieStock: this.strategieStockOptions[0] 
    }); 
    this.addModalMessage = null; 
    this.clearPageMessages(); 
  }
  closeAddModal(): void { this.isAddModalOpen = false; this.addForm.reset({ stockMinimum: 10, strategieStock: this.strategieStockOptions[0], nom: '', description: '', categorie: '' }); }
  onAddSubmit(): void {
    if (this.addForm.invalid) { 
      this.addForm.markAllAsTouched(); 
      this.addModalMessage = 'Veuillez corriger les erreurs du formulaire.';
      return; 
    }
    
    this.addModalMessage = null; 

    this.productService.ajouterProduit(this.addForm.value).subscribe({
      next: () => {
        this.pageSuccessMessage = 'Produit ajouté avec succès !';
        this.closeAddModal();
        this.refreshSearch(); 
        this.scheduleMessageClear(3000); 
      },
      error: (err: HttpErrorResponse) => { // Explicitly type 'err'
        this.pageErrorMessage = `Erreur lors de l'ajout: ${err.message}`; 
        this.scheduleMessageClear(5000);
      }
    });
  }

  // =============================================
  // ===== GESTION DE LA MODALE D'ÉDITION DE PRODUIT
  // =============================================
  openEditModal(product: ProduitList): void {
    this.currentEditingProduct = product;
    // Réinitialise le formulaire avec les valeurs existantes du produit.
    this.editForm.reset({ 
      nom: product.nom, 
      description: product.description, 
      strategieStock: product.strategieStock 
    });
    this.isEditModalOpen = true;
    this.editModalMessage = null; 
    this.clearPageMessages(); 
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.currentEditingProduct = null; }
  onEditSubmit(): void {
    this.editForm.markAllAsTouched(); 

    // IMPORTANT : Le bouton est désactivé si le formulaire est invalide OU s'il n'a pas été modifié.
    if (this.editForm.invalid || !this.editForm.dirty || !this.currentEditingProduct) { 
      this.editModalMessage = 'Veuillez corriger les erreurs du formulaire ou apporter des modifications.';
      return; 
    }
    
    this.editModalMessage = null; 

    this.productService.updateProduct(this.currentEditingProduct.id, this.editForm.value).subscribe({
      next: () => {
        this.pageSuccessMessage = 'Produit mis à jour avec succès !';
        this.closeEditModal();
        this.refreshSearch(); 
        this.scheduleMessageClear(3000);
      },
      error: (err: HttpErrorResponse) => { // Explicitly type 'err'
        this.pageErrorMessage = `Erreur lors de la mise à jour: ${err.message}`; 
        this.scheduleMessageClear(5000);
      }
    });
  }

  // =============================================
  // ===== GESTION DE LA MODALE DE STOCK
  // =============================================
  openStockModal(product: ProduitList): void {
    this.currentProductForStock = product;
    this.isStockModalOpen = true;
    this.isLoadingStock = true;
    this.isAddingStock = false;
    this.stockModalMessage = null; 
    this.clearPageMessages(); 

    this.addStockForm.reset({
      quantite: 1, 
      prixVenteHTVA: product.dernierPrixVenteHTVA || 0.01,
      prixAchat: 0.01,
      dateExpiration: this.getTodayDateString(),
      codeBarre: '', 
      seuilMin: product.stockMinimum || 10,
      depotId: null, 
      fournisseurid: null
    });

    this.stockService.getStockByProductId(product.id).subscribe({
      next: (data: StockLot[]) => { this.stockDetails = data; this.isLoadingStock = false; }, // Explicitly type 'data'
      error: (err: HttpErrorResponse) => { // Explicitly type 'err'
        this.stockModalMessage = `Erreur lors du chargement du stock: ${err.message}`; 
        this.isLoadingStock = false; 
      }
    });
  }
  closeStockModal(): void { this.isStockModalOpen = false; this.currentProductForStock = null; this.stockDetails = []; this.addStockForm.reset(); }
  onAddStockSubmit(): void {
    if (this.addStockForm.invalid || !this.currentProductForStock) { 
      this.addStockForm.markAllAsTouched();
      this.stockModalMessage = 'Veuillez corriger les erreurs du formulaire d\'ajout de stock.';
      return; 
    }
    
    this.stockModalMessage = null; 

    const newStockData: NewStockData = { produitId: this.currentProductForStock.id, ...this.addStockForm.value };
    const newPrice = parseFloat(newStockData.prixVenteHTVA as any);
    const oldPrice = this.currentProductForStock.dernierPrixVenteHTVA;
    
    if (oldPrice != null && newPrice !== oldPrice) {
      this.openConfirmModal(`Le prix de vente a changé (Ancien: ${oldPrice}TND, Nouveau: ${newPrice}TND).\nVoulez-vous définir ce nouveau prix comme prix par défaut ?`).subscribe(confirmed => {
        if (confirmed) {
          this.updateProductPriceAndAddStock(newStockData);
        } else {
          this.addStockOnly(newStockData); 
        }
      });
    } else {
      this.addStockOnly(newStockData);
    }
  }

  // --- Logique d'ajout de stock ---
  private addStockOnly(stockData: NewStockData): void {
    this.stockService.addStock(stockData).subscribe({
      next: () => { 
        this.stockModalMessage = 'Nouveau stock ajouté !'; 
        this.finalizeStockAddition(); 
      },
      error: (err: HttpErrorResponse) => { // Explicitly type 'err'
        this.stockModalMessage = `Erreur ajout stock: ${err.message}`; 
      }
    });
  }
  private updateProductPriceAndAddStock(stockData: NewStockData): void {
    if (!this.currentProductForStock) return;
    const productUpdateDto: ProduitUpdateDTO = {
      nom: this.currentProductForStock.nom,
      description: this.currentProductForStock.description,
      strategieStock: this.currentProductForStock.strategieStock,
      prixVenteParDefaut: stockData.prixVenteHTVA
    };
    this.productService.updateProduct(this.currentProductForStock.id, productUpdateDto).subscribe({
      next: () => { 
        console.log('Prix par défaut du produit mis à jour.'); 
        this.addStockOnly(stockData); 
      },
      error: (err: HttpErrorResponse) => { // Explicitly type 'err'
        this.stockModalMessage = `Erreur mise à jour produit: ${err.message}`; 
      }
    });
  }
  private finalizeStockAddition(): void {
    this.addStockForm.reset({ 
      depotId: null, 
      fournisseurid: null, 
      seuilMin: 10, 
      quantite: 1, 
      prixVenteHTVA: 0.01, 
      prixAchat: 0.01, 
      dateExpiration: this.getTodayDateString(),
      codeBarre: '' 
    });
    this.isAddingStock = false;
    this.refreshStockDetails(); 
    this.refreshSearch(); 
  }

  private refreshStockDetails(): void {
    if (this.currentProductForStock) {
      this.isLoadingStock = true;
      this.stockService.getStockByProductId(this.currentProductForStock.id).subscribe({
        next: (data: StockLot[]) => { this.stockDetails = data; this.isLoadingStock = false; }, // Explicitly type 'data'
        error: (err: HttpErrorResponse) => { // Explicitly type 'err'
          this.stockModalMessage = `Erreur lors du rechargement du stock: ${err.message}`; 
          this.isLoadingStock = false; 
        }
      });
    }
  }

  // =============================================
  // ===== ACTIONS DÉCLENCHÉES DEPUIS LA TABLE
  // =============================================
  viewStockDetails(product: ProduitList): void { this.openStockModal(product); }
  editProduct(product: ProduitList): void { this.openEditModal(product); }
  deleteProduct(id: number, name: string): void {
    
    this.clearPageMessages(); 

    this.openConfirmModal(`Êtes-vous sûr de vouloir supprimer "${name}" ?`).subscribe(confirmed => {
      if (confirmed) {
        this.productService.deleteProduct(id).subscribe({
          next: (msg: string) => { // Explicitly type 'msg'
            this.pageSuccessMessage = msg; 
            this.refreshSearch(); 
            this.scheduleMessageClear(3000);
          },
          error: (err: HttpErrorResponse) => { // Explicitly type 'err'
            this.pageErrorMessage = `Erreur: ${err.message}`; 
            this.scheduleMessageClear(5000);
          }
        });
      } else {
        this.pageErrorMessage = 'Suppression annulée.';
        this.scheduleMessageClear(3000);
      }
    });
  }

  /**
   * Rafraîchit la liste des produits en relançant la recherche avec le terme actuel.
   */
  private refreshSearch(): void {
    // Recharge tous les produits sans filtre pour garantir le rafraîchissement après ajout
    this.isLoading = true;
    this.productService.getProductsForList('').subscribe({
      next: (data: ProduitList[]) => {
        this.products = data;
        this.isLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        this.products = [];
        this.pageErrorMessage = err.message || 'Erreur lors du chargement des produits.';
        this.scheduleMessageClear(5000);
      }
    });
  }

  // =========================================================
  // === Méthodes utilitaires pour les messages ===
  // =========================================================

  /**
   * Efface les messages de succès et d'erreur de la page principale.
   */
  private clearPageMessages(): void {
    this.pageSuccessMessage = null;
    this.pageErrorMessage = null;
  }

  /**
   * Programme l'effacement d'un message de la page après un certain délai.
   */
  private scheduleMessageClear(delay: number): void {
    setTimeout(() => {
      this.pageSuccessMessage = null;
      this.pageErrorMessage = null;
    }, delay);
  }

  // =========================================================
  // === Logique de la modale de CONFIRMATION ===
  // =========================================================
  openConfirmModal(message: string): Observable<boolean> {
    this.confirmMessage = message;
    this.isConfirmModalOpen = true;
    this.confirmSubject = new Subject<boolean>(); 
    return this.confirmSubject.asObservable();
  }

  confirmAction(confirmed: boolean): void {
    this.isConfirmModalOpen = false;
    this.confirmSubject.next(confirmed); 
    this.confirmSubject.complete(); 
  }
}