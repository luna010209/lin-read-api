package io.lin.read.feature.user;

import io.lin.read.common.security.CurrentUserContext;
import io.lin.read.config.auth.AuthApiClient;
import io.lin.read.config.auth.dto.AuthProfile;
import io.lin.read.exception.CustomException;
import io.lin.read.feature.user.dto.MeResponse;
import io.lin.read.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeService {

    private static final Logger log = LoggerFactory.getLogger(MeService.class);

    private final AuthApiClient authApiClient;

    public MeResponse getCurrentUser() {
        AuthenticatedUser jwtUser = CurrentUserContext.require();
        AuthProfile profile = resolveProfile(jwtUser);
        return MeResponse.from(profile);
    }

    private AuthProfile resolveProfile(AuthenticatedUser jwtUser) {
        try {
            AuthProfile profile = authApiClient.fetchProfile();
            if (profile.id() != null && !profile.id().equals(jwtUser.id())) {
                throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.invalid");
            }
            return profile;
        } catch (CustomException ex) {
            if (ex.getStatus() != HttpStatus.UNAUTHORIZED) {
                throw ex;
            }
            log.warn("Auth profile fetch failed for user {} — falling back to JWT claims", jwtUser.id());
            return AuthProfile.fromJwt(jwtUser);
        }
    }
}
