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
package com.xwiki.xpoll.script;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;

import com.xwiki.xpoll.XPollException;
import com.xwiki.xpoll.XPollManager;

/**
 * @version $Id$
 * @since 2.1
 */
@Component
@Named("xpoll")
@Singleton
public class XPollScriptService implements ScriptService
{
    @Inject
    private XPollManager pollManager;

    @Inject
    private ContextualAuthorizationManager contextualAuthorizationManager;

    /**
     * @param documentReference the document that we want to get the URL for
     * @return the REST URL of the XPoll associated with the specific document
     */
    public String url(DocumentReference documentReference)
    {
        return pollManager.getRestURL(documentReference);
    }

    /**
     * @param documentReference the reference of the document that represents the poll (i.e. the document that holds
     *     the poll options and their votes)
     * @return a map that has the XPoll proposals as keys and the scores as values. The map is empty if the access
     *     requirements are not met or if any exception is thrown.
     */
    public Map<String, Integer> getVoteResults(DocumentReference documentReference)
    {
        if (contextualAuthorizationManager.hasAccess(Right.VIEW)) {
            try {
                return pollManager.getVoteResults(documentReference);
            } catch (XPollException ignored) {
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Calculates the winner of the XPoll present in the document and saves it.
     *
     * @param documentReference a document reference
     */
    public void updateWinner(DocumentReference documentReference)
    {
        if (contextualAuthorizationManager.hasAccess(Right.EDIT)) {
            try {
                pollManager.determineWinner(documentReference);
            } catch (XPollException ignored) {

            }
        }
    }
}
