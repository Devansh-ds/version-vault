package com.version_vault.mapper;

import com.version_vault.models.User;
import com.version_vault.dtos.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(user.getId(), user.getOriginalUsername(), user.getEmail());
    }

}
