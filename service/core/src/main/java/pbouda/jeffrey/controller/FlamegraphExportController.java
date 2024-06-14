/*
 * Jeffrey
 * Copyright (C) 2024 Petr Bouda
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pbouda.jeffrey.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbouda.jeffrey.controller.model.ExportRequest;
import pbouda.jeffrey.exception.Exceptions;
import pbouda.jeffrey.manager.GraphManager;
import pbouda.jeffrey.manager.ProfileManager;
import pbouda.jeffrey.manager.ProfilesManager;

@RestController
@RequestMapping("/flamegraph/export")
public class FlamegraphExportController {

    private static final Logger LOG = LoggerFactory.getLogger(FlamegraphExportController.class);

    private final ProfilesManager profilesManager;

    @Autowired
    public FlamegraphExportController(ProfilesManager profilesManager) {
        this.profilesManager = profilesManager;
    }

    @PostMapping("/id")
    public void exportBytId(@RequestBody ExportRequest request) {
        GraphManager graphManager = profilesManager.getProfile(request.primaryProfileId())
                .map(ProfileManager::flamegraphManager)
                .orElseThrow(Exceptions.PROFILE_NOT_FOUND);

        graphManager.export(request.flamegraphId());
        LOG.info("Flamegraph successfully exported: {}", request);
    }

    @PostMapping
    public void export(@RequestBody ExportRequest request) {
        GraphManager graphManager = profilesManager.getProfile(request.primaryProfileId()).
                map(ProfileManager::flamegraphManager)
                .orElseThrow(Exceptions.PROFILE_NOT_FOUND);

        graphManager.export(request.eventType(), request.timeRange(), request.threadMode());
    }

    @PostMapping("/range")
    public void exportDiff(@RequestBody ExportRequest request) {
        ProfileManager primaryManager = profilesManager.getProfile(request.primaryProfileId())
                .orElseThrow(Exceptions.PROFILE_NOT_FOUND);
        ProfileManager secondaryManager = profilesManager.getProfile(request.secondaryProfileId())
                .orElseThrow(Exceptions.PROFILE_NOT_FOUND);

        primaryManager.diffgraphManager(secondaryManager)
                .export(request.eventType(), request.timeRange(), false);
    }
}
