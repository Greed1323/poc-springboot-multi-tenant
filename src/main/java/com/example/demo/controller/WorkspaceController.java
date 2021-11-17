package com.example.demo.controller;

import com.example.demo.dao.common.WorkspaceEntity;
import com.example.demo.service.WorkspaceService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/workspaces")
    public ResponseEntity<List<WorkspaceEntity>> getWorkspaces() {
        return ResponseEntity.ok(workspaceService.getAllCommonWorkspaces());
    }

    @GetMapping("/users")
    public ResponseEntity<UserDto> getTenantWorkspace() {
        return ResponseEntity.ok(workspaceService.getAllUsers());
    }

    @GetMapping("/duplicate")
    public ResponseEntity<Void> duplicateUsers() {
        workspaceService.copyUsers();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/slow")
    public ResponseEntity<Void> slow() {
        workspaceService.slowRequestTenant();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/quick")
    public ResponseEntity<Void> quick() {
        workspaceService.quickRequestTenant();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/mass-create")
    public ResponseEntity<Void> massCreateUsers() {
        workspaceService.createMassUsers();
        return ResponseEntity.ok().build();
    }
}
