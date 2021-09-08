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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.rest.XWikiResource;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.XWikiRequest;
import com.xwiki.xpoll.XPollManager;
import com.xwiki.xpoll.rest.XPollResource;
/**
 * Default implementation of {@link XPollResource}.
 *
 * @version $Id$
 * @since 2.0.5
 */
@Component
@Named("com.xwiki.xpoll.internal.rest.DefaultXPollResource")
@Singleton
public class DefaultXPollResource extends XWikiResource implements XPollResource
{
    @Inject private XPollManager xPollManager;

    @Inject private Logger logger;

    @Override public Response saveXPollAnswers(String wikiName, String spaces, String pageName)
    {
        XWikiContext context = getXWikiContext();
        XWikiRequest request = context.getRequest();
        Map<String, String> votedProposals = new HashMap<>();
        DocumentReference documentReference = new DocumentReference(wikiName, Arrays.asList(spaces.split("/")),
            pageName);
        
        try {
            XWikiDocument doc = context.getWiki().getDocument(documentReference, context);
            BaseObject xpollObj = doc.getXObject(new LocalDocumentReference("XPoll", "XPollClass"));
            if (xpollObj == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            for (String s : request.getParameterMap().keySet()) {
                votedProposals.put(s, request.getParameterMap().get(s)[0]);
            }

            xPollManager.execute(doc, votedProposals, context);

        } catch (XWikiException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }
}
