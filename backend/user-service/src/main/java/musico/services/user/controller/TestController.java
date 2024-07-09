package musico.services.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;


@Slf4j
@Controller
@RequestMapping(path = "/user/test")
@RequiredArgsConstructor
public class TestController {

    @GetMapping(path = "/prova")
    @ResponseBody
    public ResponseEntity<String> test() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("Principal: {}", authentication.getPrincipal());
            log.info("Authorities: {}", authentication.getAuthorities());
            log.info("Credentials: {}", authentication.getCredentials());
            log.info("Details: {}", authentication.getDetails());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
        ResponseEntity<String> res = ResponseEntity.ok("Auth Ok");
        log.info("Response: {}", res);
        return res;
    }
}
