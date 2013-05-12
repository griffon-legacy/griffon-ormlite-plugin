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

import griffon.core.GriffonApplication
import griffon.util.Environment
import griffon.util.Metadata
import griffon.util.ConfigUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource

/**
 * @author Andres Almiray
 */
@Singleton
final class OrmliteConnector {
    private static final String DEFAULT = 'default'
    private static final Logger LOG = LoggerFactory.getLogger(OrmliteConnector)
    private bootstrap

    ConfigObject createConfig(GriffonApplication app) {
        if (!app.config.pluginConfig.ormlite) {
            app.config.pluginConfig.ormlite = ConfigUtils.loadConfigWithI18n('OrmliteConfig')
        }
        app.config.pluginConfig.ormlite
    }

    private ConfigObject narrowConfig(ConfigObject config, String databaseName) {
        if (config.containsKey('database') && databaseName == DEFAULT) {
            return config.database
        } else if (config.containsKey('databases')) {
            return config.databases[databaseName]
        }
        return config
    }

    ConnectionSource connect(GriffonApplication app, ConfigObject config, String databaseName = DEFAULT) {
        if (ConnectionSourceHolder.instance.isConnectionSourceConnected(databaseName)) {
            return ConnectionSourceHolder.instance.getConnectionSource(databaseName)
        }

        config = narrowConfig(config, databaseName)
        app.event('OrmliteConnectStart', [config, databaseName])
        ConnectionSource connection = startOrmlite(config)
        ConnectionSourceHolder.instance.setConnectionSource(databaseName, connection)
        bootstrap = app.class.classLoader.loadClass('BootstrapOrmlite').newInstance()
        bootstrap.metaClass.app = app
        resolveOrmliteProvider(app).withOrmlite { dn, c -> bootstrap.init(dn, c) }
        app.event('OrmliteConnectEnd', [databaseName, connection])
        connection
    }

    void disconnect(GriffonApplication app, ConfigObject config, String databaseName = DEFAULT) {
        if (ConnectionSourceHolder.instance.isConnectionSourceConnected(databaseName)) {
            config = narrowConfig(config, databaseName)
            ConnectionSource connection = ConnectionSourceHolder.instance.getConnectionSource(databaseName)
            app.event('OrmliteDisconnectStart', [config, databaseName, connection])
            resolveOrmliteProvider(app).withOrmlite { dn, c -> bootstrap.destroy(dn, c) }
            stopOrmlite(config, connection)
            app.event('OrmliteDisconnectEnd', [config, databaseName])
            ConnectionSourceHolder.instance.disconnectConnectionSource(databaseName)
        }
    }

    OrmliteProvider resolveOrmliteProvider(GriffonApplication app) {
        def ormliteProvider = app.config.ormliteProvider
        if (ormliteProvider instanceof Class) {
            ormliteProvider = ormliteProvider.newInstance()
            app.config.ormliteProvider = ormliteProvider
        } else if (!ormliteProvider) {
            ormliteProvider = DefaultOrmliteProvider.instance
            app.config.ormliteProvider = ormliteProvider
        }
        ormliteProvider
    }

    private ConnectionSource startOrmlite(ConfigObject config) {
        String url      = config.url.toString()
        String username = config.username.toString()
        String password = config.password.toString()

        ConnectionSource connection = new JdbcPooledConnectionSource(url, username, password)

        for (entry in config) {
            if (entry.key in ['class', 'metaClass', 'url', 'username', 'password']) continue
            connection[entry.key] = entry.value
        }

        connection.initialize()
        connection
    }

    private void stopOrmlite(ConfigObject config, ConnectionSource connection) {
        if (connection.isOpen()) connection.close()
    }
}