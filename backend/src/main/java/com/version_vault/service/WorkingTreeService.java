package com.version_vault.service;

import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.mapper.WorkingEntryMapper;
import com.version_vault.models.Branch;
import com.version_vault.models.ObjectEntity;
import com.version_vault.models.WorkingEntry;
import com.version_vault.repo.BranchRepository;
import com.version_vault.repo.WorkingEntryRepository;
import com.version_vault.response.WorkingEntryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkingTreeService {

    private final WorkingEntryRepository workingEntryRepository;
    private final ObjectService objectService;
    private final BranchRepository branchRepository;
    private final WorkingEntryMapper  workingEntryMapper;

    @Transactional
    public WorkingEntryResponse addOrUpdateFile(UUID branchId, String path, byte[] content) {

        // validate path
        validatePath(path);

        // check if branch exist or not
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id " + branchId));

        // store content and get Object
        ObjectEntity objectEntity = objectService.store(content);

        // check if working entry exist (if exist means there's content update else new file)
        Optional<WorkingEntry> existing = workingEntryRepository.findByBranchIdAndPath(branchId, path);

        if (existing.isPresent()) {
            WorkingEntry workingEntry = existing.get();
            workingEntry.setObject(objectEntity);

            WorkingEntry saved = workingEntryRepository.save(workingEntry);
            return workingEntryMapper.toWorkingEntryResponse(saved);
        }

        WorkingEntry newWorkingEntry = new WorkingEntry(branch, path, objectEntity);

        // save the new entry
        WorkingEntry saved = workingEntryRepository.save(newWorkingEntry);
        return workingEntryMapper.toWorkingEntryResponse(saved);
    }

    @Transactional(readOnly = true)
    public byte[] readFile(UUID branchId, String path) {

        validatePath(path);

        WorkingEntry entry = workingEntryRepository.findByBranchIdAndPath(branchId, path)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id " + branchId + " and path " + path));

        return objectService
                .readContent(entry.getObject().getId());
    }

    @Transactional(readOnly = true)
    public List<WorkingEntryResponse> listFiles(UUID branchId) {

        branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id " + branchId));

        return workingEntryRepository.findAllByBranchIdOrderByPathAsc(branchId)
                .stream()
                .map(workingEntryMapper::toWorkingEntryResponse)
                .toList();
    }

    @Transactional
    public void deleteFile(UUID branchId, String path) {
        validatePath(path);

        WorkingEntry entry = workingEntryRepository.findByBranchIdAndPath(branchId, path)
                        .orElseThrow(() -> new ResourceNotFoundException("File not found: " + path));

        workingEntryRepository.delete(entry);
    }

    private void validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }

//        if (path.startsWith("/")) {
//            throw new IllegalArgumentException("Path cannot start with '/'");
//        }

        if (path.contains("\\")) {
            throw new IllegalArgumentException("Path must use '/' as separator");
        }

        if (path.contains("..")) {
            throw new IllegalArgumentException("Path cannot contain '..'");
        }
    }
}
