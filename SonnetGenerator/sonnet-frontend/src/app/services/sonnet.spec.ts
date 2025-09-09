import { TestBed } from '@angular/core/testing';

import { Sonnet } from './sonnet';

describe('Sonnet', () => {
  let service: Sonnet;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Sonnet);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
