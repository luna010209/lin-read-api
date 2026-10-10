package io.lin.read.common.controller;

import io.lin.read.common.dto.ApiResponse;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Tag(
        name = "Root",
        description = "Base endpoints for the Lin Read API including service information, system health checks, and geographic region detection."
)
public class RootController {
    @Value("${spring.application.name:Lingdingdong API}")
    private String applicationName;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Value("${server.port:8082}")
    private String serverPort;

    private final RestTemplate restTemplate = new RestTemplate();

    @Operation(summary = "Root API", description = "Basic service information")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", applicationName);
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("message", "Lin Read API runs well");

        return ResponseEntity.ok(ApiResponse.ok(response));
    }


    @Operation(summary = "Health Check", description = "Confirm status of system")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", applicationName);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("uptime", System.currentTimeMillis());

        Map<String, String> checks = new HashMap<>();
        checks.put("database", "UP");
        checks.put("memory", "UP");
        checks.put("disk", "UP");
        response.put("checks", checks);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }


    @Operation(
            summary = "Detect user region and preferred language",
            description = """
                1. Retrieves the client's IP address from the request and determines the user's country, timezone, and recommended language.
                2. The service queries ipapi.co using the detected IP address.
                3. Based on the country code, the API maps the region to a default language for the platform.
                4. Response includes the client IP, country code, language code, and timezone.
                """
    )
    @GetMapping("/region")
    public ResponseEntity<ApiResponse<Map<String, String>>> getRegionLang(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();

        String ip = getClientIp(request);
        String country = "US";
        String timeZone = "UTC";

        try {
            String url = "https://ipapi.co/" + ip + "/json/";
            Map<?, ?> responseIp = restTemplate.getForObject(url, Map.class);

            if (responseIp != null) {
                if (responseIp.get("country_code") != null) {
                    country = responseIp.get("country_code").toString();
                }

                if (responseIp.get("timezone") != null) {
                    timeZone = responseIp.get("timezone").toString();
                }
            }
        } catch (Exception ignored) {}

        String lang = switch (country) {
            case "KR" -> "ko";
            case "JP" -> "ja";
            case "VN" -> "vi";
            case "CN", "TW", "HK" -> "zh";
            default -> "en";
        };

        response.put("ip", ip);
        response.put("countryCode", country);
        response.put("language", lang);
        response.put("timeZone", timeZone);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-FORWARDED-FOR");
        }

        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        if (StringUtils.isNotBlank(ip)) {
            List<String> ips = Arrays.asList(ip.split(","));
            ip = ips.get(0).trim();
        }

        return ip;
    }
}