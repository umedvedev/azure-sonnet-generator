package com.function;

import com.azure.core.credential.KeyCredential;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.azure.ai.openai.*;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.microsoft.azure.functions.annotation.FunctionName;

import java.util.*;

import static com.azure.ai.openai.models.ChatRole.ASSISTANT;

public class GenerateSonnet {

    private static final String ENDPOINT = System.getenv("AZURE_OPENAI_ENDPOINT");
    private static final String API_KEY = System.getenv("AZURE_OPENAI_KEY");
    private static final String DEPLOYMENT_NAME = "gpt-35-turbo";

    @FunctionName("GenerateSonnet")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "sonnet")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Processing sonnet generation request");

        // Parse request body
        String topic = request.getQueryParameters().get("topic");

        if (topic == null || topic.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Please provide a topic in the request body\"}")
                    .header("Content-Type", "application/json")
                    .build();
        }

        try {
            // Generate sonnet
            String sonnet = generateSonnet(topic);

            // Return response
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(String.format("{\"topic\": \"%s\", \"sonnet\": \"%s\"}",
                            topic, sonnet.replace("\n", "\\n")))
                    .header("Content-Type", "application/json")
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error generating sonnet: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to generate sonnet\"}")
                    .header("Content-Type", "application/json")
                    .build();
        }
    }

    private String extractTopic(String requestBody) {
        // Simple JSON parsing (you might want to use Jackson for production)
        if (requestBody.contains("\"topic\"")) {
            int start = requestBody.indexOf("\"topic\"") + 10;
            int end = requestBody.indexOf("\"", start);
            return requestBody.substring(start, end);
        }
        return null;
    }

    private String generateSonnet(String topic) {
        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(ENDPOINT)
                .credential(new KeyCredential(API_KEY))
                .buildClient();

        String prompt = String.format(
                "Write a short 4-line sonnet about %s. " +
                        "Make it poetic and meaningful. " +
                        "Each line should be roughly the same length.",
                topic
        );

        List<ChatRequestMessage> messages = Arrays.asList(
                new ChatRequestSystemMessage("You are a talented poet who writes beautiful short sonnets."),
                new ChatRequestUserMessage(prompt)
        );

        ChatCompletions completions = client.getChatCompletions(
                DEPLOYMENT_NAME,
                new ChatCompletionsOptions(messages)
                        .setMaxTokens(100)
                        .setTemperature(0.7)
        );

        return completions.getChoices().get(0).getMessage().getContent();
    }
}