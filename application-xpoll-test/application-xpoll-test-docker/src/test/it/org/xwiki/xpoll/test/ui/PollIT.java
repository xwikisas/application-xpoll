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
package org.xwiki.xpoll.test.ui;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.panels.test.po.ApplicationsPanel;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.CreatePagePage;
import org.xwiki.test.ui.po.ViewPage;
import org.xwiki.xpoll.test.po.ActiveStatusViewPage;
import org.xwiki.xpoll.test.po.FinishedStatusViewPage;
import org.xwiki.xpoll.test.po.InPreparationStatusViewPage;
import org.xwiki.xpoll.test.po.XPollEditPage;
import org.xwiki.xpoll.test.po.XPollHomePage;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Functional tests for the Poll Application.
 * 
 * @version $Id$
 * @since 2.2
 */
@UITest
class PollIT
{
    public static final String pollName = "Poll 1";

    public static final String pollDescription = "Poll 1 Description";

    public static final String pollProposals = "1,2,3,4,5";

    public static final String statusActive = "active";

    public static final String statusFinished = "finished";
    
    public static final String statusInPreparation = "inpreparation";

    public ArrayList<String> proposals = new ArrayList<String>(Arrays.asList(pollProposals.split(",")));

    @BeforeAll
    static void createUsers(TestUtils setup)
    {
        setup.createUser("JaneDoe", "pass", setup.getURLToNonExistentPage(), "first_name", "Jane", "last_name", "Doe");
    }
    
    @BeforeEach
    void setUp(TestUtils setup) {
        setup.login("JaneDoe", "pass");
        DocumentReference pageRef = new DocumentReference("xwiki", Arrays.asList("XPoll", pollName), "WebHome");
        setup.deletePage(pageRef);
    }

    @Test
    @Order(1)
    void appEntryRedirectsToHomePage()
    {
        ApplicationsPanel applicationPanel = ApplicationsPanel.gotoPage();
        ViewPage vp = applicationPanel.clickApplication("Polls");
        assertEquals(XPollHomePage.getSpace(), vp.getMetaDataValue("space"));
        assertEquals(XPollHomePage.getPage(), vp.getMetaDataValue("page"));
    }

    @Test
    @Order(2)
    void createNewEntryWithInPreparationStatus()
    {
        String status = statusInPreparation;
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();

        createPage(xpollHomePage);

        XPollEditPage xpollEditPage = new XPollEditPage();
        assertEquals(pollName, xpollEditPage.getName());
        xpollEditPage.setDescription(pollDescription);
        xpollEditPage.setStatus(status);
        xpollEditPage.setProposals(pollProposals);
        xpollEditPage.clickSaveAndView();

        InPreparationStatusViewPage inPreparationStatusViewPage = new InPreparationStatusViewPage();
        assertEquals(pollDescription, inPreparationStatusViewPage.getPollDescription());
        assertEquals(xpollEditPage.getStatusInPreparation(), inPreparationStatusViewPage.getPollStatus());
        assertEquals(pollProposals, inPreparationStatusViewPage.getPollProposals());
    }

    @Test
    @Order(3)
    void createNewEntryWithActiveStatus()
    {
        String status = statusActive;
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();

        createPage(xpollHomePage);

        editPage(status);

        ActiveStatusViewPage activeStatusViewPage = new ActiveStatusViewPage();
        assertEquals(pollName, activeStatusViewPage.getDocumentTitle());
        assertEquals(pollDescription, activeStatusViewPage.getDescription());

        activeStatusViewPage.getProposals();
        assertEquals(this.proposals, activeStatusViewPage.pollProposals);
    }

    @Test
    @Order(5)
    void createNewEntryAndVoteProposal() {
        String status = statusActive;
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();

        createPage(xpollHomePage);
        editPage(status);

        ActiveStatusViewPage activeStatusViewPage = new ActiveStatusViewPage();

        activeStatusViewPage.voteProposal(1);

        ActiveStatusViewPage viewPageAfterVote = new ActiveStatusViewPage();
        String inputCheckAttribute = viewPageAfterVote.getVoteInput(1).getAttribute("checked");

        assertEquals("true", inputCheckAttribute);
    }

    @Test
    @Order(4)
    void createNewEntryWithFinishedStatus()
    {
        String status = statusFinished;
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();
        createPage(xpollHomePage);

        editPage(status);

        FinishedStatusViewPage finishedStatusViewPage = new FinishedStatusViewPage();
        assertEquals(pollName, finishedStatusViewPage.getDocumentTitle());
        assertEquals(pollDescription, finishedStatusViewPage.getDescription());

        finishedStatusViewPage.getProposals();
        assertEquals(this.proposals, finishedStatusViewPage.pollProposals);
    }
    
    private void createPage(XPollHomePage xpollHomePage)
    {
        CreatePagePage createPage = xpollHomePage.createPage();
        createPage.getDocumentPicker().setTitle(pollName);
        createPage.setTemplate("XPoll.Code.XPollTemplateProvider");
        createPage.clickCreate();
    }
    
    private XPollEditPage editPage(String status)
    {
        XPollEditPage xpollEditPage = new XPollEditPage();
        assertEquals(pollName, xpollEditPage.getName());
        xpollEditPage.setDescription(pollDescription);
        xpollEditPage.setStatus(status);
        xpollEditPage.setProposals(pollProposals);
        xpollEditPage.setType("single");
        xpollEditPage.clickSaveAndView();
        return xpollEditPage;
    }
}
