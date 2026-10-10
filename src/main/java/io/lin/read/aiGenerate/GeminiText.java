package io.lin.read.aiGenerate;

import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import io.lin.read.config.gemini.GeminiProperties;
import io.lin.read.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeminiText {

    private final Client client;
    private final String model;

    public GeminiText(Client client, GeminiProperties props){
        this.client = client;
        this.model = props.models().text();
    }

    public String basicText(String prompt){
        GenerateContentResponse response =
            client.models.generateContent(
                model,
                prompt,
                null
            );
        return response.text();
    }

    /**
     * Multimodal call: text prompt plus inline audio (or other binary) for speaking evaluation.
     */
    public String textWithInlineData(String prompt, byte[] data, String mimeType) {
        if (data == null || data.length == 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "error.evaluate.audio_required");
        }

        Blob blob = Blob.builder()
                .data(data)
                .mimeType(mimeType != null ? mimeType : "application/octet-stream")
                .build();

        Part textPart = Part.fromText(prompt);
        Part dataPart = Part.builder().inlineData(blob).build();

        Content userContent = Content.builder()
                .role("user")
                .parts(List.of(textPart, dataPart))
                .build();

        GenerateContentResponse response =
                client.models.generateContent(model, List.of(userContent), null);

        return response.text();
    }

}
