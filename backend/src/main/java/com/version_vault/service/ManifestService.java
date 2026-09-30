package com.version_vault.service;

import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.mapper.ManifestMapper;
import com.version_vault.models.*;
import com.version_vault.repo.BranchRepository;
import com.version_vault.repo.ManifestEntryRepository;
import com.version_vault.repo.ManifestRepository;
import com.version_vault.repo.WorkingEntryRepository;
import com.version_vault.dtos.response.ManifestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManifestService {

    private final ManifestRepository manifestRepository;
    private final ManifestEntryRepository manifestEntryRepository;
    private final WorkingEntryRepository workingEntryRepository;
    private final BranchRepository branchRepository;
    private final ManifestMapper manifestMapper;

    @Transactional
    public ManifestResponse createManifest(UUID branchId) {

        // check if the branch exists or not
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id " + branchId));

        // get all working entry of the branch
        List<WorkingEntry> workingEntries = workingEntryRepository.findAllByBranchIdOrderByPathAsc(branchId);

        // create and save manifest
        Manifest manifest = new Manifest(branch.getRepository());
        Manifest savedManifest = manifestRepository.save(manifest);

        // create List<ManifestEntry> from List<WorkingEntry>
        List<ManifestEntry> manifestEntries = workingEntries.stream()
                .map(we -> new ManifestEntry(
                                savedManifest,
                                we.getPath(),
                                we.getObject()
                        )
                )
                .toList();

        // save the manifest entries
        manifestEntryRepository.saveAll(manifestEntries);

        return manifestMapper.toManifestResponse(savedManifest, manifestEntries);
    }

    @Transactional(readOnly = true)
    public ManifestResponse getManifest(UUID manifestId) {
        Manifest manifest = manifestRepository.findById(manifestId)
                .orElseThrow(() -> new ResourceNotFoundException("Manifest not found with id " + manifestId));

        List<ManifestEntry> entries = manifestEntryRepository.findAllByManifestIdOrderByPathAsc(manifestId);

        return manifestMapper.toManifestResponse(manifest, entries);
    }

    @Transactional
    public Manifest createManifestEntity(UUID branchId) {

        // check if the branch exists or not
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id " + branchId));

        // get all working entry of the branch
        List<WorkingEntry> workingEntries = workingEntryRepository.findAllByBranchIdOrderByPathAsc(branchId);

        // create and save manifest
        Manifest manifest = new Manifest(branch.getRepository());
        Manifest savedManifest = manifestRepository.save(manifest);

        // create List<ManifestEntry> from List<WorkingEntry>
        List<ManifestEntry> manifestEntries = workingEntries.stream()
                .map(we -> new ManifestEntry(
                                savedManifest,
                                we.getPath(),
                                we.getObject()
                        )
                )
                .toList();

        // save the manifest entries
        manifestEntryRepository.saveAll(manifestEntries);

        return savedManifest;
    }

    @Transactional
    public Manifest createManifestFromFiles(Repository repository, Map<String, ObjectEntity> files) {
        Manifest manifest = new Manifest(repository);
        Manifest savedManifest = manifestRepository.save(manifest);

        List<ManifestEntry> entries = files.entrySet().stream()
                .map(entry -> new ManifestEntry(
                        savedManifest,
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();

        manifestEntryRepository.saveAll(entries);
        return savedManifest;
    }

}