/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
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
package com.xwiki.xpoll.internal.rest;

import java.util.Collections;

import javax.inject.Provider;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.rest.XWikiRestException;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.XWikiRequest;
import com.xwiki.xpoll.XPollException;
import com.xwiki.xpoll.XPollManager;
import com.xwiki.xpoll.rest.model.jaxb.Vote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultXPollResource}.
 *
 * @version $Id$
 * @since 2.1
 */
@ComponentTest
public class DefaultXPollResourceTest
{
    private static final DocumentReference userDocumentReference = new DocumentReference("xwiki", "XWiki", "User");

    @InjectMockComponents
    private DefaultXPollResource resource;

    @MockComponent
    private XPollManager xPollManager;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    private EntityReferenceSerializer<String> serializer;

    @MockComponent
    private ContextualAuthorizationManager contextualAuthorizationManager;

    @MockComponent
    protected Provider<XWikiContext> xcontextProvider;

    private XWikiContext xWikiContext;

    @BeforeComponent
    public void configure() throws Exception
    {
        ComponentManager contextComponentManager =
            this.componentManager.registerMockComponent(ComponentManager.class, "context");
        Execution execution = mock(Execution.class);
        when(contextComponentManager.getInstance(Execution.class)).thenReturn(execution);
        ExecutionContext executionContext = new ExecutionContext();
        this.xWikiContext = mock(XWikiContext.class);
        executionContext.setProperty("xwikicontext", this.xWikiContext);
        when(execution.getContext()).thenReturn(executionContext);
        when(this.xcontextProvider.get()).thenReturn(this.xWikiContext);
    }

    @Test
    void saveXPollAnswersWithEditRightTest() throws XWikiRestException
    {
        setMockUserRights(true, true, userDocumentReference);
        Response response = this.resource.vote("wiki", "space", "page", new Vote());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void saveXPollAnswersWithoutEditRightTest() throws XWikiRestException
    {
        setMockUserRights(true, false, userDocumentReference);
        Response response = this.resource.vote("wiki", "space", "page", new Vote());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void saveXPollAnswersWithoutViewRightTest() throws XWikiRestException
    {
        setMockUserRights(false, false, userDocumentReference);
        Response response = this.resource.vote("wiki", "space", "page", new Vote());
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    void saveXPollAnswersLoggedOutTest() throws XWikiRestException
    {
        setMockUserRights(true, false, null);
        Response response = this.resource.vote("wiki", "space", "page", new Vote());
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    void saveXPollButManagerThrowsException() throws XPollException, XWikiRestException
    {
        DocumentReference docRef = new DocumentReference("xwiki", "Main", "WebHome");
        setMockUserRights(true, false, userDocumentReference);
        when(this.serializer.serialize(null, new WikiReference("wiki"))).thenReturn("userIdentifier");

        Vote vote = new Vote();

        doThrow(new XPollException("Message")).when(this.xPollManager)
            .vote(docRef, userDocumentReference, Collections.emptyList());
        Response response = resource.vote("xwiki", "Main", "WebHome", vote);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    private void setMockUserRights(boolean viewRight, boolean editRight, DocumentReference userReference)
    {
        when(this.contextualAuthorizationManager.hasAccess(eq(Right.VIEW), any(DocumentReference.class))).thenReturn(viewRight);
        when(this.contextualAuthorizationManager.hasAccess(eq(Right.EDIT), any(DocumentReference.class))).thenReturn(editRight);
        when(this.xWikiContext.getUserReference()).thenReturn(userReference);
    }
}
