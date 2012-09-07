/*
 * Copyright 2012 the original author or authors.
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

import com.j256.ormlite.jdbc.JdbcConnectionSource

import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder
import griffon.util.CallableWithArgs
import static griffon.util.GriffonNameUtils.isBlank

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
class ConnectionSourceHolder implements OrmliteProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionSourceHolder)
    private static final Object[] LOCK = new Object[0]
    private final Map<String, JdbcConnectionSource> connections = [:]

    String[] getConnectionNames() {
        List<String> databaseNames = new ArrayList().addAll(connections.keySet())
        databaseNames.toArray(new String[databaseNames.size()])
    }

    JdbcConnectionSource getConnection(String databaseName = 'default') {
        if(isBlank(databaseName)) databaseName = 'default'
        retrieveConnection(databaseName)
    }

    void setConnection(String databaseName = 'default', JdbcConnectionSource connection) {
        if(isBlank(databaseName)) databaseName = 'default'
        storeConnection(databaseName, connection)
    }

    Object withOrmlite(String databaseName = 'default', Closure closure) {
        JdbcConnectionSource connection = fetchConnection(databaseName)
        if(LOG.debugEnabled) LOG.debug("Executing statement on connection '$databaseName'")
        try {
            return closure(databaseName, connection)
        } finally {
            connection.close()
        }
    }

    public <T> T withOrmlite(String databaseName = 'default', CallableWithArgs<T> callable) {
        JdbcConnectionSource connection = fetchConnection(databaseName)
        if(LOG.debugEnabled) LOG.debug("Executing statement on connection '$databaseName'")
        callable.args = [databaseName, connection] as Object[]
        try {
            return callable.call()
        } finally {
            connection.close()
        }
    }
    
    boolean isConnectionConnected(String databaseName) {
        if(isBlank(databaseName)) databaseName = 'default'
        retrieveConnection(databaseName) != null
    }
    
    void disconnectConnection(String databaseName) {
        if(isBlank(databaseName)) databaseName = 'default'
        storeConnection(databaseName, null)
    }

    private JdbcConnectionSource fetchConnection(String databaseName) {
        if(isBlank(databaseName)) databaseName = 'default'
        JdbcConnectionSource connection = retrieveConnection(databaseName)
        if(connection == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = OrmliteConnector.instance.createConfig(app)
            connection = OrmliteConnector.instance.connect(app, config, databaseName)
        }

        if(connection == null) {
            throw new IllegalArgumentException("No such ormlite connection configuration for name $databaseName")
        }
        connection
    }

    private JdbcConnectionSource retrieveConnection(String databaseName) {
        synchronized(LOCK) {
            connections[databaseName]
        }
    }

    private void storeConnection(String databaseName, JdbcConnectionSource connection) {
        synchronized(LOCK) {
            connections[databaseName] = connection
        }
    }
}
