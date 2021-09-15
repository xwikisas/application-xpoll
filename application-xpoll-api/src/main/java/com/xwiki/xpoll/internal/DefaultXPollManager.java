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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.xpoll.XPollException;
import com.xwiki.xpoll.XPollManager;

/**
 * Provides methods to interact with the polls of a XWiki page.
 *
 * @version $Id$
 * @since 2.1
 */
@Component
@Singleton
public class DefaultXPollManager implements XPollManager
{
    static final String XPOLL_SPACE_NAME = "XPoll";

    static final LocalDocumentReference XPOLL_CLASS_REFERENCE =
        new LocalDocumentReference(XPOLL_SPACE_NAME, "XPollClass");

    static final LocalDocumentReference XPOLL_VOTES_CLASS_REFERENCE =
        new LocalDocumentReference(XPOLL_SPACE_NAME, "XPollVoteClass");

    static final String PROPOSALS = "proposals";

    static final String VOTES = "votes";

    static final String WINNER = "winner";

    static final String USER = "user";

    private static final String MISSING_XPOLL_OBJECT_MESSAGE = "The document [%s] does not have a poll object.";

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    @Named("compactwiki")
    private EntityReferenceSerializer<String> serializer;

    @Override
    public void vote(DocumentReference docReference, DocumentReference user, List<String> votedProposals)
        throws XPollException
    {
        XWikiContext context = contextProvider.get();
        XWikiDocument document = null;
        try {
            document = context.getWiki().getDocument(docReference, context);
            setUserVotes(votedProposals, context, document, user);
            updateWinner(context, document);
        } catch (XWikiException e) {
            throw new XPollException(String.format("Failed to vote for [%s] on behalf of [%s].", docReference, user),
                e);
        }
    }

    @Override
    public String getRestURL(DocumentReference documentReference)
    {

        String contextPath = contextProvider.get().getRequest().getContextPath();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(contextPath);
            stringBuilder.append("/rest/wikis/");
            stringBuilder
                .append(URLEncoder.encode(documentReference.getWikiReference().getName(), XWiki.DEFAULT_ENCODING));
            for (SpaceReference spaceReference : documentReference.getSpaceReferences()) {
                stringBuilder.append("/spaces/");
                stringBuilder.append(URLEncoder.encode(spaceReference.getName(), XWiki.DEFAULT_ENCODING));
            }
            stringBuilder.append("/pages/");
            stringBuilder.append(URLEncoder.encode(documentReference.getName(), XWiki.DEFAULT_ENCODING));
            stringBuilder.append("/xpoll");
            return stringBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(String.format("Failed to retrieve the REST URL of the document: [%s]",
                documentReference), e);
        }
    }

    @Override
    public Map<String, Integer> getVoteResults(DocumentReference documentReference) throws XPollException
    {
        XWikiContext context = contextProvider.get();
        try {
            XWikiDocument doc = context.getWiki().getDocument(documentReference, context);
            List<BaseObject> xpollVotes = doc.getXObjects(XPOLL_VOTES_CLASS_REFERENCE);
            BaseObject xpollObj = doc.getXObject(XPOLL_CLASS_REFERENCE);
            if (xpollObj == null) {
                throw new XPollException(String.format(MISSING_XPOLL_OBJECT_MESSAGE,
                    documentReference));
            }
            List<String> proposals = xpollObj.getListValue(PROPOSALS);
            return getXPollResults(xpollVotes, proposals);
        } catch (XWikiException e) {
            throw new XPollException(String
                .format("Failed to retrieve the vote results for poll [%s]. Root cause: [%s].", documentReference,
                    ExceptionUtils.getRootCauseMessage(e)));
        }
    }

    private void setUserVotes(List<String> votedProposals, XWikiContext context, XWikiDocument doc,
        DocumentReference user) throws XWikiException
    {
        String currentUserName = serializer.serialize(user, doc.getDocumentReference().getWikiReference().getName());
        BaseObject xpollVoteOfCUrrentUser = doc.getXObject(XPOLL_VOTES_CLASS_REFERENCE, USER,
            currentUserName, false);

        if (xpollVoteOfCUrrentUser == null) {
            xpollVoteOfCUrrentUser = doc.newXObject(XPOLL_VOTES_CLASS_REFERENCE, context);
        }

        xpollVoteOfCUrrentUser.set(USER, currentUserName, context);
        xpollVoteOfCUrrentUser.set(VOTES, votedProposals, context);
    }

    private void updateWinner(XWikiContext context, XWikiDocument doc) throws XWikiException, XPollException
    {
        BaseObject xpollObj = doc.getXObject(XPOLL_CLASS_REFERENCE);
        if (xpollObj == null) {
            throw new XPollException(String.format(MISSING_XPOLL_OBJECT_MESSAGE,
                doc.getDocumentReference()));
        }
        List<BaseObject> xpollVotes = doc.getXObjects(XPOLL_VOTES_CLASS_REFERENCE);

        List<String> proposals = xpollObj.getListValue(PROPOSALS);

        Map<String, Integer> voteCount = getXPollResults(xpollVotes, proposals);

        List<String> currentWinners = findWinner(voteCount);

        xpollObj.set(WINNER, String.join(",", currentWinners), context);
        doc.setAuthorReference(context.getAuthorReference());
        context.getWiki().saveDocument(doc, "New Vote", context);
    }

    private List<String> findWinner(Map<String, Integer> voteCount)
    {
        List<String> currentWinners = new ArrayList<>();

        int maxVotes = 0;
        for (Map.Entry<String, Integer> proposal : voteCount.entrySet()) {
            if (proposal.getValue() == maxVotes) {
                currentWinners.add(proposal.getKey());
            } else if (proposal.getValue() > maxVotes) {
                currentWinners.clear();
                currentWinners.add(proposal.getKey());
                maxVotes = proposal.getValue();
            }
        }
        return currentWinners;
    }

    private Map<String, Integer> getXPollResults(List<BaseObject> xpollVotes, List<String> proposals)
    {
        Map<String, Integer> voteCount = new HashMap<>();
        for (String proposal : proposals) {
            voteCount.put(proposal, 0);
        }
        for (BaseObject xpollVote : xpollVotes) {
            List<String> currentVotes = xpollVote.getListValue(VOTES);
            for (String currentVote : currentVotes) {
                if (proposals.contains(currentVote)) {
                    int nbvotes = voteCount.get(currentVote) + 1;
                    voteCount.put(currentVote, nbvotes);
                }
            }
        }
        return voteCount;
    }
}
