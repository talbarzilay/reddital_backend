package com.project.reddital_backend.controllers.api;

import com.project.reddital_backend.DTOs.mappers.UserMapper;
import com.project.reddital_backend.DTOs.models.UserDto;
import com.project.reddital_backend.controllers.requests.LoginRequest;
import com.project.reddital_backend.controllers.requests.UserSignupRequest;
import com.project.reddital_backend.exceptions.BadParametersException;
import com.project.reddital_backend.exceptions.DuplicateEntityException;
import com.project.reddital_backend.exceptions.EntityNotFoundException;
import com.project.reddital_backend.exceptions.UnauthorizedException;
import com.project.reddital_backend.models.User;
import com.project.reddital_backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody @Valid UserSignupRequest userSignupRequest) {
        userSignupRequest.validate();

        return ResponseEntity.created(URI.create("/user/signup"))
                .body(registerUser(userSignupRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody @Valid LoginRequest loginRequest) {
        loginRequest.validate();

        return ResponseEntity.ok()
                .body(loginUser(loginRequest));
    }


    // -------------------------------------- exception handlers --------------------------------------

    @ExceptionHandler(DuplicateEntityException.class)
    public final ResponseEntity<String> handle(DuplicateEntityException ex) {
       return ResponseEntity.unprocessableEntity()
                .body(ex.getMessage());
    }

    @ExceptionHandler(BadParametersException.class)
    public final ResponseEntity<String> handle(BadParametersException ex) {
        return ResponseEntity.badRequest()
                .body(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public final ResponseEntity<String> handle(EntityNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public final ResponseEntity<String> handle(UnauthorizedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }



    // -------------------------------------- private methods --------------------------------------

    /**
     * receive a request ro user registration, and register it
     * @param userSignupRequest the request
     * @throws DuplicateEntityException if user already exist
     * @return the user that was saved to the DB
     */
    private UserDto registerUser(UserSignupRequest userSignupRequest) throws DuplicateEntityException {
        UserDto userDto = new UserDto()
                //.setId(userSignupRequest.getId())
                .setUsername(userSignupRequest.getUsername())
                .setEmail(userSignupRequest.getEmail())
                .setPassword(userSignupRequest.getPassword());

        return userService.signup(userDto).setPassword("");
    }

    /**
     * perform a login
     * @param loginRequest the login request object
     * @return a user DTO of the user that was logged in
     * @throws UnauthorizedException if the credentials were wrong
     * @throws BadParametersException if the parameters if the request were bad
     */
    private UserDto loginUser(LoginRequest loginRequest) throws UnauthorizedException, BadParametersException {
        return userService.login(loginRequest.getUsername(), loginRequest.getPassword()).setPassword("");
    }


}
