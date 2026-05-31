package com.nklcbdty.batch.nklcbdty.batch.user.service;

import org.springframework.stereotype.Service;

import com.nklcbdty.common.user.repository.UserRepository;
import com.nklcbdty.common.user.service.BaseUserService;

// batch 는 BaseUserService 의 조회 메서드만 필요. 추가 동작 없음.
@Service
public class UserService extends BaseUserService {

    public UserService(UserRepository userRepository) {
        super(userRepository);
    }
}
