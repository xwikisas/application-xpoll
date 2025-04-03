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
package com.xwiki.xpoll.test.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.panels.test.po.ApplicationsPanel;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.CreatePagePage;
import org.xwiki.test.ui.po.ViewPage;
import com.xwiki.xpoll.test.po.ActiveStatusViewPage;
import com.xwiki.xpoll.test.po.FinishedStatusViewPage;
import com.xwiki.xpoll.test.po.InPreparationStatusViewPage;
import com.xwiki.xpoll.test.po.XPollEditPage;
import com.xwiki.xpoll.test.po.XPollHomePage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Functional tests for the Poll Application.
 * 
 * @version $Id$
 * @since 2.2
 */
@UITest( properties = {
        // Add the RightsManagerPlugin needed by the test
        "xwikiCfgPlugins=com.xpn.xwiki.plugin.rightsmanager.RightsManagerPlugin" })
class PollIT
{
    private static final String POLL_NAME = "Poll 1";

    private static final String POLL_SPACE = "XPoll";

    private static final String POLL_DESCRIPTION = "Poll 1 Description";

    private static final String POLL_PROPOSALS = "1,2,3,4,5";

    private static final String STATUS_ACTIVE = "active";

    private static final String STATUS_FINISHED = "finished";

    private static final String STATUS_IN_PREPARATION = "inpreparation";

    private static final String STATUS_IN_PREPARATION_LABEL = "In preparation";

    private static final String TYPE_SINGLE = "single";

    private static final String POLL_VOTE_PRIVACY_PUBLIC_VALUE = "public";

    private static final String POLL_VOTE_PRIVACY_PRIVATE_VALUE = "private";

    private static final String POLL_VOTE_PRIVACY_PRIVATE_LABEL = "Private";

    private final ArrayList<String> proposals = new ArrayList<>(Arrays.asList(POLL_PROPOSALS.split(",")));

    @BeforeAll
    static void createUsers(TestUtils setup)
    {
        setup.createUser("JaneDoe", "pass", setup.getURLToNonExistentPage(), "first_name", "Jane", "last_name", "Doe");
        setup.createUser("JohnDoe", "pass", setup.getURLToNonExistentPage(), "first_name", "John", "last_name", "Doe");

        setup.loginAsSuperAdmin();
        setup.setGlobalRights(null, "XWiki.JohnDoe", "edit", false);
    }
    
    @BeforeEach
    void setUp(TestUtils setup) {
        setup.login("JaneDoe", "pass");
        DocumentReference pageRef = new DocumentReference("xwiki", Arrays.asList(POLL_SPACE, POLL_NAME), "WebHome");
        setup.deletePage(pageRef);
    }

    @Test
    @Order(1)
    void appEntryRedirectsToHomePage()
    {
        ApplicationsPanel applicationPanel = ApplicationsPanel.gotoPage();
        ViewPage vp = applicationPanel.clickApplication("Polls");
        Assertions.assertEquals(XPollHomePage.getSpace(), vp.getMetaDataValue("space"));
        assertEquals(XPollHomePage.getPage(), vp.getMetaDataValue("page"));
    }

    @Test
    @Order(2)
    void createNewEntryWithInPreparationStatus()
    {
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();

        createPage(xpollHomePage);
        editPage(STATUS_IN_PREPARATION, POLL_VOTE_PRIVACY_PRIVATE_VALUE);

        InPreparationStatusViewPage inPreparationStatusViewPage = new InPreparationStatusViewPage();
        assertEquals(POLL_DESCRIPTION, inPreparationStatusViewPage.getPollDescription());
        assertEquals(STATUS_IN_PREPARATION_LABEL, inPreparationStatusViewPage.getPollStatus());
        assertEquals(POLL_PROPOSALS, inPreparationStatusViewPage.getPollProposals());
        assertEquals(POLL_VOTE_PRIVACY_PRIVATE_LABEL, inPreparationStatusViewPage.getPollVotePrivacy());
    }

