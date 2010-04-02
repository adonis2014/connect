/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.plugins;

import java.util.Properties;

public interface ServerPlugin {
    public void init(Properties properties);

    public void start();

    public void update(Properties properties);

    public void onDeploy();

    public void stop();

    public Object invoke(String method, Object object, String sessionId) throws Exception;

    public Properties getDefaultProperties();
}