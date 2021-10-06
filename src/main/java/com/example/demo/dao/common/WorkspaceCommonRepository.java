package com.example.demo.dao.common;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceCommonRepository extends CrudRepository<WorkspaceEntity, Long> {

}
