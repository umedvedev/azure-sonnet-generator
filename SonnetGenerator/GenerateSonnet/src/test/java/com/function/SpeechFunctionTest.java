package com.function;

import com.microsoft.azure.functions.*;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class SpeechFunctionTest {


    //@Test
    void test() {

        SpeechFunction speechFunction = new SpeechFunction();
        HttpRequestMessage<Optional<String>> req = new HttpRequestMessage<Optional<String>>() {
            @Override
            public URI getUri() {
                return null;
            }

            @Override
            public HttpMethod getHttpMethod() {
                return HttpMethod.POST;
            }

            @Override
            public Map<String, String> getHeaders() {
                return Map.of();
            }

            @Override
            public Map<String, String> getQueryParameters() {
                return Map.of();
            }

            @Override
            public Optional<String> getBody() {
                return Optional.of("{\n" +
                        "\t\"text\":\"test\"\n" +
                        "}");
            }

            @Override
            public HttpResponseMessage.Builder createResponseBuilder(HttpStatus httpStatus) {
                return null;
            }

            @Override
            public HttpResponseMessage.Builder createResponseBuilder(HttpStatusType httpStatusType) {
                return null;
            }
        };
        speechFunction.generateSpeech(req, new ExecutionContext() {
            @Override
            public Logger getLogger() {
                return Logger.getLogger("speech");
            }

            @Override
            public String getInvocationId() {
                return "111";
            }

            @Override
            public String getFunctionName() {
                return "generateSpeech";
            }
        });

    }

}