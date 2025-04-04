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
package com.xwiki.xpoll.test.po;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.ViewPage;

/**
 * @version $Id$
 */
public class FinishedStatusViewPage extends ViewPage
{
    @FindBy(xpath = "//div[@id = 'xwikicontent']/p[2]")
    public WebElement pollDescription;

    public List<WebElement> proposals = getDriver().findElements(By.xpath("//table//tr[1]//th[position()>1]"));

    public ArrayList<String> pollProposals = new ArrayList<String>();

    @FindBy(xpath = "//table[contains(@class, 'xpoll')]//tbody//tr//a")
    public List<WebElement> usersThatVotedTableRows;

    @FindBy(xpath = "//table[contains(@class, 'xpoll')]//tbody//tr//td[1][not(descendant::a) and not(contains(translate(., 'SCORES', 'scores'), 'scores'))]")
    public List<WebElement> guestUsersThatVotedTableRows;

    public String getDescription()
    {
        return pollDescription.getText();
    }

    public void getProposals()
    {
        for (WebElement proposal : proposals) {
            pollProposals.add(proposal.getText().trim());
        }
    }

    public int getNumberOfUsersThatAlreadyVotedFromTable() {
        return usersThatVotedTableRows.size();
    }

    public boolean searchIfUserIsInTable(String user) {
        return usersThatVotedTableRows.stream().anyMatch(vote -> user.equals(vote.getText()));
    }

    public int getNumberOfGuestsThatVotedFromTable()
    {
        return guestUsersThatVotedTableRows.size();
    }

    public boolean searchIfGuestUserIsInTable(String user)
    {
        return guestUsersThatVotedTableRows.stream().anyMatch(vote -> user.equals(vote.getText()));
    }
}
