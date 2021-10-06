package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.example.demo.controller.UserDto;
import com.example.demo.controller.UserDto.UserDtoBuilder;
import com.example.demo.dao.common.WorkspaceCommonRepository;
import com.example.demo.dao.common.WorkspaceEntity;
import com.example.demo.dao.tenant.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {
	@Autowired
	private WorkspaceCommonRepository workspaceRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional
	public List<WorkspaceEntity> getAllCommonWorkspaces() {
		return StreamSupport.stream(this.workspaceRepository.findAll().spliterator(), false).collect(Collectors.toList());
	}

	@Override
	public UserDto getAllUsers() {
		UserDtoBuilder userDtoBuilder = UserDto.builder();

		return userDtoBuilder
				.userNames(StreamSupport.stream(this.userRepository.findAll().spliterator(), false).map(user -> user.getName())
						.collect(Collectors.toList()))
				.workspaceNames(
						this.getAllCommonWorkspaces().stream().map(workspace -> workspace.getName()).collect(Collectors.toList()))
				.build();
	}
}
