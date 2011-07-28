/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *      Nuxeo
 */

package org.nuxeo.ecm.social.workspace.service;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.social.workspace.adapters.SocialWorkspace;

/**
 * Service interface for Social Workspace.
 */
public interface SocialWorkspaceService {

    void initializeSocialWorkspace(SocialWorkspace socialWorkspace,
            String principalName);

    /**
     * Returns the {@code SocialWorkspace} container of the given document if it
     * is in a Social Workspace even if the user does not have right on it,
     * {@code null} otherwise.
     * <p>
     * The underlying {@code DocumentModel} is detached.
     */
    SocialWorkspace getDetachedSocialWorkspaceContainer(DocumentModel doc);

    /**
     * Returns the {@code SocialWorkspace} container of the given document if it
     * is part of a Social Workspace, {@code null} otherwise.
     */
    SocialWorkspace getSocialWorkspaceContainer(DocumentModel doc);

    /**
     * Makes the given {@code socialWorkspace} public.
     * <p>
     * Puts the correct rights so that non-members can view public documents and
     * publci dashboard.
     */
    void makeSocialWorkspacePublic(SocialWorkspace socialWorkspace);

    /**
     * Makes the given {@code socialWorkspace} private.
     * <p>
     * Restricts rights for non-members users.
     */
    void makeSocialWorkspacePrivate(SocialWorkspace socialWorkspace);

    /**
     * Gets the number of days before a social workspace expires without
     * validation.
     *
     * @return number of days
     */
    int getValidationDays();

}
