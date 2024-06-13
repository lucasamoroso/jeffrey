package pbouda.jeffrey.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.openjdk.jmc.flightrecorder.CouldNotLoadRecordingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pbouda.jeffrey.controller.model.DeleteRecordingRequest;
import pbouda.jeffrey.manager.ProfilesManager;
import pbouda.jeffrey.manager.RecordingManager;
import pbouda.jeffrey.repository.model.AvailableRecording;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/recordings")
public class RecordingController {

    private static final Logger LOG = LoggerFactory.getLogger(RecordingController.class);

    private final RecordingManager recordingManager;
    private final ProfilesManager profilesManager;

    public RecordingController(
            RecordingManager recordingManager,
            ProfilesManager profilesManager) {

        this.recordingManager = recordingManager;
        this.profilesManager = profilesManager;
    }

    @GetMapping
    public List<AvailableRecording> recordings() {
        return recordingManager.all().stream()
                .sorted(Comparator.comparing((AvailableRecording p) -> p.file().dateTime()).reversed())
                .toList();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("files[]") MultipartFile[] files) throws IOException {
        for (MultipartFile file : files) {
            try {
                recordingManager.upload(file.getOriginalFilename(), file.getInputStream());
            } catch (Exception e) {
                LOG.error("Couldn't load recording: {}", file.getOriginalFilename(), e);
                return ResponseEntity.badRequest()
                        .body("Invalid JFR file: " + file.getOriginalFilename());
            }
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/uploadAndInit")
    public ResponseEntity<String> uploadAndInit(@RequestParam("files[]") MultipartFile[] files) throws IOException {
        for (MultipartFile file : files) {
            try {
                recordingManager.upload(file.getOriginalFilename(), file.getInputStream());
            } catch (Exception e) {
                LOG.error("Couldn't load recording: {}", file.getOriginalFilename(), e);
                return ResponseEntity.badRequest()
                        .body("Invalid JFR file: " + file.getOriginalFilename());
            }

            profilesManager.createProfile(file.getOriginalFilename());
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/delete")
    public void deleteRecording(@RequestBody DeleteRecordingRequest request) {
        for (String filename : request.filenames()) {
            recordingManager.delete(filename);
        }
    }
}
