import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { SonnetService } from './services/sonnet.service';
import { SpeechService } from './services/speech.service';

@Component({
selector: 'app-root',
standalone: true,
imports: [CommonModule, FormsModule, HttpClientModule],
templateUrl: './app.component.html',
styleUrls: ['./app.component.css']
})
export class AppComponent implements OnDestroy {
topic: string = '';
sonnet: string = '';
loading: boolean = false;
error: string = '';
copySuccess: boolean = false;
isSpeaking: boolean = false;
selectedVoice: string = 'en-US-AriaNeural';
availableVoices: string[] = [
  'en-US-AriaNeural',
  'en-US-GuyNeural',
  'en-US-JennyNeural',
  'en-GB-SoniaNeural'
];

constructor(
    private sonnetService: SonnetService,
    private speechService: SpeechService
  ) {
  }

  ngOnDestroy(): void {
    this.speechService.stop();
  }

  generateSonnet(): void {
    if (!this.topic.trim()) return;

    this.loading = true;
    this.error = '';
    this.sonnet = '';
    this.copySuccess = false;
    this.speechService.stop(); // Stop any ongoing speech

    this.sonnetService.generateSonnet(this.topic).subscribe({
      next: (response) => {
        this.sonnet = response.sonnet;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        if (error.error?.error) {
          this.error = error.error.error;
        } else if (error.status === 0) {
          this.error = 'Cannot connect to the server. Please check if the API is running.';
        } else {
          this.error = 'An error occurred while generating the sonnet. Please try again.';
        }
        console.error('Error:', error);
      }
    });
  }

  async speakSonnet(): Promise<void> {
    if (!this.sonnet) return;

    try {
      this.isSpeaking = true;
      await this.speechService.speakText(this.sonnet, this.selectedVoice);
      this.isSpeaking = false;
    } catch (error) {
      this.isSpeaking = false;
      this.error = 'Failed to read the sonnet. Please try again.';
      console.error('Speech error:', error);
    }
  }

  stopSpeaking(): void {
    this.speechService.stop();
    this.isSpeaking = false;
  }

  onVoiceChange(): void {
    this.speechService.setVoice(this.selectedVoice);
  }

  async copySonnet(): Promise<void> {
    try {
      await navigator.clipboard.writeText(this.sonnet);
      this.copySuccess = true;
      setTimeout(() => {
        this.copySuccess = false;
      }, 3000);
    } catch (err) {
      console.error('Failed to copy:', err);
    }
  }

  clearSonnet(): void {
    this.sonnet = '';
    this.topic = '';
    this.copySuccess = false;
    this.error = '';
    this.speechService.stop();
    this.isSpeaking = false;
  }
}
