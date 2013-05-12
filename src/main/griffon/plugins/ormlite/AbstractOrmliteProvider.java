/*
 * Copyright 2012-2013 the original author or authors.
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

package griffon.plugins.ormlite;

import griffon.util.CallableWithArgs;
import griffon.exceptions.GriffonException;
import groovy.lang.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.support.ConnectionSource;

import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public abstract class AbstractOrmliteProvider implements OrmliteProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractOrmliteProvider.class);
    private static final String DEFAULT = "default";

    public <R> R withOrmlite(Closure<R> closure) {
        return withOrmlite(DEFAULT, closure);
    }

    public <R> R withOrmlite(String databaseName, Closure<R> closure) {
        if (isBlank(databaseName)) databaseName = DEFAULT;
        if (closure != null) {
            ConnectionSource connection = getConnectionSource(databaseName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on databaseName '" + databaseName + "'");
            }
            // try {
                return closure.call(databaseName, connection);
            // } finally {
            //     try {
            //         connection.close();
            //     } catch (Exception e) {
            //         throw new GriffonException(e);
            //     }
            // }
        }
        return null;
    }

    public <R> R withOrmlite(CallableWithArgs<R> callable) {
        return withOrmlite(DEFAULT, callable);
    }

    public <R> R withOrmlite(String databaseName, CallableWithArgs<R> callable) {
        if (isBlank(databaseName)) databaseName = DEFAULT;
        if (callable != null) {
            ConnectionSource connection = getConnectionSource(databaseName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on databaseName '" + databaseName + "'");
            }
            // try {
                callable.setArgs(new Object[]{databaseName, connection});
                return callable.call();
            // } finally {
            //     try {
            //         connection.close();
            //     } catch (Exception e) {
            //         throw new GriffonException(e);
            //     }
            // }
        }
        return null;
    }

    protected abstract ConnectionSource getConnectionSource(String databaseName);
}