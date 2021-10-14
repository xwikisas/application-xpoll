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

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * An empty implementation of the DefaultPollResultsCalculator with the sole purpose of mapping the type of a poll
 * (Single option, Multiple Option, Condorcet Poll) with a way to calculate its results. Since the Multiple Option and
 * Single Option polls have their winner calculated in the same manner, they will inherit the behaviour of a common
 * parent.
 *
 * @version $Id$
 * @since 2.1
 */
@Component
@Named("multi")
@Singleton
public class MultiSelectionPollResultsCalculator extends DefaultPollResultsCalculator
{
}
