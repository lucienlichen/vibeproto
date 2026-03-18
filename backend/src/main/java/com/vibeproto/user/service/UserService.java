package com.vibeproto.user.service;

import com.vibeproto.common.model.PageResponse;
import com.vibeproto.user.dto.ChangePasswordRequest;
import com.vibeproto.user.dto.UserCreateRequest;
import com.vibeproto.user.dto.UserUpdateRequest;
import com.vibeproto.user.vo.UserVO;

public interface UserService {
    PageResponse<UserVO> pageUsers(long pageNum, long pageSize);
    UserVO createUser(UserCreateRequest request);
    UserVO updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    void changePassword(Long id, ChangePasswordRequest request);
}
