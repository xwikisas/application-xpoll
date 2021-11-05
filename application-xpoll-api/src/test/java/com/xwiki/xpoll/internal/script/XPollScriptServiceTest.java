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
package com.xwiki.xpoll.internal.script;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xwiki.xpoll.XPollException;
import com.xwiki.xpoll.XPollManager;
import com.xwiki.xpoll.internal.DefaultXPollManager;
import com.xwiki.xpoll.internal.rest.DefaultXPollResource;
import com.xwiki.xpoll.script.XPollScriptService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link com.xwiki.xpoll.script.XPollScriptService}.
 *
 * @version $Id$
 * @since 2.1
 */
@ComponentTest
public class XPollScriptServiceTest
{
    @InjectMockComponents
    private XPollScriptService scriptService;

    @MockComponent
    private XPollManager manager;

    @MockComponent
    private ContextualAuthorizationManager authorizationManager;

    @Test
    void urlTest() {
        scriptService.url(null);
        verify(manager).getRestURL(null);
    }

    @Test
    void getVoteResultsWithAuthorizationTest() throws XPollException
    {
        when(this.authorizationManager.hasAccess(Right.VIEW)).thenReturn(true);
        scriptService.getVoteResults(null);
        verify(this.manager).getVoteResults(null);
    }

    @Test
    void getVoteResultsWithAuthorizationAndThrownExceptionTest() throws XPollException
    {
        when(this.authorizationManager.hasAccess(Right.VIEW)).thenReturn(true);
        when(manager.getVoteResults(null)).thenThrow(XPollException.class);
        Map<String, Integer> result = scriptService.getVoteResults(null);
        assertEquals(0, result.size());
    }
}
