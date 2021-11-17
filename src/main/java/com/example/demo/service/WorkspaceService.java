package com.example.demo.service;

import com.example.demo.controller.UserDto;
import com.example.demo.dao.common.WorkspaceEntity;

import java.util.List;

public interface WorkspaceService {
    public List<WorkspaceEntity> getAllCommonWorkspaces();

    public UserDto getAllUsers();

    public void createTenant();

    public void copyUsers();

    void slowRequestTenant();

    void quickRequestTenant();

    void createMassUsers();
}
