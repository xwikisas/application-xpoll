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
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.xpoll.XPollManager;

public class DefaultXPollManager implements XPollManager
{
    private static final String SPACENAME = "XPoll";
    private static final LocalDocumentReference XPOLL_CLASSREFERENCE =
        new LocalDocumentReference(SPACENAME, "XPollClass");

    private static final LocalDocumentReference XPOLL_VOTES_CLASSREFERENCE =
        new LocalDocumentReference(SPACENAME, "XPollVoteClass");

    private static final String STATUS = "status";

    private static final String PROPOSALS = "proposals";

    private static final String VOTES = "votes";

    private static final String WINNER = "winner";

    private static final String USER = "user";

    @Inject
    private Logger logger;

    @Override
    public void execute(XWikiDocument page, Map<String, String> votedProposals, XWikiContext context)
    {
        try {
            BaseObject xpollObj = page.getXObject(XPOLL_CLASSREFERENCE);

            List<String> proposals = xpollObj.getListValue(PROPOSALS);

            setUserVotes(votedProposals, context, page, proposals);
            updateWinner(context, page, xpollObj, proposals);
        } catch (XWikiException e) {
            logger.error(e.getFullMessage());
        }
    }

    private void setUserVotes(Map<String, String> votedProposals, XWikiContext context, XWikiDocument doc,
        List<String> proposals) throws XWikiException
    {
        String currentUserName = context.getUserReference().getLocalDocumentReference().toString();
        BaseObject xpollVoteOfCUrrentUser = doc.getXObject(XPOLL_VOTES_CLASSREFERENCE, USER,
            currentUserName, false);
        Map<String, String> sortedVotes = new TreeMap<>();

        List<String> votes = new ArrayList<>();
        for (String proposal : proposals) {
            String proposalHash = String.valueOf(proposal.hashCode());
            String value = votedProposals.get(proposalHash);
            if (value != null) {
                if (StringUtils.isNumeric(value)) {
                    sortedVotes.put(value, proposal);
                } else {
                    votes.add(proposal);
                }
            } else if (votedProposals.get(currentUserName) != null
                && votedProposals.get(currentUserName).equals(proposalHash)) {
                votes.add(proposal);
                break;
            }
        }

        for (String s : sortedVotes.keySet()) {
            votes.add(sortedVotes.get(s));
        }

        if (xpollVoteOfCUrrentUser == null) {
            xpollVoteOfCUrrentUser = doc.newXObject(XPOLL_VOTES_CLASSREFERENCE, context);
        }

        xpollVoteOfCUrrentUser.set(USER, currentUserName, context);
        xpollVoteOfCUrrentUser.set(VOTES, votes, context);
    }


    private void updateWinner(XWikiContext context, XWikiDocument doc,
        BaseObject xpollClass, List<String> proposals) throws XWikiException
    {
        List<BaseObject> xpollVotes = doc.getXObjects(XPOLL_VOTES_CLASSREFERENCE);

        Map<String, Integer> voteCount = new HashMap<>();
        for (String proposal : proposals) {
            voteCount.put(proposal, 0);
        }
        int maxVote = 0;
        List<String> currentWinners = new ArrayList<>();
        boolean isProposal;
        for (BaseObject xpollVote : xpollVotes) {
            List<String> currentVotes = xpollVote.getListValue(VOTES);
            for (String currentVote : currentVotes) {
                isProposal = false;
                for (String proposal : proposals) {
                    if (currentVote.equals(proposal)) {
                        isProposal = true;
                        break;
                    }
                }
                if (isProposal) {
                    int nbvotes = voteCount.get(currentVote);
                    nbvotes += 1;
                    voteCount.put(currentVote, nbvotes);
                    if (nbvotes == maxVote) {
                        currentWinners.add(currentVote);
                    } else if (nbvotes > maxVote) {
                        currentWinners = new ArrayList<>();
                        currentWinners.add(currentVote);
                        maxVote = nbvotes;
                    }
                }
            }
        }

        xpollClass.set(WINNER, String.join(",", currentWinners), context);
        doc.setAuthorReference(context.getAuthorReference());
        context.getWiki().saveDocument(doc, "New Vote", context);
    }
}
