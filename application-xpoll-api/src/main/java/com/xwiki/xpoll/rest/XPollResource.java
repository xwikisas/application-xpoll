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
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Provides the list of entries from an existing.
 *
 * @version $Id$
 * @since 12.10
 */
@Path("/xpoll/{wikiName}/spaces/{spaceName: .+}/pages/{pageName}")
public interface XPollResource
{
    /**
     *
     * @param wikiName sada
     * @param spaces sadad
     * @param pageName gfas
     * @return gfda
     */
    @GET
    Response test(
        @PathParam("wikiName") String wikiName,
        @PathParam("spaceName") @Encoded String spaces,
        @PathParam("pageName") String pageName);
    // getting the form parameters from context request
    // https://github.com/xwiki/xwiki-platform/blob/a0e4456359e3682c98f33013be3076eda75b863a/
    // xwiki-platform-core/xwiki-platform-notifications/xwiki-platform-notifications-rest/src/
    // main/java/org/xwiki/notifications/rest/internal/DefaultNotificationsResource.java#L237-L239

    /**
     *  sad a.
     * @param wikiName asdasd
     * @param spaces sadasd
     * @param pageName sadad
     * @return dasdas
     */
    @PUT
    Response saveXPollAnswers(
        @PathParam("wikiName") String wikiName,
        @PathParam("spaceName") @Encoded String spaces,
        @PathParam("pageName") String pageName
    );
}
