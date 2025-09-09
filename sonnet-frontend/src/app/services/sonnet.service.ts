import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface SonnetRequest {
  topic: string;
}

export interface SonnetResponse {
  topic: string;
  sonnet: string;
}

@Injectable({
  providedIn: 'root'
})
export class SonnetService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  generateSonnet(topic: string): Observable<SonnetResponse> {
    return this.http.get<SonnetResponse>(`${this.apiUrl}/sonnet?topic=${topic}`);
  }

  testConnection(): Observable<any> {
    return this.http.get(`${this.apiUrl}/hello`);
  }
}
