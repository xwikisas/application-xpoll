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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.xpn.xwiki.objects.BaseObject;

/**
 * Calculates the results of a poll using the plurality method. The score of an entry is equal to the number of votes it
 * got. The winner is the one with the most votes.
 *
 * @version $Id$
 * @since 2.1
 */
@Component
@Singleton
public class DefaultPollResultsCalculator extends AbstractPollResultsCalculator
{
    @Override
    public Map<String, Integer> calculateResults(List<BaseObject> xpollVotes, List<String> proposals)
    {
        Map<String, Integer> voteCount = new HashMap<>();
        for (String proposal : proposals) {
            voteCount.put(proposal, 0);
        }
        for (BaseObject xpollVote : xpollVotes) {
            List<String> currentVotes = xpollVote.getListValue(DefaultXPollManager.VOTES);
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
