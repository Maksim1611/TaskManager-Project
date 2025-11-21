package com.example.TaskManager.tag.repository;

import com.example.TaskManager.tag.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    void deleteAllByProjectId(UUID id);
}
