package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.*;

import java.util.*;
import java.util.concurrent.*;

import org.json.*;

public class SpeechFunction {

    private static final String SPEECH_KEY = System.getenv(   "AZURE_SPEECH_KEY");
    private static final String SPEECH_REGION = System.getenv("AZURE_SPEECH_REGION");

    @FunctionName("GenerateSpeech")
    public HttpResponseMessage generateSpeech(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "speech")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Processing speech generation request");

        try {
            // Parse request
            String requestBody = request.getBody().orElse("");
            JSONObject json = new JSONObject(requestBody);
            String text = json.getString("text");
            String voice = json.optString("voice", "en-US-AriaNeural");

            // Generate speech
            byte[] audioData = synthesizeSpeech(text, voice, context);

            // Return audio file
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(audioData)
                    .header("Content-Type", "audio/mpeg")
                    .header("Cache-Control", "no-cache")
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error generating speech: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to generate speech\"}")
                    .header("Content-Type", "application/json")
                    .build();
        }
    }

    private byte[] synthesizeSpeech(String text, String voice, ExecutionContext context) throws Exception {
        try (SpeechConfig config = SpeechConfig.fromEndpoint(new java.net.URI("https://" + SPEECH_REGION + ".api.cognitive.microsoft.com/"), SPEECH_KEY)) {

            config.setSpeechSynthesisVoiceName(voice);
            config.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio16Khz32KBitRateMonoMp3);

            // Use pull audio output stream
            PullAudioOutputStream outputStream = AudioOutputStream.createPullStream();
            AudioConfig audioConfig = AudioConfig.fromStreamOutput(outputStream);

            try (SpeechSynthesizer synthesizer = new SpeechSynthesizer(config, audioConfig)) {
                // Create SSML for better poetry reading
                String ssml = createPoetrySSML(text, voice);

                // Synthesize
                Future<SpeechSynthesisResult> task = synthesizer.SpeakSsmlAsync(ssml);
                SpeechSynthesisResult result = task.get();

                if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                    return result.getAudioData();
                } else {
                    throw new Exception("Speech synthesis failed: " + result.getReason());
                }
            }
        }
    }

    private String createPoetrySSML(String text, String voice) {
        String[] lines = text.split("\n");
        StringBuilder ssml = new StringBuilder();

        ssml.append("<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='en-US'>");
        ssml.append("<voice name='").append(voice).append("'>");
        ssml.append("<prosody rate='0.9' pitch='+5%'>");

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                ssml.append(line.trim());
                ssml.append("<break time='500ms'/>");
            }
        }

        ssml.append("</prosody></voice></speak>");

        return ssml.toString();
    }
}