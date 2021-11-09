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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import com.xpn.xwiki.objects.BaseObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultXPollManager}.
 *
 * @version $Id$
 * @since 2.1
 */
@ComponentTest
public class DefaultPollResultsCalculatorTest
{
    @InjectMockComponents
    private DefaultPollResultsCalculator calculator;

    private final String proposal1 = "Proposal1";

    private final String proposal2 = "Proposal2";

    private final String proposal3 = "Proposal3";

    @Test
    void calculateResultsTest()
    {

        BaseObject ballot1 = mock(BaseObject.class);
        BaseObject ballot2 = mock(BaseObject.class);
        BaseObject ballot3 = mock(BaseObject.class);
        BaseObject ballot4 = mock(BaseObject.class);

        when(ballot1.getListValue(DefaultXPollManager.VOTES))
            .thenReturn(Collections.singletonList(proposal1));
        when(ballot2.getListValue(DefaultXPollManager.VOTES))
            .thenReturn(Collections.singletonList(proposal2));
        when(ballot3.getListValue(DefaultXPollManager.VOTES))
            .thenReturn(Collections.singletonList(proposal3));
        when(ballot4.getListValue(DefaultXPollManager.VOTES))
            .thenReturn(Collections.singletonList(proposal1));

        Map<String, Integer> results = calculator
            .calculateResults(Arrays.asList(ballot1, ballot2, ballot3, ballot4),
                Arrays.asList(proposal1, proposal2, proposal3));

        assertEquals(2, results.get(proposal1));
        assertEquals(1, results.get(proposal2));
        assertEquals(1, results.get(proposal3));
    }

    @Test
    void calculateResultsWithNoVotesTest()
    {
        Map<String, Integer> results =
            calculator.calculateResults(Collections.emptyList(), Arrays.asList(proposal1, proposal2, proposal3));

        assertEquals(0, results.get(proposal1));
        assertEquals(0, results.get(proposal2));
        assertEquals(0, results.get(proposal3));
    }

    @Test
    void calculateResultsWithNoProposalsTest()
    {
        Map<String, Integer> results =
            calculator.calculateResults(Collections.emptyList(), Collections.emptyList());
        assertEquals(0, results.size());
    }
}
