package com.example.demo.service;

import java.util.List;

import com.example.demo.controller.UserDto;
import com.example.demo.dao.common.WorkspaceEntity;

public interface WorkspaceService {
	public List<WorkspaceEntity> getAllCommonWorkspaces();

	public UserDto getAllUsers();
}
