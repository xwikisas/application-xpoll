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
package com.xwiki.xpoll.rest;

import javax.ws.rs.Encoded;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.xwiki.rest.XWikiRestException;
import org.xwiki.stability.Unstable;

/**
 * Provides the functionality of saving the user's votes and determining the winning proposal in a desired page.
 *
 * @version $Id$
 * @since 2.1
 */
@Path("/wikis/{wikiName}/spaces/{spaceName: .+}/pages/{pageName}/xpoll")
@Unstable
public interface XPollResource
{
    /**
     * Used to create/modify a user's votes and to determine the winning proposal whenever a vote is cast.
     *
     * @param wikiName the name of the wiki in which the page resides
     * @param spaces the spaces of the page
     * @param pageName the name of the page
     * @return returns 404 if the page doesn't exist or doesn't have a XPollClass object, 200 otherwise
     * @throws XWikiRestException when failing to find the document or the document is missing the poll
     */
    @PUT
    Response saveXPollAnswers(
        @PathParam("wikiName") String wikiName,
        @PathParam("spaceName") @Encoded String spaces,
        @PathParam("pageName") String pageName
    ) throws XWikiRestException;
}