    @Test
    @Order(3)
    void createNewEntryWithActiveStatus()
    {
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();

        createPage(xpollHomePage);

        editPage(STATUS_ACTIVE);

        ActiveStatusViewPage activeStatusViewPage = new ActiveStatusViewPage();
        assertEquals(POLL_NAME, activeStatusViewPage.getDocumentTitle());
        assertEquals(POLL_DESCRIPTION, activeStatusViewPage.getDescription());

        activeStatusViewPage.getProposals();
        assertEquals(this.proposals, activeStatusViewPage.pollProposals);
    }

    @Test
    @Order(5)
    void createNewEntryAndVoteProposal() {
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();

        createPage(xpollHomePage);
        editPage(STATUS_ACTIVE);

        voteProposals(List.of(1));

        ActiveStatusViewPage viewPageAfterVote = new ActiveStatusViewPage();
        String inputCheckAttribute = viewPageAfterVote.getVoteInput(1).getAttribute("checked");

        assertEquals("true", inputCheckAttribute);
    }

    @Test
    @Order(4)
    void createNewEntryWithFinishedStatus()
    {
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();
        createPage(xpollHomePage);

        editPage(STATUS_FINISHED);

        FinishedStatusViewPage finishedStatusViewPage = new FinishedStatusViewPage();
        assertEquals(POLL_NAME, finishedStatusViewPage.getDocumentTitle());
        assertEquals(POLL_DESCRIPTION, finishedStatusViewPage.getDescription());

        finishedStatusViewPage.getProposals();
        assertEquals(this.proposals, finishedStatusViewPage.pollProposals);
    }

