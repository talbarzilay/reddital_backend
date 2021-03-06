package com.project.reddital_backend.controllers.api;

import com.project.reddital_backend.DTOs.mappers.UserMapper;
import com.project.reddital_backend.DTOs.models.UserDto;
import com.project.reddital_backend.controllers.requests.LoginRequest;
import com.project.reddital_backend.controllers.requests.SignupRequest;
import com.project.reddital_backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @CrossOrigin
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody @Valid SignupRequest signupRequest) {
        signupRequest.validate();

        return ResponseEntity.created(URI.create("/user/signup"))
                .body(registerUser(signupRequest));
    }

    @CrossOrigin
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody @Valid LoginRequest loginRequest) {
        loginRequest.validate();

        return ResponseEntity.ok()
                .body(loginUser(loginRequest));
    }



    // -------------------------------------- private methods --------------------------------------

    /**
     * receive a request ro user registration, and register it
     * @param signupRequest the request
     * @return the user that was saved to the DB
     */
    private UserDto registerUser(SignupRequest signupRequest) {
        return userService.signup(userMapper.toUserDto(signupRequest));
    }

    /**
     * perform a login
     * @param loginRequest the login request object
     * @return a user DTO of the user that was logged in
     */
    private UserDto loginUser(LoginRequest loginRequest) {
        return userService.login(loginRequest.getUsername(), loginRequest.getPassword());
    }


}
