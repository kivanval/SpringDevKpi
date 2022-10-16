package com.example.springdevkpi.web;

import com.example.springdevkpi.service.UserService;
import com.example.springdevkpi.web.data.transfer.PostPayload;
import com.example.springdevkpi.web.data.transfer.TopicPayload;
import com.example.springdevkpi.web.data.transfer.UserPayload;
import com.example.springdevkpi.web.data.transfer.UserUpdatePayload;
import org.hibernate.validator.constraints.Range;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;


    private final ModelMapper modelMapper;

    @Autowired
    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    private static final String USER_PROPERTIES = "id|username|createdAt|role_id";

    @GetMapping("/")
    public Collection<UserPayload> getAll(
            @RequestParam(defaultValue = "0") @Min(0) final int page,
            @RequestParam(defaultValue = "20") @Range(min = 0, max = 1000) final int size,
            @RequestParam(defaultValue = "username") @Pattern(regexp = USER_PROPERTIES) final String sortBy) {
        return userService.findAll(page, size, sortBy)
                .stream()
                .map(user -> modelMapper.map(user, UserPayload.class))
                .collect(Collectors.toSet());
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserPayload> getByUsername(
            @PathVariable @Size(min = 5, max = 255) final String username) {
        var optUser = userService.findByUsername(username);
        return optUser.map(user -> ResponseEntity.ok(modelMapper.map(user, UserPayload.class)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/{username}/topics")
    public ResponseEntity<Set<TopicPayload>> getTopicsByUsername(
            @PathVariable @Size(min = 5, max = 255) final String username) {
        var optUser = userService.findByUsername(username);
        return optUser.map(user -> ResponseEntity.ok(user.getTopics().stream()
                        .map(topic -> modelMapper.map(topic, TopicPayload.class))
                        .collect(Collectors.toSet())))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/{username}/posts")
    public ResponseEntity<Set<PostPayload>> addPostsByUsername(
            @PathVariable @Size(min = 5, max = 255) final String username) {
        var optUser = userService.findByUsername(username);
        return optUser.map(user -> ResponseEntity.ok(user.getPosts().stream()
                        .map(post -> modelMapper.map(post, PostPayload.class))
                        .collect(Collectors.toSet())))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<UserPayload> deleteById(
            @PathVariable @Size(min = 5, max = 255) final String username) {
        userService.deleteByUsername(username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{username}")
    public ResponseEntity<UserPayload> update(
            @RequestBody @Valid final UserUpdatePayload payload,
            @PathVariable @Size(min = 5, max = 255) final String username) {
        return userService.update(payload, username) ?
                ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
    }
}
