package com.example.demo.dao.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceCommonRepository extends JpaRepository<WorkspaceEntity, Long> {

}
