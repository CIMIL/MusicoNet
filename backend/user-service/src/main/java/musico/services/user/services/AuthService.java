package musico.services.user.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import musico.services.user.utils.SessionStorage;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final RestTemplate restTemplate;
    private final SessionStorage sessionStorage;
}
