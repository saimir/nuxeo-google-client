/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *      Nelson Silva
 */
package org.nuxeo.ecm.googleclient;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

import java.io.Serializable;

/**
 * Descriptor for Google Drive configuration.
 *
 * @since 7.3
 */
@XObject("configuration")
public class GoogleDriveConfigurationDescriptor implements Serializable {
    private static final long serialVersionUID = 1L;

    @XNode("emailAddress")
    private String emailAddress;

    @XNode("clientId")
    private String clientId;

    @XNode("clientSecret")
    private String clientSecret;

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
