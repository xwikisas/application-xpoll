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
package com.xwiki.xpoll;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 * @since 2.1
 */
@Role
@Unstable
public interface XPollManager
{
    /**
     * @param page the page that contains an instance of XPollClass
     * @param votedProposals a array or Proposals that the user voted
     * @param user a reference to the user that cast the votes
     * @throws XWikiException thrown if the page is not found or if the function fails to create a new XPollVote or
     * save the Document
     */
    void vote(DocumentReference page, DocumentReference user, List<String> votedProposals) throws XWikiException;

    /**
     *
     * @param documentReference the document that we want to get the URL for
     * @return the REST URL of the XPoll associated with the specific document
     */
    String getRestURL(DocumentReference documentReference);

    /**
     *
     * @param documentReference a document reference
     * @return  a map that has the XPoll proposals as keys and the number of votes as values. The function returns an
     * empty map if the document doesn't have an XPollObject
     */
    Map<String, Integer> getVoteResults(DocumentReference documentReference);
}
