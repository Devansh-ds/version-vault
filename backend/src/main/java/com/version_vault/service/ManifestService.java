package com.version_vault.service;

import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.mapper.ManifestMapper;
import com.version_vault.models.Branch;
import com.version_vault.models.Manifest;
import com.version_vault.models.ManifestEntry;
import com.version_vault.models.WorkingEntry;
import com.version_vault.repo.BranchRepository;
import com.version_vault.repo.ManifestEntryRepository;
import com.version_vault.repo.ManifestRepository;
import com.version_vault.repo.WorkingEntryRepository;
import com.version_vault.response.ManifestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        // check if branch exist or not
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

        // check if branch exist or not
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

}