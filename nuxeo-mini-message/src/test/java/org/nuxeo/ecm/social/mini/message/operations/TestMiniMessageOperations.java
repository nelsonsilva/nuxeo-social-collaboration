/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.social.mini.message.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.social.mini.message.AbstractMiniMessageTest;
import org.nuxeo.ecm.social.mini.message.MiniMessage;
import org.nuxeo.runtime.test.runner.Deploy;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Deploy({ "org.nuxeo.ecm.automation.core",
        "org.nuxeo.ecm.platform.query.api:OSGI-INF/pageprovider-framework.xml" })
public class TestMiniMessageOperations extends AbstractMiniMessageTest {

    @Inject
    protected AutomationService automationService;

    @Test
    public void shouldAddAMiniMessage() throws Exception {
        CoreSession newSession = openSessionAs("Leela");
        OperationContext ctx = new OperationContext(newSession);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testMiniMessageOperation");
        chain.add(AddMiniMessage.ID).set("message",
                "At the risk of sounding negative, no.");
        Blob result = (Blob) automationService.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        List<MiniMessage> messages = miniMessageService.getMiniMessageFor(
                ActivityHelper.createUserActivityObject("Leela"),
                CIRCLE_RELATION, 0, 0);
        assertNotNull(messages);
        assertEquals(1, messages.size());

        CoreInstance.getInstance().close(newSession);
    }

    @Test
    public void shouldGetNoMiniMessage() throws Exception {
        CoreSession newSession = openSessionAs("Leela");
        OperationContext ctx = new OperationContext(newSession);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testMiniMessageOperation");
        chain.add(GetMiniMessages.ID);
        Blob result = (Blob) automationService.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> m = mapper.readValue(json,
                new TypeReference<Map<String, Object>>() {
                });
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> miniMessages = (List<Map<String, Object>>) m.get("miniMessages");
        assertTrue(miniMessages.isEmpty());

        CoreInstance.getInstance().close(newSession);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetPaginatedMiniMessages() throws Exception {
        initializeSomeMiniMessagesAndRelations();
        CoreSession newSession = openSessionAs("Leela");

        OperationContext ctx = new OperationContext(newSession);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testMiniMessageOperation");
        chain.add(GetMiniMessages.ID);
        Blob result = (Blob) automationService.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> m = mapper.readValue(json,
                new TypeReference<Map<String, Object>>() {
                });
        List<Map<String, Object>> miniMessages = (List<Map<String, Object>>) m.get("miniMessages");
        assertEquals(5, miniMessages.size());

        chain = new OperationChain("testMiniMessageOperation");
        chain.add(GetMiniMessages.ID).set("offset", 5);
        result = (Blob) automationService.run(ctx, chain);
        assertNotNull(result);
        json = result.getString();
        assertNotNull(json);

        mapper = new ObjectMapper();
        m = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
        miniMessages = (List<Map<String, Object>>) m.get("miniMessages");
        assertEquals(5, miniMessages.size());

        chain = new OperationChain("testMiniMessageOperation");
        chain.add(GetMiniMessages.ID).set("offset", 10);
        result = (Blob) automationService.run(ctx, chain);
        assertNotNull(result);
        json = result.getString();
        assertNotNull(json);

        mapper = new ObjectMapper();
        m = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
        miniMessages = (List<Map<String, Object>>) m.get("miniMessages");
        assertEquals(0, miniMessages.size());

        CoreInstance.getInstance().close(newSession);
    }

    @Test
    public void shouldRemoveAMiniMessage() throws Exception {
        initializeSomeMiniMessagesAndRelations();
        CoreSession newSession = openSessionAs("Bender");

        String benderActivityObject = ActivityHelper.createUserActivityObject("Bender");
        List<MiniMessage> messages = miniMessageService.getMiniMessageFrom(
                benderActivityObject, 0, 0);
        assertEquals(5, messages.size());
        MiniMessage miniMessage = messages.get(0);

        OperationContext ctx = new OperationContext(newSession);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testMiniMessageOperation");
        chain.add(RemoveMiniMessage.ID).set("miniMessageId", String.valueOf(miniMessage.getId()));
        automationService.run(ctx, chain);

        messages = miniMessageService.getMiniMessageFrom(
                benderActivityObject, 0, 0);
        assertEquals(4, messages.size());

        miniMessage = messages.get(0);
        chain = new OperationChain("testMiniMessageOperation");
        chain.add(RemoveMiniMessage.ID).set("miniMessageId", String.valueOf(miniMessage.getId()));
        automationService.run(ctx, chain);

        messages = miniMessageService.getMiniMessageFrom(
                benderActivityObject, 0, 0);
        assertEquals(3, messages.size());

        CoreInstance.getInstance().close(newSession);
    }

    @Test
    public void shouldNotBeAbleToRemoveOtherUserMiniMessage() throws Exception {
        initializeSomeMiniMessagesAndRelations();
        CoreSession newSession = openSessionAs("Leela");

        String benderActivityObject = ActivityHelper.createUserActivityObject("Bender");
        List<MiniMessage> messages = miniMessageService.getMiniMessageFrom(
                benderActivityObject, 0, 0);
        assertEquals(5, messages.size());
        MiniMessage miniMessage = messages.get(0);

        OperationContext ctx = new OperationContext(newSession);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testMiniMessageOperation");
        chain.add(RemoveMiniMessage.ID).set("miniMessageId", String.valueOf(miniMessage.getId()));
        automationService.run(ctx, chain);

        messages = miniMessageService.getMiniMessageFrom(
                benderActivityObject, 0, 0);
        assertEquals(5, messages.size());

        CoreInstance.getInstance().close(newSession);
    }

}
