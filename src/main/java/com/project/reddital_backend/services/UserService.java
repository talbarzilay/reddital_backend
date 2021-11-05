package com.project.reddital_backend.services;

import com.project.reddital_backend.DTOs.mappers.UserMapper;
import com.project.reddital_backend.DTOs.models.UserDto;
import com.project.reddital_backend.exceptions.DuplicateEntityException;
import com.project.reddital_backend.exceptions.EntityNotFoundException;
import com.project.reddital_backend.models.User;
import com.project.reddital_backend.repositories.UserRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Getter
public class UserService {

    // ------------------------------------------------------- properties -------------------------------------------------------

    @Autowired
    private UserRepository userRepository; // acess DB for user operations

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder; // encrypt and decrypt passwords

    // ------------------------------------------------------- methods -------------------------------------------------------

    /**
     * perform a signup method.
     * @throws DuplicateEntityException if a user with such username already exsists.
     * @param userDto the information on the user to be created
     * @return the user dto that was saved to the DB
     */
    public UserDto signup(UserDto userDto) {
        Optional<User> user = getOptional(userRepository.findByUsername(userDto.getUsername()));

        if (user.isEmpty()) {
            //No such user exit, create the new user object and save it.

            User newUser = new User()
                    .setUsername(userDto.getUsername())
                    .setEmail(userDto.getEmail())
                    .setPassword(userDto.getPassword());

            return saveUser(newUser, true); // save new user, and encrypt its password.
        }

        // user already exist. throw exception.
        throw new DuplicateEntityException("Failed to signup, user already exists : " + user.get());
    }

    /**
     * find and return a user dto by the username
     * @throws EntityNotFoundException if no such user exist
     * @param username the username to search a user by
     * @return the requested user dto
     */
    public UserDto findUserByUsername(String username) {
        Optional<User> user = getOptional(userRepository.findByUsername(username));

        if (user.isPresent()) {
            return mapDto(user.get()); // return requested user
        }

       // user does not exist
       throw new EntityNotFoundException(String.format("The user %s was not found!", username));
    }

    /**
     * find and return a user dto by the id
     * @throws EntityNotFoundException if no such user exist
     * @param id the id to search a user by
     * @return the requested user dto
     */
    public UserDto findUserById(long id) {
        Optional<User> user = getOptional(userRepository.findById(id));

        if (user.isPresent()) {
            return mapDto(user.get()); // return requested user
        }

        // user does not exist
        throw new EntityNotFoundException(String.format("The user with id %d was not found!", id));
    }


    /**
     * update a user profile
     * @throws EntityNotFoundException if no such user exist
     * @param userDto the user with the updated profile info
     * @return the updated user dto in the DB
     */
    public UserDto updateProfile(UserDto userDto) {
        Optional<User> user = getOptional(userRepository.findByUsername(userDto.getUsername()));

        if (user.isPresent()) {
            User userModel = user.get();

            // update requested user info
            userModel
                    .setEmail(userDto.getEmail());

            // save changes and return them
            return saveUser(userModel, false);
        }

        // user does not exist
        throw new EntityNotFoundException(String.format("The user %s was not found!", userDto.getUsername()));
    }

    /**
     * change a user's password
     * @throws EntityNotFoundException if no such user exist
     * @param userDto the user to change its the password
     * @param newPassword the new password (plain text)
     * @return the new updated user's dto
     */
    public UserDto changePassword(UserDto userDto, String newPassword) {
        Optional<User> user = getOptional(userRepository.findByUsername(userDto.getUsername()));

        if (user.isPresent()) {
            User userModel = user.get();
            userModel.setPassword(newPassword); // update the new password

            return saveUser(userModel, true); // update the user info, and encrypt the password
        }

        // user does not exist
        throw new EntityNotFoundException(String.format("The user %s was not found!", userDto.getUsername()));
    }


    // ------------------------------------------------------- private methods -------------------------------------------------------

    /**
     * get an optional of a given user
     * @param user the user
     * @return an optional of the user
     */
    private Optional<User> getOptional(User user) {
        return Optional.ofNullable(user);
    }

    /**
     * map a user model to user dto
     * @param user the user to map
     * @return the user as dto
     */
    private UserDto mapDto(User user) {
        return UserMapper.toUserDto(user);
    }

    /**
     * recives a password and encrypt it
     * @param password the password to encrypt
     * @return the encrypted password
     */
    private String encryptPassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    /**
     * save a user to the DB and return the updated one
     * @throws NullPointerException if the user object is null
     * @param user the user to save
     * @param shouldEncrypt whether the password should be encrypted before saving
     * @return the new updated user's dto
     */
    private UserDto saveUser(User user, boolean shouldEncrypt) {

        if (user == null)
            throw new NullPointerException("Received a null user!");

        if(shouldEncrypt)
            user.setPassword(encryptPassword(user.getPassword())); // encrypt password

        return mapDto(userRepository.save(user));
    }
}
