package pbouda.jeffrey.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import pbouda.jeffrey.WorkingDirs;
import pbouda.jeffrey.controller.model.CreateProfileRequest;
import pbouda.jeffrey.controller.model.DeleteRecordingRequest;
import pbouda.jeffrey.controller.model.DeleteProfileRequest;
import pbouda.jeffrey.manager.ProfileManager;
import pbouda.jeffrey.manager.ProfilesManager;
import pbouda.jeffrey.repository.AvailableRecording;
import pbouda.jeffrey.repository.ProfileInfo;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileController.class);

    private final WorkingDirs workingDirs;
    private final ProfilesManager profilesManager;

    public ProfileController(WorkingDirs workingDirs, ProfilesManager profilesManager) {
        this.workingDirs = workingDirs;
        this.profilesManager = profilesManager;
    }

    @GetMapping
    public List<ProfileInfo> profiles() {
        return profilesManager.allProfiles().stream()
                .map(ProfileManager::info)
                .sorted(Comparator.comparing(ProfileInfo::createdAt).reversed())
                .toList();
    }

    @GetMapping("/recording")
    public List<AvailableRecording> recordings() {
        return profilesManager.allRecordings().stream()
                .sorted(Comparator.comparing((AvailableRecording p) -> p.file().dateTime()).reversed())
                .toList();
    }

    @PostMapping
    public ProfileInfo createProfile(@RequestBody CreateProfileRequest request) {
        Path resolve = workingDirs.recordingsDir().resolve(request.jfrName());
        ProfileManager profileManager = profilesManager.createProfile(resolve);

        ProfileInfo info = profileManager.info();
        LOG.info("New profile created: id={} path={}", info.id(), info.recordingPath());
        return info;
    }

    @PostMapping("/delete")
    public void deleteProfile(@RequestBody DeleteProfileRequest request) {
        for (String profileId : request.profileIds()) {
            profilesManager.deleteProfile(profileId);
        }
    }

    @PostMapping("/recording/delete")
    public void deleteRecording(@RequestBody DeleteRecordingRequest request) {
        for (String profileId : request.filenames()) {
            profilesManager.deleteRecording(workingDirs.recordingsDir().resolve(profileId));
        }
    }
}
