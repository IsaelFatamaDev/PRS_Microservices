package pe.edu.vallegrande.vgmsusers.infrastructure.rest.common;

import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsusers.application.service.UserService;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ApiResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserRest {

    private final UserService userService;

    public UserRest(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/username/{username}")
    public Mono<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @GetMapping("/email/{email}")
    public Mono<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        return userService.findUserByEmail(email);
    }
}
