package io.lin.read.aiGenerate.dto;

public record AudioResult(
        byte[] bytes,
        String mimeType
) {
}
