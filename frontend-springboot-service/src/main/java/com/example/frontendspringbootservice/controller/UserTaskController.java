package com.example.frontendspringbootservice.controller;

import com.example.frontendspringbootservice.dto.UserTaskEvent;
import com.example.frontendspringbootservice.service.UserTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user-tasks")
public class UserTaskController {

    private final UserTaskService userTaskService;

    public UserTaskController(UserTaskService userTaskService) {
        this.userTaskService = userTaskService;
    }

    @GetMapping
    public List<UserTaskEvent> openTasks() {
        return userTaskService.openTasks();
    }

    @GetMapping("/recent")
    public List<UserTaskEvent> recentEvents() {
        return userTaskService.recentEvents();
    }
}
