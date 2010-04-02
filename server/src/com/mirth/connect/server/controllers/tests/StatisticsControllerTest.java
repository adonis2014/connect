/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.controllers.tests;

import java.util.Properties;

import junit.framework.TestCase;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.ChannelStatisticsController;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.tools.ScriptRunner;

public class StatisticsControllerTest extends TestCase {
	private ChannelStatisticsController statisticsController = ControllerFactory.getFactory().createChannelStatisticsController();
	private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
	private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
	private Channel sampleChannel;

	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("derby-database.sql");

		// create a sample channel
		sampleChannel = new Channel();
		sampleChannel.setId(configurationController.generateGuid());
		sampleChannel.setName("Sample Channel");
		sampleChannel.setDescription("This is a sample channel");
		sampleChannel.setEnabled(true);
		sampleChannel.setVersion(configurationController.getServerVersion());
		sampleChannel.setRevision(0);
		sampleChannel.setSourceConnector(new Connector());
		sampleChannel.setPreprocessingScript("return 1;");

		Properties sampleProperties = new Properties();
		sampleProperties.setProperty("testProperty", "true");
		sampleChannel.setProperties(sampleProperties);

		channelController.updateChannel(sampleChannel, true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

//	public void testGetStatistics() throws ControllerException {
//		// Important: the received count is incremented twice to simulate the
//		// errant behavior of Mule
//		statisticsController.incReceivedCount(sampleChannel.getId());
//		statisticsController.incReceivedCount(sampleChannel.getId());
//
//		statisticsController.incSentCount(sampleChannel.getId());
//		statisticsController.incErrorCount(sampleChannel.getId());
//
//		ChannelStatistics testStatistics = statisticsController.getStatistics(sampleChannel.getId());
//
//		Assert.assertEquals(sampleChannel.getId(), testStatistics.getChannelId());
//		Assert.assertEquals(1, testStatistics.getReceivedCount());
//		Assert.assertEquals(1, testStatistics.getSentCount());
//		Assert.assertEquals(1, testStatistics.getErrorCount());
//	}

}