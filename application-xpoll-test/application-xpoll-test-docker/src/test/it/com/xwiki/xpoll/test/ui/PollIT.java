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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.administration.test.po.EditGroupModal;
import org.xwiki.administration.test.po.GroupsPage;
import org.xwiki.model.reference.DocumentReference;
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
    public static final String pollName = "Poll 1";
    
    public static final String pollSpace = "XPoll";

    public static final String pollDescription = "Poll 1 Description";

    public static final String pollProposals = "1,2,3,4,5";

    public static final String statusActive = "active";

    public static final String statusFinished = "finished";
    
    public static final String statusInPreparation = "inpreparation";

    public static final String pollVotePrivacyPublicValue = "public";

    public static final String pollVotePrivacyPrivateValue = "private";

    public static final String pollVotePrivacyPrivateLabel = "Private";

    private static final String GROUP_NAME =  "Xwiki_no_edit_group";

    public ArrayList<String> proposals = new ArrayList<String>(Arrays.asList(pollProposals.split(",")));
    @BeforeAll
    static void createUsers(TestUtils setup)
    {
        setup.createUser("JaneDoe", "pass", setup.getURLToNonExistentPage(), "first_name", "Jane", "last_name", "Doe");
        setup.createUser("JohnDoe", "pass", setup.getURLToNonExistentPage(), "first_name", "John", "last_name", "Doe");

        setup.loginAsSuperAdmin();
        GroupsPage groupsPage = GroupsPage.gotoPage();
        groupsPage.addNewGroup(GROUP_NAME);

        EditGroupModal devsGroupModal = groupsPage.clickEditGroup(GROUP_NAME);
        devsGroupModal.addUsers("JohnDoe");
        groupsPage = GroupsPage.gotoPage();
        assertEquals("1", groupsPage.getMemberCount(GROUP_NAME));
        setup.setGlobalRights(GROUP_NAME, "JohnDoe", "edit", false);

        groupsPage.logout();
    }
    
    @BeforeEach
    void setUp(TestUtils setup) {
        setup.login("JaneDoe", "pass");
        DocumentReference pageRef = new DocumentReference("xwiki", Arrays.asList(pollSpace, pollName), "WebHome");
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
        String status = statusInPreparation;
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();

        createPage(xpollHomePage);

        XPollEditPage xpollEditPage = new XPollEditPage();
        assertEquals(pollName, xpollEditPage.getName());
        xpollEditPage.setDescription(pollDescription);
        xpollEditPage.setStatus(status);
        xpollEditPage.setProposals(pollProposals);
        xpollEditPage.setVotePrivacy(pollVotePrivacyPrivateValue);
        xpollEditPage.clickSaveAndView();

        InPreparationStatusViewPage inPreparationStatusViewPage = new InPreparationStatusViewPage();
        assertEquals(pollDescription, inPreparationStatusViewPage.getPollDescription());
        assertEquals(xpollEditPage.getStatusInPreparation(), inPreparationStatusViewPage.getPollStatus());
        assertEquals(pollProposals, inPreparationStatusViewPage.getPollProposals());
        assertEquals(pollVotePrivacyPrivateLabel, inPreparationStatusViewPage.getPollVotePrivacy());
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

    @Test
    @Order(6)
    void createNewEntryWithVotePrivacyPublicFinalStatusActive(TestUtils setup) {
        XPollHomePage xpollHomePage = XPollHomePage.gotoPage();
        createPage(xpollHomePage);

        XPollEditPage xpollEditPage = new XPollEditPage();
        xpollEditPage.setDescription(pollDescription);
        xpollEditPage.setStatus(statusActive);
        xpollEditPage.setProposals(pollProposals);
        xpollEditPage.setType("single");
        xpollEditPage.setVotePrivacy(pollVotePrivacyPublicValue);
        xpollEditPage.clickSaveAndView();

        ActiveStatusViewPage activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.voteProposal(1);
        activeStatusViewPage.logout();

        setup.login("JohnDoe", "pass");
        setup.gotoPage(pollSpace, pollName);
        activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.voteProposal(1);

        assertEquals(2, activeStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(activeStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertTrue(activeStatusViewPage.searchIfUserIsInTable("JaneDoe"));

        activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.logout();

        setup.login("JaneDoe", "pass");
        setup.gotoPage(pollSpace, pollName);

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

        XPollEditPage xpollEditPage = new XPollEditPage();
        xpollEditPage.setDescription(pollDescription);
        xpollEditPage.setStatus(statusActive);
        xpollEditPage.setProposals(pollProposals);
        xpollEditPage.setType("single");
        xpollEditPage.setVotePrivacy(pollVotePrivacyPrivateValue);
        xpollEditPage.clickSaveAndView();

        ActiveStatusViewPage activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.voteProposal(1);
        activeStatusViewPage.logout();

        setup.login("JohnDoe", "pass");
        setup.gotoPage(pollSpace, pollName);
        activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.voteProposal(1);

        assertEquals(1, activeStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(activeStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertFalse(activeStatusViewPage.searchIfUserIsInTable("JaneDoe"));

        activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.logout();

        setup.login("JaneDoe", "pass");
        setup.gotoPage(pollSpace, pollName);

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

        XPollEditPage xpollEditPage = new XPollEditPage();
        xpollEditPage.setDescription(pollDescription);
        xpollEditPage.setStatus(statusActive);
        xpollEditPage.setProposals(pollProposals);
        xpollEditPage.setType("single");
        xpollEditPage.setVotePrivacy(pollVotePrivacyPublicValue);
        xpollEditPage.clickSaveAndView();

        ActiveStatusViewPage activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.voteProposal(1);
        activeStatusViewPage.logout();

        setup.login("JohnDoe", "pass");
        setup.gotoPage(pollSpace, pollName);
        activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.voteProposal(1);

        activeStatusViewPage.logout();

        setup.login("JaneDoe", "pass");
        setup.gotoPage(pollSpace, pollName);
        activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.edit();
        xpollEditPage = new XPollEditPage();
        xpollEditPage.setStatus(statusFinished);
        xpollEditPage.clickSaveAndView();

        FinishedStatusViewPage finishedStatusViewPage = new FinishedStatusViewPage();

        assertEquals(2, finishedStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(finishedStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertTrue(finishedStatusViewPage.searchIfUserIsInTable("JaneDoe"));

        finishedStatusViewPage.logout();

        setup.login("JohnDoe", "pass");
        setup.gotoPage(pollSpace, pollName);

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

        XPollEditPage xpollEditPage = new XPollEditPage();
        xpollEditPage.setDescription(pollDescription);
        xpollEditPage.setStatus(statusActive);
        xpollEditPage.setProposals(pollProposals);
        xpollEditPage.setType("single");
        xpollEditPage.setVotePrivacy(pollVotePrivacyPrivateValue);
        xpollEditPage.clickSaveAndView();

        ActiveStatusViewPage activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.voteProposal(1);
        activeStatusViewPage.logout();

        setup.login("JohnDoe", "pass");
        setup.gotoPage(pollSpace, pollName);

        activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.voteProposal(1);
        activeStatusViewPage.logout();

        setup.login("JaneDoe", "pass");
        setup.gotoPage(pollSpace, pollName);

        activeStatusViewPage = new ActiveStatusViewPage();
        activeStatusViewPage.edit();
        xpollEditPage = new XPollEditPage();
        xpollEditPage.setStatus(statusFinished);
        xpollEditPage.clickSaveAndView();

        FinishedStatusViewPage finishedStatusViewPage = new FinishedStatusViewPage();

        assertEquals(2, finishedStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(finishedStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertTrue(finishedStatusViewPage.searchIfUserIsInTable("JaneDoe"));

        finishedStatusViewPage.logout();

        setup.login("JohnDoe", "pass");
        setup.gotoPage(pollSpace, pollName);

        finishedStatusViewPage = new FinishedStatusViewPage();

        assertEquals(1, finishedStatusViewPage.getNumberOfUsersThatAlreadyVotedFromTable());
        assertTrue(finishedStatusViewPage.searchIfUserIsInTable("JohnDoe"));
        assertFalse(finishedStatusViewPage.searchIfUserIsInTable("JaneDoe"));
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
