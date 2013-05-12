/*
 * Copyright 2011-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.plugins.ormlite

import com.j256.ormlite.support.ConnectionSource

import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder
import static griffon.util.GriffonNameUtils.isBlank

/**
 * @author Andres Almiray
 */
class ConnectionSourceHolder {
    private static final String DEFAULT = 'default'
    private static final Object[] LOCK = new Object[0]
    private final Map<String, ConnectionSource> connections = [:]

    private static final ConnectionSourceHolder INSTANCE

    static {
        INSTANCE = new ConnectionSourceHolder()
    }

    static ConnectionSourceHolder getInstance() {
        INSTANCE
    }

    private ConnectionSourceHolder() {}

    String[] getConnectionSourceNames() {
        List<String> databaseNames = new ArrayList().addAll(connections.keySet())
        databaseNames.toArray(new String[databaseNames.size()])
    }

    ConnectionSource getConnectionSource(String databaseName = DEFAULT) {
        if (isBlank(databaseName)) databaseName = DEFAULT
        retrieveConnectionSource(databaseName)
    }

    void setConnectionSource(String databaseName = DEFAULT, ConnectionSource connection) {
        if (isBlank(databaseName)) databaseName = DEFAULT
        storeConnectionSource(databaseName, connection)
    }

    boolean isConnectionSourceConnected(String databaseName) {
        if (isBlank(databaseName)) databaseName = DEFAULT
        retrieveConnectionSource(databaseName) != null
    }
    
    void disconnectConnectionSource(String databaseName) {
        if (isBlank(databaseName)) databaseName = DEFAULT
        storeConnectionSource(databaseName, null)
    }

    ConnectionSource fetchConnectionSource(String databaseName) {
        if (isBlank(databaseName)) databaseName = DEFAULT
        ConnectionSource connection = retrieveConnectionSource(databaseName)
        if (connection == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = OrmliteConnector.instance.createConfig(app)
            connection = OrmliteConnector.instance.connect(app, config, databaseName)
        }

        if (connection == null) {
            throw new IllegalArgumentException("No such ormlite connection configuration for name $databaseName")
        }
        connection
    }

    private ConnectionSource retrieveConnectionSource(String databaseName) {
        synchronized(LOCK) {
            connections[databaseName]
        }
    }

    private void storeConnectionSource(String databaseName, ConnectionSource connection) {
        synchronized(LOCK) {
            connections[databaseName] = connection
        }
    }
}
