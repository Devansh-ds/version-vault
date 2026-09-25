package com.version_vault.mapper;

import com.version_vault.models.WorkingEntry;
import com.version_vault.response.WorkingEntryResponse;
import org.springframework.stereotype.Service;

@Service
public class WorkingEntryMapper {

    public WorkingEntryResponse toWorkingEntryResponse(WorkingEntry workingEntry) {
        return new WorkingEntryResponse(
                workingEntry.getId(),
                workingEntry.getBranch().getId(),
                workingEntry.getPath(),
                workingEntry.getObject().getId(),
                workingEntry.getObject().getSize(),
                workingEntry.getCreatedAt(),
                workingEntry.getUpdatedAt()
        );
    }

}
