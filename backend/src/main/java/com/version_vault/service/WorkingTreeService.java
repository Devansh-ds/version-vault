package com.version_vault.service;

import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.exceptions.UnauthorizedException;
import com.version_vault.mapper.WorkingEntryMapper;
import com.version_vault.models.*;
import com.version_vault.repo.BranchRepository;
import com.version_vault.repo.ManifestEntryRepository;
import com.version_vault.repo.WorkingEntryRepository;
import com.version_vault.dtos.response.WorkingEntryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkingTreeService {

    private final WorkingEntryRepository workingEntryRepository;
    private final ObjectService objectService;
    private final BranchRepository branchRepository;
    private final WorkingEntryMapper  workingEntryMapper;
    private final ManifestEntryRepository manifestEntryRepository;

    @Transactional
    public WorkingEntryResponse addOrUpdateFile(UUID branchId, String path, byte[] content, User owner) {

        // validate the path
        validatePath(path);

        // check if the branch exists or not
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id " + branchId));

        // check if the user owns the branch
        if (!branch.getRepository().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not allowed to make changes in this branch");
        }

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
    public void deleteFile(UUID branchId, String path, User owner) {
        validatePath(path);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id " + branchId));

        if (!branch.getRepository().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not allowed to make changes in this branch");
        }

        WorkingEntry entry = workingEntryRepository.findByBranchIdAndPath(branchId, path)
                        .orElseThrow(() -> new ResourceNotFoundException("File not found: " + path));

        workingEntryRepository.delete(entry);
    }

    private void validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }

        if (path.contains("\\")) {
            throw new IllegalArgumentException("Path must use '/' as separator");
        }

        if (path.contains("..")) {
            throw new IllegalArgumentException("Path cannot contain '..'");
        }
    }

    @Transactional
    public void initializeFromManifestOfCommit(UUID branchId, UUID manifestId) {

        // Find the branch
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Branch not found with id " + branchId
                        ));

        // Get all files from the source manifest
        List<ManifestEntry> manifestEntries =
                manifestEntryRepository
                        .findAllByManifestIdOrderByPathAsc(manifestId);

        // Convert ManifestEntry -> WorkingEntry
        List<WorkingEntry> workingEntries = manifestEntries.stream()
                .map(entry -> new WorkingEntry(
                        branch,
                        entry.getPath(),
                        entry.getObject()
                ))
                .toList();

        // Save working tree entries
        workingEntryRepository.saveAll(workingEntries);
    }

    @Transactional
    public void replaceWithManifest(UUID branchId, UUID manifestId) {

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Branch not found with id " + branchId
                        )
                );

        List<ManifestEntry> manifestEntries =
                manifestEntryRepository
                        .findAllByManifestIdOrderByPathAsc(manifestId);

        workingEntryRepository.deleteAllByBranchId(branchId);

        List<WorkingEntry> workingEntries = manifestEntries.stream()
                .map(entry -> new WorkingEntry(
                        branch,
                        entry.getPath(),
                        entry.getObject()
                ))
                .toList();

        workingEntryRepository.saveAll(workingEntries);
    }

    @Transactional(readOnly = true)
    public boolean isClean(UUID branchId, UUID manifestId) {
        List<WorkingEntry> workingEntries =
                workingEntryRepository
                        .findAllByBranchIdOrderByPathAsc(branchId);

        List<ManifestEntry> manifestEntries =
                manifestEntryRepository
                        .findAllByManifestIdOrderByPathAsc(manifestId);

        Map<String, String> workingHashes = workingEntries.stream()
                        .collect(Collectors.toMap(
                                WorkingEntry::getPath,
                                entry -> entry.getObject().getContentHash()
                        ));

        Map<String, String> manifestHashes =
                manifestEntries.stream()
                        .collect(Collectors.toMap(
                                ManifestEntry::getPath,
                                entry -> entry.getObject().getContentHash()
                        ));

        return workingHashes.equals(manifestHashes);
    }
}