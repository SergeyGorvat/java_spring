package spring.mvc.shop.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring.mvc.shop.config.Views;
import spring.mvc.shop.dto.user.CreateUserDto;
import spring.mvc.shop.dto.user.UpdateUserDto;
import spring.mvc.shop.entity.User;
import spring.mvc.shop.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @JsonView(Views.UserSummary.class)
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @JsonView(Views.UserDetails.class)
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    @JsonView(Views.UserDetails.class)
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserDto dto) {
        User created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PatchMapping("/{id}")
    @JsonView(Views.UserDetails.class)
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDto dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
