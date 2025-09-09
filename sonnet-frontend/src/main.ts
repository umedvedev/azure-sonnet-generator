import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import 'zone.js';

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(withFetch())
  ]
}).catch(err => console.error(err));
