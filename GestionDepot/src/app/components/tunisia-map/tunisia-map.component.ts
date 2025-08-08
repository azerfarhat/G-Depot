
import { Component, AfterViewInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UtilisateurService } from '../../services/utilisateur.service';
import { UtilisateurSimpleDto } from '../../models/utilisateur.model';
import { LocalisationService, Localisation } from '../../services/localisation.service';
import { DepotService } from '../../services/depot.service';
import { DepotDto } from '../../models/depot.model';
import * as L from 'leaflet';

@Component({
  selector: 'app-tunisia-map',
  imports: [CommonModule, FormsModule],
  templateUrl: './tunisia-map.component.html',
  styleUrl: './tunisia-map.component.css'
})
export class TunisiaMapComponent implements AfterViewInit {
  private map: L.Map | undefined;
  marker: L.Marker | undefined;
  showForm = false;
  locationType = '';
  locationTypes = ['Client', 'Dépôt', 'Autre'];
  lat: number | null = null;
  lng: number | null = null;
  clientId: number | null = null;
  clients: UtilisateurSimpleDto[] = [];
  localisations: Localisation[] = [];
  markers: L.Marker[] = [];

  // Icônes SVG inline (data URI)
  private clientIcon = L.icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32"><circle cx="16" cy="16" r="16" fill="%233497e2"/><circle cx="16" cy="13" r="5" fill="white"/><ellipse cx="16" cy="23" rx="8" ry="5" fill="white"/></svg>',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32]
  });
  private depotIcon = L.icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32"><rect x="4" y="14" width="24" height="12" rx="2" fill="%233497e2"/><rect x="10" y="18" width="4" height="8" fill="white"/><rect x="18" y="18" width="4" height="8" fill="white"/><rect x="2" y="12" width="28" height="4" fill="%23bdbdbd"/></svg>',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32]
  });
  private autreIcon = L.icon({
    iconUrl: 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32"><polygon points="16,2 20,12 31,12 22,19 25,30 16,23 7,30 10,19 1,12 12,12" fill="%233497e2"/></svg>',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32]
  });
  depots: DepotDto[] = [];
  depotId: number | null = null;
  loadDepots() {
    this.depotService.getAllDepots().subscribe(depots => {
      this.depots = depots;
    });
  }


  constructor(
    private utilisateurService: UtilisateurService,
    private localisationService: LocalisationService,
    private depotService: DepotService
  ) {}

  ngAfterViewInit(): void {
    this.initMap();
    this.loadClients();
    this.loadDepots();
    this.loadLocalisations();
  }

  loadLocalisations() {
    this.localisationService.getAll().subscribe(locs => {
      this.localisations = locs;
      this.displayAllMarkers();
    });
  }

  displayAllMarkers() {
    // Supprimer les anciens marqueurs
      this.markers.forEach(m => this.map?.removeLayer(m));
      this.markers = [];
      this.localisations.forEach(loc => {
        let icon = this.autreIcon;
        if (loc.type && loc.type.toUpperCase() === 'CLIENT') icon = this.clientIcon;
        else if (loc.type && (loc.type.toUpperCase() === 'DÉPÔT' || loc.type.toUpperCase() === 'DEPOT')) icon = this.depotIcon;
        const marker = L.marker([loc.lat, loc.lng], { icon }).addTo(this.map!);
        let popupText = `<b>${loc.client?.nom || loc.depot?.nom || 'Localisation'}</b><br>Type: ${loc.type}<br>Lat: ${loc.lat}<br>Lng: ${loc.lng}` +
          `<br><button class='delete-marker-btn' data-id='${loc.id}'>🗑️ Supprimer</button>`;
        marker.bindPopup(popupText);
        marker.on('popupopen', () => {
          setTimeout(() => {
            const btn = document.querySelector(`.delete-marker-btn[data-id='${loc.id}']`);
            if (btn) {
              btn.addEventListener('click', () => this.deleteLocation(loc.id));
            }
          }, 0);
        });
        this.markers.push(marker);
      });
  }

  deleteLocation(id: number | undefined) {
    if (!id) return;
    this.localisationService.delete(id).subscribe(() => {
      this.localisations = this.localisations.filter(l => l.id !== id);
      this.displayAllMarkers();
    });
  }

  loadClients() {
    this.utilisateurService.getAllUsers().subscribe(users => {
      console.log('USERS:', users); // Debug : voir la structure des utilisateurs
      // Accepte toutes les variantes de rôle client
      this.clients = users.filter(u => u.role && u.role.toUpperCase() === 'CLIENT');
    });
  }

  initMap(): void {
    this.map = L.map('map', {
      center: [34.0, 9.0], // Centre Tunisie
      zoom: 7
    });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Utiliser le clic gauche standard pour ajouter un marqueur et afficher le formulaire
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      this.addMarker(e.latlng.lat, e.latlng.lng);
    });
  }

  addMarker(lat: number, lng: number) {
    if (this.marker) {
      this.map?.removeLayer(this.marker);
    }
    // Par défaut, icône "autre" jusqu'à sélection du type
    this.marker = L.marker([lat, lng], { icon: this.autreIcon }).addTo(this.map!);
    this.lat = lat;
    this.lng = lng;
    this.showForm = true;
    this.locationType = '';
    this.clientId = null;
    this.marker.bindPopup('Remplissez le formulaire ci-dessous pour enregistrer la localisation.').openPopup();
  }

  saveLocation() {
    if (!this.lat || !this.lng || !this.locationType || (this.locationType === 'Client' && !this.clientId) || (this.locationType === 'Dépôt' && !this.depotId)) return;
    const localisation: Localisation = {
      lat: this.lat,
      lng: this.lng,
      type: this.locationType,
      client: this.locationType === 'Client' ? { id: this.clientId! } : undefined,
      depot: this.locationType === 'Dépôt' ? { id: this.depotId!, nom: this.depots.find(d => d.id === this.depotId)?.nom || '' } : undefined
    };
    this.localisationService.add(localisation).subscribe(saved => {
      this.localisations.push(saved);
      this.displayAllMarkers();
      this.showForm = false;
      if (this.marker) {
        this.map?.removeLayer(this.marker);
        this.marker = undefined;
      }
    });
  }
}