    @Test
    @Order(6)
    void createNewEntryWithVotePrivacyPublicFinalStatusActive(TestUtils setup) {
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();
        createPage(xpollHomePage);
        editPage(STATUS_ACTIVE, POLL_VOTE_PRIVACY_PUBLIC_VALUE);

        voteProposals(List.of(1));

        setup.loginAndGotoPage("JohnDoe", "pass", setup.getURL(new LocalDocumentReference(POLL_SPACE, POLL_NAME)));

        voteProposals(List.of(1));

        ActiveStatusViewPage activeStatusViewPage = new ActiveStatusViewPage();

        assertEquals(2, activeStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(activeStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertTrue(activeStatusViewPage.searchIfUserIsInTable("JaneDoe"));

        setup.loginAndGotoPage("JaneDoe", "pass", setup.getURL(new LocalDocumentReference(POLL_SPACE, POLL_NAME)));

        activeStatusViewPage = new ActiveStatusViewPage();

        assertEquals(2, activeStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(activeStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertTrue(activeStatusViewPage.searchIfUserIsInTable("JaneDoe"));
    }

    @Test
    @Order(7)
    void createNewEntryWithVotePrivacyPrivateFinalStatusActive(TestUtils setup) {
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();
        createPage(xpollHomePage);
        editPage(STATUS_ACTIVE, POLL_VOTE_PRIVACY_PRIVATE_VALUE);

        voteProposals(List.of(1));

        setup.loginAndGotoPage("JohnDoe", "pass", setup.getURL(new LocalDocumentReference(POLL_SPACE, POLL_NAME)));

        voteProposals(List.of(1));

        ActiveStatusViewPage activeStatusViewPage = new ActiveStatusViewPage();

        assertEquals(1, activeStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(activeStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertFalse(activeStatusViewPage.searchIfUserIsInTable("JaneDoe"));

        setup.loginAndGotoPage("JaneDoe", "pass", setup.getURL(new LocalDocumentReference(POLL_SPACE, POLL_NAME)));
        setup.gotoPage(POLL_SPACE, POLL_NAME);

        activeStatusViewPage = new ActiveStatusViewPage();

        assertEquals(2, activeStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(activeStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertTrue(activeStatusViewPage.searchIfUserIsInTable("JaneDoe"));
    }

    @Test
    @Order(8)
    void createNewEntryWithVotePrivacyPublicFinalStatusFinal(TestUtils setup) {
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();
        createPage(xpollHomePage);
        editPage(STATUS_ACTIVE, POLL_VOTE_PRIVACY_PUBLIC_VALUE);

        voteProposals(List.of(1));

        setup.loginAndGotoPage("JohnDoe", "pass", setup.getURL(new LocalDocumentReference(POLL_SPACE, POLL_NAME)));

        voteProposals(List.of(1));

        setup.loginAndGotoPage("JaneDoe", "pass", setup.getURL(new LocalDocumentReference(POLL_SPACE, POLL_NAME)));

        ActiveStatusViewPage activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.edit();

        XPollEditPage xpollEditPage = new XPollEditPage();
        xpollEditPage.setStatus(STATUS_FINISHED);
        xpollEditPage.clickSaveAndView();

        FinishedStatusViewPage finishedStatusViewPage = new FinishedStatusViewPage();

        assertEquals(2, finishedStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(finishedStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertTrue(finishedStatusViewPage.searchIfUserIsInTable("JaneDoe"));

        setup.loginAndGotoPage("JohnDoe", "pass", setup.getURL(new LocalDocumentReference(POLL_SPACE, POLL_NAME)));
        setup.gotoPage(POLL_SPACE, POLL_NAME);

        finishedStatusViewPage = new FinishedStatusViewPage();

        assertEquals(2, finishedStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(finishedStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertTrue(finishedStatusViewPage.searchIfUserIsInTable("JaneDoe"));
    }

    @Test
    @Order(9)
    void createNewEntryWithVotePrivacyPrivateFinalStatusFinal(TestUtils setup) {
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();
        createPage(xpollHomePage);
        editPage(STATUS_ACTIVE, POLL_VOTE_PRIVACY_PRIVATE_VALUE);

        voteProposals(List.of(1));

        setup.loginAndGotoPage("JohnDoe", "pass", setup.getURL(new LocalDocumentReference(POLL_SPACE, POLL_NAME)));

        voteProposals(List.of(1));

        setup.loginAndGotoPage("JaneDoe", "pass", setup.getURL(new LocalDocumentReference(POLL_SPACE, POLL_NAME)));

        ActiveStatusViewPage activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.edit();

        XPollEditPage xpollEditPage = new XPollEditPage();
        xpollEditPage.setStatus(STATUS_FINISHED);
        xpollEditPage.clickSaveAndView();

        FinishedStatusViewPage finishedStatusViewPage = new FinishedStatusViewPage();

        assertEquals(2, finishedStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(finishedStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertTrue(finishedStatusViewPage.searchIfUserIsInTable("JaneDoe"));

        setup.loginAndGotoPage("JohnDoe", "pass", setup.getURL(new LocalDocumentReference(POLL_SPACE, POLL_NAME)));

        finishedStatusViewPage = new FinishedStatusViewPage();

        assertEquals(1, finishedStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(finishedStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertFalse(finishedStatusViewPage.searchIfUserIsInTable("JaneDoe"));
    }

    private void createPage(XPollHomePage xpollHomePage)
    {
        CreatePagePage createPage = xpollHomePage.createPage();
        createPage.getDocumentPicker().setTitle(POLL_NAME);
        createPage.setTemplate("XPoll.Code.XPollTemplateProvider");
        createPage.clickCreate();
    }
    
    private void editPage(String status)
    {
        XPollEditPage xpollEditPage = new XPollEditPage();
        assertEquals(POLL_NAME, xpollEditPage.getName());
        xpollEditPage.setDescription(POLL_DESCRIPTION);
        xpollEditPage.setStatus(status);
        xpollEditPage.setProposals(POLL_PROPOSALS);
        xpollEditPage.setType(TYPE_SINGLE);
        xpollEditPage.clickSaveAndView();
    }

    private void editPage(String status, String votePrivacy) {
        XPollEditPage xpollEditPage = new XPollEditPage();
        xpollEditPage.setDescription(POLL_DESCRIPTION);
        xpollEditPage.setStatus(status);
        xpollEditPage.setProposals(POLL_PROPOSALS);
        xpollEditPage.setType(TYPE_SINGLE);
        xpollEditPage.setVotePrivacy(votePrivacy);
        xpollEditPage.clickSaveAndView();
    }

    private void voteProposals(List<Integer> optionsIndexes) {
        ActiveStatusViewPage activeStatusViewPage = new ActiveStatusViewPage();
        for (Integer index : optionsIndexes) {
            activeStatusViewPage.voteProposal(index);
        }
    }
}
