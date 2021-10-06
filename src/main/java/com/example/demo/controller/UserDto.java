package com.example.demo.controller;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
	List<String> userNames;
	List<String> workspaceNames;
}
