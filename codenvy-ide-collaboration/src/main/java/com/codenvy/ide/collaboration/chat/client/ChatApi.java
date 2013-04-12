/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package com.codenvy.ide.collaboration.chat.client;

import com.codenvy.ide.collaboration.dto.ChatMessage;
import com.codenvy.ide.collaboration.dto.GetChatParticipants;
import com.codenvy.ide.collaboration.dto.GetChatParticipantsResponse;

import org.exoplatform.ide.client.framework.websocket.FrontendApi;
import org.exoplatform.ide.client.framework.websocket.MessageBus;

/**
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class ChatApi extends FrontendApi {

    public SendApi<ChatMessage> SEND_MESSAGE = makeApi("ide/collaboration/chat/send/message");

    public RequestResponseApi<GetChatParticipants, GetChatParticipantsResponse> GET_CHAT_PARTISIPANTS =
            makeApi("ide/collaboration/chat/participants");

    public ChatApi(MessageBus messageBus) {
        super(messageBus);
    }
}
