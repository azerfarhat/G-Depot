import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BonDeSortieDetailComponent } from './bon-de-sortie-detail.component';

describe('BonDeSortieDetailComponent', () => {
  let component: BonDeSortieDetailComponent;
  let fixture: ComponentFixture<BonDeSortieDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BonDeSortieDetailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BonDeSortieDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
