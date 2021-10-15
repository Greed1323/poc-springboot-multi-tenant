package com.example.demo.service;

import com.example.demo.config.context.TenantContext;
import com.example.demo.config.datasource.ProductDataSourceConfiguration;
import com.example.demo.controller.UserDto;
import com.example.demo.controller.UserDto.UserDtoBuilder;
import com.example.demo.dao.common.WorkspaceCommonRepository;
import com.example.demo.dao.common.WorkspaceEntity;
import com.example.demo.dao.tenant.UserRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    @Autowired
    private WorkspaceCommonRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    DataSource primeDataSource;

    @Autowired
    private DataSourceProperties properties;

    @Override
    @Transactional
    public List<WorkspaceEntity> getAllCommonWorkspaces() {
        return StreamSupport.stream(this.workspaceRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public UserDto getAllUsers() {
        UserDtoBuilder userDtoBuilder = UserDto.builder();
        TenantContext.setCurrentTenant("plop");

        System.err.println("tenant 1 " + this.userRepository.findById(1L).get().getName());
        TenantContext.setCurrentTenant("airbasedb");

        System.err.println("tenant 2 " + this.userRepository.findById(1L).get().getName());

        // try {
        // primeDataSource.getConnection().prepareStatement("create schema prout;").execute();
        // primeDataSource.getConnection().prepareStatement("SET search_path TO prout;").execute();
        // primeDataSource.getConnection()
        // .prepareStatement("CREATE TABLE user_entity (" + " id int8 NOT NULL,"
        // + " name varchar NULL," + " CONSTRAINT user_pk PRIMARY KEY (id)"
        // + " );")
        // .execute();
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create(this.getClass().getClassLoader())
                .driverClassName(properties.getDriverClassName())
                .url(properties.getUrl() + "?currentSchema=" + "prout")
                .username(properties.getUsername())
                .password(properties.getPassword());

        if (properties.getType() != null) {
            dataSourceBuilder.type(properties.getType());
        }

        ProductDataSourceConfiguration.getDataSources().put("prout", dataSourceBuilder.build());

        // } catch (SQLException e) {
        // System.err.println(e.getStackTrace());
        // e.printStackTrace();
        // }
        TenantContext.setCurrentTenant("prout");

        System.err.println("tenant prout " + this.userRepository.findById(1L).orElse(null));

        return userDtoBuilder
                .userNames(StreamSupport.stream(this.userRepository.findAll().spliterator(), false)
                        .map(user -> user.getName())
                        .collect(Collectors.toList()))
                .workspaceNames(this.getAllCommonWorkspaces().stream().map(workspace -> workspace.getName()).collect(Collectors.toList()))
                .build();
    }
}
