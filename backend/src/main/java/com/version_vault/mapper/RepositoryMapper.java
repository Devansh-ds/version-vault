package com.version_vault.mapper;

import com.version_vault.models.Repository;
import com.version_vault.models.User;
import com.version_vault.dtos.response.RepositoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepositoryMapper {

    public RepositoryResponse toRepositoryResponse(Repository repository, User owner) {
        return RepositoryResponse.builder()
                .id(repository.getId())
                .name(repository.getName())
                .description(repository.getDescription())
                .createdAt(repository.getCreatedAt())
                .updatedAt(repository.getUpdatedAt())
                .ownerId(owner.getId())
                .ownerName(owner.getOriginalUsername())
                .build();
    }

}
