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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.xpoll.XPollManager;

public class DefaultXPollManager implements XPollManager
{
    private static final String XPOLL_SPACE_NAME = "XPoll";

    private static final LocalDocumentReference XPOLL_CLASS_REFERENCE =
        new LocalDocumentReference(XPOLL_SPACE_NAME, "XPollClass");

    private static final LocalDocumentReference XPOLL_VOTES_CLASS_REFERENCE =
        new LocalDocumentReference(XPOLL_SPACE_NAME, "XPollVoteClass");

    private static final String PROPOSALS = "proposals";

    private static final String VOTES = "votes";

    private static final String WINNER = "winner";

    private static final String USER = "user";

    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    @Named("compactwiki")
    private EntityReferenceSerializer<String> serializer;

    @Override
    // one more method -> getVoteResults (primeste poll-ul - doc ref)
    public void vote(DocumentReference docReference, DocumentReference user, List<String> votedProposals)
        throws XWikiException
    {
        XWikiContext context = contextProvider.get();
        XWikiDocument document = context.getWiki().getDocument(docReference, context);

        setUserVotes(votedProposals, context, document, user);
        updateWinner(context, document);
    }

    @Override public String getRestURL(DocumentReference documentReference)
    {
        String contextPath = contextProvider.get().getRequest().getContextPath();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(contextPath);
        stringBuilder.append("/rest/wikis/");
        stringBuilder.append(documentReference.getWikiReference().getName());
        stringBuilder.append("/spaces/");

        for (SpaceReference spaceReference : documentReference.getSpaceReferences()) {
            stringBuilder.append(spaceReference.getName());
            stringBuilder.append('/');
        }

        stringBuilder.append("pages/");
        stringBuilder.append(documentReference.getName());
        stringBuilder.append("/xpoll");
        return stringBuilder.toString();
    }

    @Override
    public Map<String, Integer> getVoteResults(DocumentReference documentReference) {
        XWikiContext context = contextProvider.get();
        try {
            XWikiDocument doc = context.getWiki().getDocument(documentReference, context);
            List<BaseObject> xpollVotes = doc.getXObjects(XPOLL_VOTES_CLASS_REFERENCE);
            BaseObject xpollObj = doc.getXObject(XPOLL_CLASS_REFERENCE);
            List<String> proposals = xpollObj.getListValue(PROPOSALS);
            return getXPollResults(xpollVotes, proposals);
        } catch (XWikiException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
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

    private void updateWinner(XWikiContext context, XWikiDocument doc) throws XWikiException
    {
        BaseObject xpollObj = doc.getXObject(XPOLL_CLASS_REFERENCE);
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
