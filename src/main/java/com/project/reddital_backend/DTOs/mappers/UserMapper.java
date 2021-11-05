package com.project.reddital_backend.DTOs.mappers;

import com.project.reddital_backend.DTOs.models.UserDto;
import com.project.reddital_backend.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public static UserDto toUserDto(User user) {

        if(user == null)
            return null;

        return new UserDto()
                .setId(user.getId())
                .setEmail(user.getEmail())
                .setUsername(user.getUsername())
                .setPassword(user.getPassword());
    }

}