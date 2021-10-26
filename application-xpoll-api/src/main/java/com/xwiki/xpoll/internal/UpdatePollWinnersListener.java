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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.xpoll.XPollException;
import com.xwiki.xpoll.XPollManager;

/**
 * Event listener to listen to DocumentUpdatedEvent. When the event triggers, it calculates the winner of the poll and
 * sets it accordingly.
 *
 * @version $Id$
 * @since 2.1
 */
@Component
@Named("update-poll-winners")
@Singleton
public class UpdatePollWinnersListener implements EventListener
{
    private static final String XPOLL_WINNER_FIELD = "winner";

    private static final String UPDATING_FLAG = "updatingWinner";

    @Inject
    private XPollManager xPollManager;

    @Inject
    private Logger logger;

    private final List<Event> eventsList = Collections.singletonList(new DocumentUpdatedEvent());

    @Override
    public String getName()
    {
        return "update-poll-winners";
    }

    @Override
    public List<Event> getEvents()
    {
        return eventsList;
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiContext context = (XWikiContext) data;

        XWikiDocument doc = (XWikiDocument) source;
        BaseObject pollObject = doc.getXObject(DefaultXPollManager.XPOLL_CLASS_REFERENCE);
        if (pollObject != null && context.get(UPDATING_FLAG) == null) {
            String winner = "";
            String status = pollObject.getStringValue("status");
            if ("active".equals(status) || "finished".equals(status)) {
                Map<String, Integer> voteResults;
                try {
                    DocumentReference docRef = doc.getDocumentReference();
                    voteResults = xPollManager.getVoteResults(docRef);
                } catch (XPollException e) {
                    logger.warn(e.getMessage(), e);
                    return;
                }
                List<String> currentWinners = getWinners(voteResults);
                winner = String.join(",", currentWinners);
            }
            String oldWinner = pollObject.getStringValue(XPOLL_WINNER_FIELD);
            // We don't want to display the winner if the status is In Preparation or unselected, thus we set it as
            // empty.
            if (!oldWinner.equals(winner)) {
                pollObject.set(XPOLL_WINNER_FIELD, winner, context);
                try {
                    context.put(UPDATING_FLAG, true);
                    context.getWiki().saveDocument(doc, "Updated winner", context);
                } catch (XWikiException e) {
                    logger.warn(e.getMessage(), e);
                } finally {
                    context.put(UPDATING_FLAG, null);
                }
            }
        }
    }

    private List<String> getWinners(Map<String, Integer> voteResults)
    {
        int maxVotes = 0;
        List<String> currentWinners = new ArrayList<>();
        for (Map.Entry<String, Integer> proposal : voteResults.entrySet()) {
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
}
