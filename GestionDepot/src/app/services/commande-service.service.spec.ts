import { TestBed } from '@angular/core/testing';

import { CommandeServiceService } from './commande-service.service';

describe('CommandeServiceService', () => {
  let service: CommandeServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CommandeServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
