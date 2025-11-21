package com.example.TaskManager.tag.service;

import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.tag.model.Tag;
import com.example.TaskManager.tag.repository.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> parseTags(List<String> tags) {
        List<Tag> parsed = new ArrayList<>();

        for (String tag : tags) {
            Tag current = Tag.builder()
                    .title(tag)
                    .build();
            parsed.add(current);
        }

        return parsed;
    }

    @Transactional
    public void saveTags(List<Tag> tags, Project project) {
        tagRepository.deleteAllByProjectId(project.getId());

        for (Tag tag : tags) {
            tag.setProject(project);
            tagRepository.save(tag);
        }
    }
}
