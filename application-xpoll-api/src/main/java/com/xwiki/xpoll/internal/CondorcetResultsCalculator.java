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

import com.xpn.xwiki.objects.BaseObject;

/**
 * Calculates the result of a poll in a condorcet way. The Schulze algorithm is used to rank the entries of the poll.
 * The winners of the poll are the entries satisfy the relation: p[E,X] >= p[X,E] where E is the winning entry, X any
 * other entry and p[A,B] equals to the number of voters who prefer the entry A over B.
 *
 * @version $Id$
 * @since 2.1
 */
public class CondorcetResultsCalculator implements PollResultsCalculator
{
    @Override
    public Map<String, Integer> getResults(List<BaseObject> xpollVotes,
        List<String> proposals)
    {
        Map<String, Map<String, Integer>> ballotsMatrix = new HashMap<>();

        for (String proposal : proposals) {
            ballotsMatrix.put(proposal, new HashMap<>());
            for (String s : proposals) {
                ballotsMatrix.get(proposal).put(s, 0);
            }
        }

        computeProposalWeights(xpollVotes, ballotsMatrix, proposals);

        Map<String, Map<String, Integer>> p = new HashMap<>();
        for (String proposal : proposals) {
            p.put(proposal, new HashMap<>());
            for (String s : proposals) {
                p.get(proposal).put(s, 0);
            }
        }

        computeProposalStronghestPaths(proposals, ballotsMatrix, p);

        Map<String, Integer> scores = new HashMap<>();
        // E wins since p[E,X] >= p[X,E] for every other candidate X.
        for (String p1 : p.keySet()) {
            for (String p2 : p.get(p1).keySet()) {
                int value = scores.getOrDefault(p1, 0);
                if (!p1.equals(p2)) {
                    if (p.get(p1).getOrDefault(p2, 0) >= p.get(p2).getOrDefault(p1, 0)) {
                        scores.put(p1, ++value);
                    } else {
                        scores.put(p1, value);
                    }
                }
            }
        }
        return scores;
    }

    private void computeProposalStronghestPaths(List<String> proposals, Map<String, Map<String, Integer>> ballotsMatrix,
        Map<String, Map<String, Integer>> p)
    {
        for (String p1 : ballotsMatrix.keySet()) {
            for (String p2 : ballotsMatrix.get(p1).keySet()) {
                updateValue(ballotsMatrix, p, p1, p2);
            }
        }

        for (String i : ballotsMatrix.keySet()) {
            for (String j : ballotsMatrix.get(i).keySet()) {
                if (!i.equals(j)) {
                    for (String k : proposals) {
                        if (!i.equals(k) && !j.equals(k)) {
                            p.get(j).put(k, Math.max(p.get(j).get(k), Math.min(p.get(j).get(i), p.get(i).get(k))));
                        }
                    }
                }
            }
        }
    }

    private void updateValue(Map<String, Map<String, Integer>> ballotsMatrix, Map<String, Map<String, Integer>> p,
        String p1, String p2)
    {
        if (!p1.equals(p2)) {
            if (ballotsMatrix.get(p1).get(p2) > ballotsMatrix.get(p2).get(p1)) {
                p.get(p1).put(p2, ballotsMatrix.get(p1).get(p2));
            } else {
                p.get(p1).put(p2, 0);
            }
        }
    }

    private void computeProposalWeights(List<BaseObject> xpollVotes, Map<String, Map<String, Integer>> ballotsMatrix,
        List<String> proposals)
    {
        List<String> notVotedProposals = new ArrayList<>(proposals);

        for (BaseObject xpollVote : xpollVotes) {
            List<String> currentVotes = xpollVote.getListValue(DefaultXPollManager.VOTES);

            notVotedProposals.removeAll(currentVotes);

            for (int i = 0; i < currentVotes.size(); i++) {
                Map<String, Integer> line = ballotsMatrix.get(currentVotes.get(i));
                for (int j = i + 1; j < currentVotes.size(); j++) {
                    int value = line.get(currentVotes.get(j));
                    line.put(currentVotes.get(j), value + 1);
                }
                for (String notVotedProposal : notVotedProposals) {
                    int value = line.get(notVotedProposal);
                    line.put(notVotedProposal, value + 1);
                }
            }
            notVotedProposals.addAll(proposals);
        }
    }
}
