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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rest.XWikiRestException;
import org.xwiki.rest.internal.resources.pages.ModifiablePageResource;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;

import com.xpn.xwiki.XWikiContext;
import com.xwiki.xpoll.XPollException;
import com.xwiki.xpoll.XPollManager;
import com.xwiki.xpoll.model.jaxb.Vote;
import com.xwiki.xpoll.rest.XPollResource;

/**
 * Default implementation of {@link XPollResource}.
 *
 * @version $Id$
 * @since 2.1
 */
@Component
@Named("com.xwiki.xpoll.internal.rest.DefaultXPollResource")
@Singleton
public class DefaultXPollResource extends ModifiablePageResource implements XPollResource
{
    @Inject
    private XPollManager xPollManager;

    @Inject
    private ContextualAuthorizationManager contextualAuthorizationManager;

    @Override
    public Response saveXPollAnswers(String wikiName, String spaces, String pageName, Vote vote)
        throws XWikiRestException
    {
        DocumentReference documentReference = new DocumentReference(pageName, getSpaceReference(spaces, wikiName));

        if (!contextualAuthorizationManager.hasAccess(Right.EDIT, documentReference)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        XWikiContext context = getXWikiContext();
        try {
            DocumentReference userReference = context.getUserReference();
            xPollManager.vote(documentReference, userReference, vote.getVotedProposals());
            return Response.ok().build();
        } catch (XPollException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e).build();
        }
    }
}
