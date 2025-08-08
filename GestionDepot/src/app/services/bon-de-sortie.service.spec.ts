import { TestBed } from '@angular/core/testing';

import { BonDeSortieService } from './bon-de-sortie.service';

describe('BonDeSortieService', () => {
  let service: BonDeSortieService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BonDeSortieService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
