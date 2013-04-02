/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ide.extension.samples.client.github.load;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: ProjectData.java Nov 18, 2011 3:27:38 PM vereshchaka $
 */
public class ProjectData {

    private String name;

    private String description;

    private String type;

    /** Url to clone from GitHub. */
    private String repositoryUrl;

    /** Url to clone from GitHub (readOnly). */
    private String readOnlyUrl;

    private List<String> targets;

    public ProjectData(String name, String description, String type, List<String> targets, String repositoryUrl, String readOnlyUrl) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.repositoryUrl = repositoryUrl;
        this.targets = targets;
        this.readOnlyUrl = readOnlyUrl;
    }

    /**
     * Get the url to clone from GitHub.
     *
     * @return the repositoryUrl
     */
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    /**
     * @param repositoryUrl
     *         the repositoryUrl to set
     */
    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    /** @return the name */
    public String getName() {
        return name;
    }

    /** @return the description */
    public String getDescription() {
        return description;
    }

    /** @return the type */
    public String getType() {
        return type;
    }

    /**
     * @param name
     *         the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    /** @return the targets */
    public List<String> getTargets() {
        if (targets == null) {
            targets = new ArrayList<String>();
        }
        return targets;
    }

    /**
     * @param targets
     *         the targets to set
     */
    public void setTargets(List<String> targets) {
        this.targets = targets;
    }

    /**
     * @param description
     *         the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getReadOnlyUrl() {
        return readOnlyUrl;
    }

    public void setReadOnlyUrl(String readOnlyUrl) {
        this.readOnlyUrl = readOnlyUrl;
    }
}
