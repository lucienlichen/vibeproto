package com.vibeproto.user.controller;

import com.vibeproto.common.api.Result;
import com.vibeproto.common.model.PageResponse;
import com.vibeproto.user.dto.ChangePasswordRequest;
import com.vibeproto.user.dto.UserCreateRequest;
import com.vibeproto.user.dto.UserUpdateRequest;
import com.vibeproto.user.service.UserService;
import com.vibeproto.user.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<PageResponse<UserVO>> listUsers(
        @RequestParam(defaultValue = "1") long pageNum,
        @RequestParam(defaultValue = "20") long pageSize) {
        return Result.success(userService.pageUsers(pageNum, pageSize));
    }

    @PostMapping
    public Result<UserVO> createUser(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public Result<UserVO> updateUser(@PathVariable Long id,
                                     @RequestBody UserUpdateRequest request) {
        return Result.success(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/password")
    public Result<Void> changePassword(@PathVariable Long id,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return Result.success(null);
    }
}
