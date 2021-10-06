package com.example.demo.controller;

import java.util.List;

import com.example.demo.dao.common.WorkspaceEntity;
import com.example.demo.service.WorkspaceService;

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
}
