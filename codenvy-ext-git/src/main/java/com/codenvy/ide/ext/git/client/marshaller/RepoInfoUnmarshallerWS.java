/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide.ext.git.client.marshaller;

import com.codenvy.ide.commons.exception.UnmarshallerException;
import com.codenvy.ide.ext.git.dto.client.DtoClientImpls;
import com.codenvy.ide.ext.git.shared.RepoInfo;
import com.codenvy.ide.websocket.Message;
import com.codenvy.ide.websocket.rest.Unmarshallable;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * @author <a href="mailto:azatsarynnyy@exoplatfrom.com">Artem Zatsarynnyy</a>
 * @version $Id: RepoInfoUnmarshallerWS.java Nov 21, 2012 3:02:52 PM azatsarynnyy $
 */
public class RepoInfoUnmarshallerWS implements Unmarshallable<RepoInfo> {
    private final DtoClientImpls.RepoInfoImpl repoInfo;

    public RepoInfoUnmarshallerWS(DtoClientImpls.RepoInfoImpl repoInfo) {
        this.repoInfo = repoInfo;
    }

    /** {@inheritDoc} */
    @Override
    public void unmarshal(Message response) throws UnmarshallerException {
        JSONObject jsonObject = JSONParser.parseLenient(response.getBody()).isObject();
        String value = jsonObject.toString();
        DtoClientImpls.RepoInfoImpl repoInfo = DtoClientImpls.RepoInfoImpl.deserialize(value);
        this.repoInfo.setRemoteUri(repoInfo.getRemoteUri());
    }

    /** {@inheritDoc} */
    @Override
    public RepoInfo getPayload() {
        return repoInfo;
    }
}