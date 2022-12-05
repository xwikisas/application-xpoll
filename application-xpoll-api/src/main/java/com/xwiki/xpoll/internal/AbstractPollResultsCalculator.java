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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xwiki.xpoll.PollResultsCalculator;
import com.xwiki.xpoll.XPollException;

/**
 * Base class for all vote calculators.
 *
 * @version $Id$
 * @since 2.1
 */
public abstract class AbstractPollResultsCalculator implements PollResultsCalculator
{
    @Inject
    private Provider<XWikiContext> contextProvider;

    @Override public Map<String, Integer> getResults(DocumentReference documentReference) throws XPollException
    {
        XWikiContext context = contextProvider.get();
        XWikiDocument document;
        try {
            document = context.getWiki().getDocument(documentReference, context);

            BaseObject xpollObj = document.getXObject(DefaultXPollManager.XPOLL_CLASS_REFERENCE);
            if (xpollObj == null) {
                throw new XPollException(String.format(DefaultXPollManager.MISSING_XPOLL_OBJECT_MESSAGE,
                    document.getDocumentReference()));
            }
            List<BaseObject> xpollVotes = document.getXObjects(DefaultXPollManager.XPOLL_VOTES_CLASS_REFERENCE);
            xpollVotes.removeAll(Collections.singletonList(null));

            List<String> proposals = xpollObj.getListValue(DefaultXPollManager.PROPOSALS);

            return calculateResults(xpollVotes, proposals);
        } catch (XWikiException e) {
            throw new XPollException(String.format("Failed to compute the results for the poll [%s].",
                documentReference), e);
        }
    }

    protected abstract Map<String, Integer> calculateResults(List<BaseObject> xpollVotes, List<String> proposals);
}
