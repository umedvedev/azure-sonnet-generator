import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { firstValueFrom } from 'rxjs';

export interface SpeechVoice {
shortName: string;
locale: string;
}

@Injectable({
providedIn: 'root'
})
export class SpeechService {
private audio: HTMLAudioElement | null = null;
private isPlaying: boolean = false;
private selectedVoice: string = 'en-US-AriaNeural';

constructor(private http: HttpClient) {}

  async speakText(text: string, voice: string = 'en-US-AriaNeural'): Promise<void> {
    try {
      // Stop any existing audio
      this.stop();

      // Request speech from backend
      const audioBlob = await this.getSpeechAudio(text, voice);

      // Create audio URL and play
      const audioUrl = URL.createObjectURL(audioBlob);
      this.audio = new Audio(audioUrl);
      this.isPlaying = true;

      // Set up event handlers
      this.audio.onended = () => {
        this.isPlaying = false;
        URL.revokeObjectURL(audioUrl);
      };

      this.audio.onerror = () => {
        this.isPlaying = false;
        URL.revokeObjectURL(audioUrl);
        throw new Error('Failed to play audio');
      };

      // Play the audio
      await this.audio.play();

    } catch (error) {
      this.isPlaying = false;
      throw error;
    }
  }

  private async getSpeechAudio(text: string, voice: string): Promise<Blob> {
    const response = await firstValueFrom(
      this.http.post(`${environment.apiUrl}/speech`,
        { text, voice },
        { responseType: 'blob' }
)
);
return response;
}

stop(): void {
    if (this.audio) {
      this.audio.pause();
      this.audio.currentTime = 0;
      this.audio = null;
      this.isPlaying = false;
    }
  }

  getIsPlaying(): boolean {
    return this.isPlaying;
  }

  // Since we're using backend speech synthesis now, we don't need these methods
  // But if you want to keep them for future use, here they are with proper types:

  async getVoices(): Promise<string[]> {
    // Since we're not using the Speech SDK directly anymore,
    // just return a hardcoded list of available voices
    return [
      'en-US-AriaNeural',
      'en-US-GuyNeural',
      'en-US-JennyNeural',
      'en-GB-SoniaNeural'
    ];
  }

  setVoice(voiceName: string): void {
    // This is now handled by passing the voice parameter to the backend

    console.log(`Voice set to: ${voiceName}`);
  }
}
