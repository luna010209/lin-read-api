package io.lin.read.aiGenerate;

import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.*;
import io.lin.read.config.gemini.GeminiProperties;
import io.lin.read.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class GeminiImage {

    private static final String ASPECT_RATIO = "4:3";

    /** Target width; height follows API aspect ratio after resize. */
    private static final int OUTPUT_WIDTH = 1024;

    private final Client client;
    private final String model;

    public GeminiImage(Client client, GeminiProperties props){
        this.client = client;
        this.model = props.models().image();
    }

    public byte[] basicImage(String prompt) {
        List<Content> contents = List.of(
                Content.builder()
                        .role("user")
                        .parts(List.of(Part.fromText(prompt)))
                        .build()
        );

        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(List.of("IMAGE", "TEXT"))
                .imageConfig(ImageConfig.builder()
                        .aspectRatio(ASPECT_RATIO)
                        .build())
                .build();

        ResponseStream<GenerateContentResponse> stream =
                client.models.generateContentStream(model, contents, config);

        try {
            for (GenerateContentResponse res : stream) {
                if (res.candidates().isEmpty()) continue;

                var contentOpt = res.candidates().get().get(0).content();
                if (contentOpt.isEmpty()) continue;

                for (Part part : contentOpt.get().parts().orElse(List.of())) {
                    if (part.inlineData().isPresent()) {
                        Blob blob = part.inlineData().get();
                        byte[] imageBytes = blob.data().orElseThrow(
                                () -> new CustomException(HttpStatus.BAD_REQUEST, "error.ai.no_image")
                        );

                        return toJpeg(imageBytes);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Gemini image generation failed", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "error.ai.image_generation_failed");
        } finally {
            stream.close();
        }

        throw new CustomException(HttpStatus.BAD_REQUEST, "error.ai.no_image");
    }

    private byte[] toJpeg(byte[] imageBytes) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Thumbnails.of(inputStream)
                    .width(OUTPUT_WIDTH)
                    .keepAspectRatio(true)
                    .outputQuality(0.7)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        }
    }

}
