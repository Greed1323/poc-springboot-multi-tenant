package com.example.demo.service;

import com.example.demo.config.context.TenantContext;
import com.example.demo.config.datasource.ProductDataSourceConfiguration;
import com.example.demo.controller.UserDto;
import com.example.demo.controller.UserDto.UserDtoBuilder;
import com.example.demo.dao.common.WorkspaceCommonRepository;
import com.example.demo.dao.common.WorkspaceEntity;
import com.example.demo.dao.tenant.UserEntity;
import com.example.demo.dao.tenant.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

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
        TenantContext.setCurrentTenant("tenant1");

        System.err.println("tenant 1 " + this.userRepository.findById(1L).get().getName());
        TenantContext.setCurrentTenant("tenant2");

        System.err.println("tenant 2 " + this.userRepository.findById(1L).get().getName());

        return userDtoBuilder
                .userNames(StreamSupport.stream(this.userRepository.findAll().spliterator(), false)
                        .map(user -> user.getName())
                        .collect(Collectors.toList()))
                .workspaceNames(this.getAllCommonWorkspaces().stream().map(workspace -> workspace.getName()).collect(Collectors.toList()))
                .build();
    }

    @Override
    public void createTenant() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create(this.getClass().getClassLoader())
                .driverClassName(properties.getDriverClassName())
                .url(properties.getUrl() + "?currentSchema=" + "tenant3")
                .username(properties.getUsername())
                .password(properties.getPassword());

        if (properties.getType() != null) {
            dataSourceBuilder.type(properties.getType());
        }

        ProductDataSourceConfiguration.getDataSources().put("tenant3", dataSourceBuilder.build());

        TenantContext.setCurrentTenant("tenant3");

        System.err.println("tenant 3 " + this.userRepository.findById(1L).orElse(null));

    }

    @Override
    public void copyUsers() {
        StopWatch watch = new StopWatch();
        watch.start();
        List<UserEntity> users = this.userRepository.findAll();
        users.get(0).setName("Avant changement de tenant");
        userRepository.save(users.get(0));
        System.err.println("tenant 1 " + users.size());

        TenantContext.setCurrentTenant("tenant2");
        users.get(0).setName("Après changement de tenant");
        userRepository.saveAll(users);
        int userNb = userRepository.findAll().size();
        System.err.println("tenant 2 " + userNb);
        watch.stop();
        System.err.println("Time for copy of " + userNb + " simple elements " + watch.getTotalTimeSeconds() + " seconds");
    }

    @Override
    public void slowRequestTenant() {
        System.err.println("debut slow tenant 1");
        try {
            Thread.sleep(4000L);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Optional<UserEntity> user = this.userRepository.findById(4L);
        UserEntity toReturn = user.orElse(null);
        toReturn.setName("Tenant11111111concurent");
        userRepository.save(toReturn);
        System.err.println("fin slow tenant 1");

    }

    @Override
    public void quickRequestTenant() {
        System.err.println("début quick tenant 2");
        Optional<UserEntity> user = this.userRepository.findById(4L);
        UserEntity newUser = new UserEntity();
        newUser.setId(4L);
        UserEntity toReturn = user.orElse(newUser);
        toReturn.setName("Tenant222222concurent");
        userRepository.save(toReturn);
        System.err.println("fin quick tenant 2");

    }

    @Override
    public void createMassUsers() {
        List<UserEntity> userList = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            UserEntity newUser = new UserEntity();
            newUser.setName("testName");
            userList.add(newUser);
        }
        userRepository.saveAll(userList);
    }
}
