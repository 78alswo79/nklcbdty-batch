package com.nklcbdty.batch.nklcbdty.batch.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nklcbdty.batch.nklcbdty.batch.user.dto.UserIdAndEmailDto;
import com.nklcbdty.batch.nklcbdty.batch.user.dto.UserResponseDto;
import com.nklcbdty.batch.nklcbdty.batch.user.repository.UserRepository;
import com.nklcbdty.batch.nklcbdty.batch.user.vo.UserVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponseDto findByUserId(String userId) {
        UserVo user = userRepository.findByUserId(userId);
        return UserResponseDto.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .build();
    }

    public List<UserIdAndEmailDto> findByUserIdIn(List<String> userIds) {
        List<UserVo> items = userRepository.findByUserIdIn(userIds);
        List<UserIdAndEmailDto> results = new ArrayList<>();
        for (UserVo item : items) {
            results.add(new UserIdAndEmailDto(item.getUserId(), item.getEmail()));
        }

        return results;
    }
}
