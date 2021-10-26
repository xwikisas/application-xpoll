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
package com.xwiki.xpoll.internal;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.xpoll.XPollException;
import com.xwiki.xpoll.XPollManager;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UpdatePollWinnersListener}.
 *
 * @version $Id$
 * @since 2.1
 */
@ComponentTest
public class UpdatePollWinnersListenerTest
{
    @InjectMockComponents
    private UpdatePollWinnersListener listener;

    @Mock
    private XWikiContext xWikiContext;

    @Mock
    private XWiki wiki;

    @Mock
    private XWikiDocument document;

    @Mock
    private BaseObject xpollObject;

    @MockComponent
    private XPollManager pollManager;

    @Test
    void onEvent() throws XPollException, XWikiException
    {
        when(this.document.getXObject(DefaultXPollManager.XPOLL_CLASS_REFERENCE)).thenReturn(this.xpollObject);

        when(this.xWikiContext.get("updatingWinner")).thenReturn(null);
        when(this.xpollObject.getStringValue("status")).thenReturn("active");

        DocumentReference docRef = new DocumentReference("wiki", "XPoll", "Test");

        when(this.document.getDocumentReference()).thenReturn(docRef);
        Map<String, Integer> results = new HashMap<>();
        results.put("Proposal1", 3);
        results.put("Proposal2", 1);
        results.put("Proposal3", 2);
        when(this.pollManager.getVoteResults(docRef)).thenReturn(results);
        when(this.xpollObject.getStringValue("winner")).thenReturn("");
        when(this.xWikiContext.getWiki()).thenReturn(this.wiki);

        this.listener.onEvent(new DocumentUpdatedEvent(), this.document, this.xWikiContext);


        verify(this.xpollObject).set("winner", "Proposal1", xWikiContext);
        verify(this.wiki).saveDocument(this.document, "Updated winner", this.xWikiContext);
    }
}
