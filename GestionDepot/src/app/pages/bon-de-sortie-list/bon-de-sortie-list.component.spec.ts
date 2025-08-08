import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BonDeSortieListComponent } from './bon-de-sortie-list.component';

describe('BonDeSortieListComponent', () => {
  let component: BonDeSortieListComponent;
  let fixture: ComponentFixture<BonDeSortieListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BonDeSortieListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BonDeSortieListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
