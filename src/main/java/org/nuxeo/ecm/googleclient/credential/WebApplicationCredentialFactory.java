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
 *
 *      Nelson Silva
 */

package org.nuxeo.ecm.googleclient.credential;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.oauth2.providers.NuxeoOAuth2ServiceProvider;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProviderRegistry;
import org.nuxeo.ecm.platform.ui.web.util.BaseURL;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;
import java.util.Arrays;

/**
 * Credential factory for Web Applications.
 *
 * @since 7.3
 */
public class WebApplicationCredentialFactory implements CredentialFactory {

    private static final Log log = LogFactory.getLog(WebApplicationCredentialFactory.class);

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final String providerId;

    private final String clientId;

    private final String clientSecret;

    public WebApplicationCredentialFactory(String providerId, String clientId, String clientSecret) {
        this.providerId = providerId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        initOAuth2ServiceProvider();
    }

    @Override
    public Credential build(String user) {
        Credential credential = null;
        // Use system wide OAuth2 provider
        if (getOAuth2ServiceProvider() != null) {
            AuthorizationCodeFlow flow = getOAuth2ServiceProvider().getAuthorizationCodeFlow(HTTP_TRANSPORT, JSON_FACTORY);
            try {
                credential = flow.loadCredential(user);
            } catch (IOException e) {
                throw new ClientException(e.getMessage());
            }
        }
        return credential;
    }

    protected NuxeoOAuth2ServiceProvider getOAuth2ServiceProvider() {
        return Framework.getLocalService(OAuth2ServiceProviderRegistry.class).getProvider(providerId);
    }

    protected void initOAuth2ServiceProvider() throws ClientException {

        // Register the system wide OAuth2 provider
        OAuth2ServiceProviderRegistry registry = Framework.getLocalService(OAuth2ServiceProviderRegistry.class);

        if (registry == null) {
            throw new ClientException("Failed to retrieve OAuth2 provider registry");
        }

        NuxeoOAuth2ServiceProvider oauth2Provider = getOAuth2ServiceProvider();

        if (oauth2Provider == null) {
            try {
                oauth2Provider = registry.addProvider(
                    providerId,
                    GoogleOAuthConstants.TOKEN_SERVER_URL,
                    GoogleOAuthConstants.AUTHORIZATION_SERVER_URL,
                    clientId, clientSecret,
                    Arrays.asList("https://www.googleapis.com/auth/drive.readonly"));
            } catch (Exception e) {
                throw new ClientException(e.getMessage());
            }
            // TODO: remove this once this is made available in the admin UI
            log.warn("Please go to " + getAuthorizationURL(oauth2Provider, "http://localhost:8080") + " to start the authorization flow");
        } else {
            log.warn("Provider "
                + providerId
                + " is already in the Database, XML contribution  won't overwrite it");
        }
    }

    private static String getAuthorizationURL(NuxeoOAuth2ServiceProvider provider, String serverURL) {
        AuthorizationCodeFlow flow = provider.getAuthorizationCodeFlow(HTTP_TRANSPORT, JSON_FACTORY);

        String redirectUrl = serverURL + BaseURL.getContextPath() + "/site/oauth2/" + provider.getServiceName() + "/callback";

        // redirect to the authorization flow
        AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl();
        authorizationUrl.setRedirectUri(redirectUrl);

        // request offline access and force consent screen
        return authorizationUrl.build() + "&access_type=offline&approval_prompt=force";
    }
}
