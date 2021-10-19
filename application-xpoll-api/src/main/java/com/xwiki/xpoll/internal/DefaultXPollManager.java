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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.xpoll.PollResultsCalculator;
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

    static final String XPOLL_TYPE = "type";

    static final String MISSING_XPOLL_OBJECT_MESSAGE = "The document [%s] does not have a poll object.";

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    @Named("compactwiki")
    private EntityReferenceSerializer<String> serializer;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Override
    public void vote(DocumentReference docReference, DocumentReference user, List<String> votedProposals)
        throws XPollException
    {
        XWikiContext context = contextProvider.get();
        XWikiDocument document;
        try {
            document = context.getWiki().getDocument(docReference, context).clone();
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
            BaseObject xpollObj = doc.getXObject(XPOLL_CLASS_REFERENCE);
            if (xpollObj == null) {
                throw new XPollException(String.format(MISSING_XPOLL_OBJECT_MESSAGE,
                    documentReference));
            }
            String pollType = xpollObj.getStringValue(XPOLL_TYPE);

            return getXPollResults(documentReference, pollType);
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
        BaseObject xpollVoteOfCurrentUser = doc.getXObject(XPOLL_VOTES_CLASS_REFERENCE, USER,
            currentUserName, false);

        if (xpollVoteOfCurrentUser == null) {
            xpollVoteOfCurrentUser = doc.newXObject(XPOLL_VOTES_CLASS_REFERENCE, context);
        }

        List<String> filteredProposals = votedProposals.stream().filter(p -> !p.isEmpty()).collect(Collectors.toList());

        xpollVoteOfCurrentUser.set(USER, currentUserName, context);
        xpollVoteOfCurrentUser.set(VOTES, filteredProposals, context);
        // Saving the document so the results calculator will also take the last vote into account.
        // The results calculator will retrieve the document using the reference rather than using the already
        // fetched document.
        context.getWiki().saveDocument(doc, "New Vote", context);
    }

    private void updateWinner(XWikiContext context, XWikiDocument doc) throws XWikiException, XPollException
    {
        BaseObject xpollObj = doc.getXObject(XPOLL_CLASS_REFERENCE);
        if (xpollObj == null) {
            throw new XPollException(String.format(MISSING_XPOLL_OBJECT_MESSAGE,
                doc.getDocumentReference()));
        }

        String pollType = xpollObj.getStringValue(XPOLL_TYPE);

        Map<String, Integer> voteCount = getXPollResults(doc.getDocumentReference(), pollType);

        List<String> currentWinners = findWinner(voteCount);

        xpollObj.set(WINNER, String.join(",", currentWinners), context);
        doc.setAuthorReference(context.getAuthorReference());
        // Saving the document that has updated the winner.
        context.getWiki().saveDocument(doc, "Updated winner", context);
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
        if (maxVotes == 0) {
            currentWinners.clear();
        }
        return currentWinners;
    }

    private Map<String, Integer> getXPollResults(DocumentReference documentReference, String pollType)
        throws XPollException
    {
        try {
            ComponentManager componentManager = componentManagerProvider.get();
            PollResultsCalculator calculator = componentManager.getInstance(PollResultsCalculator.class,
                pollType);
            return calculator.getResults(documentReference);
        } catch (ComponentLookupException e) {
            throw new XPollException(String.format(
                "The results could not be calculated because the poll type [%s] lacks an implementation.", pollType));
        }
    }
}
