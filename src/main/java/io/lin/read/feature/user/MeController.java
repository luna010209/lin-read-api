package io.lin.read.feature.user;

import io.lin.read.common.dto.ApiResponse;
import io.lin.read.feature.user.dto.MeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/me")
@Tag(name = "01. Profile")
public class MeController {

    private final MeService meService;

    @GetMapping
    @Operation(
            summary = "Current user profile",
            description = """
                    Returns account profile from lin-auth-api.
                    Requires Authorization: Bearer access token issued by lin-auth-api.
                    """
    )
    public ResponseEntity<ApiResponse<MeResponse>> currentUser() {
        return ResponseEntity.ok(ApiResponse.ok(meService.getCurrentUser()));
    }
}
