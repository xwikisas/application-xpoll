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

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.rest.XWikiRestException;
import org.xwiki.rest.internal.resources.pages.ModifiablePageResource;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;

import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.XWikiContext;
import com.xwiki.xpoll.XPollException;
import com.xwiki.xpoll.XPollManager;
import com.xwiki.xpoll.rest.XPollResource;
import com.xwiki.xpoll.rest.model.jaxb.Vote;

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
    private static final String POLL_PUBLICITY_PRIVATE = "private";

    @Inject
    private XPollManager xPollManager;

    @Inject
    private ContextualAuthorizationManager contextualAuthorizationManager;

    @Override
    public Response vote(String wikiName, String spaces, String pageName, Vote vote) throws XWikiRestException
    {
        DocumentReference documentReference = new DocumentReference(pageName, getSpaceReference(spaces, wikiName));
        XWikiContext context = getXWikiContext();
        DocumentReference userReference = context.getUserReference();
        try {
            XWikiDocument doc = context.getWiki().getDocument(documentReference, context).clone();
            BaseObject xpollObj = doc.getXObject(new LocalDocumentReference("XPoll", "XPollClass"));
            String pollPublicity = xpollObj.getStringValue("pollPublicity");
            if (!contextualAuthorizationManager.hasAccess(Right.VIEW, documentReference)
                || (XWikiRightService.isGuest(userReference) && pollPublicity.equals(POLL_PUBLICITY_PRIVATE)))
            {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            if ((vote.getGuestName() == null || vote.getGuestName().isEmpty()) && userReference == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            xPollManager.vote(documentReference, userReference, vote);
            return Response.ok().build();
        } catch (XWikiException e) {
            throw new XWikiRestException(e);
        } catch (XPollException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e).build();
        }
    }
}
