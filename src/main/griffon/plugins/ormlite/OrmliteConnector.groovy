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
import griffon.util.Environment
import griffon.util.Metadata
import griffon.util.CallableWithArgs
import griffon.util.ConfigUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
final class OrmliteConnector implements OrmliteProvider {
    private bootstrap

    private static final Logger LOG = LoggerFactory.getLogger(OrmliteConnector)

    Object withOrmlite(String databaseName = 'default', Closure closure) {
        ConnectionSourceHolder.instance.withOrmlite(databaseName, closure)
    }

    public <T> T withOrmlite(String databaseName = 'default', CallableWithArgs<T> callable) {
        return ConnectionSourceHolder.instance.withOrmlite(databaseName, callable)
    }

    // ======================================================

    ConfigObject createConfig(GriffonApplication app) {
        ConfigUtils.loadConfigWithI18n('OrmliteConfig')
    }

    private ConfigObject narrowConfig(ConfigObject config, String databaseName) {
        return databaseName == 'default' ? config.database : config.databases[databaseName]
    }

    JdbcConnectionSource connect(GriffonApplication app, ConfigObject config, String databaseName = 'default') {
        if (ConnectionSourceHolder.instance.isConnectionConnected(databaseName)) {
            return ConnectionSourceHolder.instance.getConnection(databaseName)
        }

        config = narrowConfig(config, databaseName)
        app.event('OrmliteConnectStart', [config, databaseName])
        JdbcConnectionSource connection = startOrmlite(config)
        ConnectionSourceHolder.instance.setConnection(databaseName, connection)
        bootstrap = app.class.classLoader.loadClass('BootstrapOrmlite').newInstance()
        bootstrap.metaClass.app = app
        bootstrap.init(databaseName, connection)
        app.event('OrmliteConnectEnd', [databaseName, connection])
        connection
    }

    void disconnect(GriffonApplication app, ConfigObject config, String databaseName = 'default') {
        if (ConnectionSourceHolder.instance.isConnectionConnected(databaseName)) {
            config = narrowConfig(config, databaseName)
            JdbcConnectionSource connection = ConnectionSourceHolder.instance.getConnection(databaseName)
            app.event('OrmliteDisconnectStart', [config, databaseName, connection])
            bootstrap.destroy(databaseName, connection)
            stopOrmlite(config, connection)
            app.event('OrmliteDisconnectEnd', [config, databaseName])
            ConnectionSourceHolder.instance.disconnectConnection(databaseName)
        }
    }

    private JdbcConnectionSource startOrmlite(ConfigObject config) {
        String url      = config.url.toString()
        String username = config.username.toString()
        String password = config.password.toString()

        JdbcConnectionSource connection = new JdbcConnectionSource(url, username, password)
        connection.initialize()
        connection
    }

    private void stopOrmlite(ConfigObject config, JdbcConnectionSource connection) {
        if (connection.isOpen()) connection.close()
    }
}
