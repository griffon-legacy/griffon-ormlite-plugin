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

import griffon.util.CallableWithArgs
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
final class OrmliteEnhancer {
    private static final Logger LOG = LoggerFactory.getLogger(OrmliteEnhancer)

    private OrmliteEnhancer() {}

    static void enhance(MetaClass mc, OrmliteProvider provider = ConnectionSourceHolder.instance) {
        if(LOG.debugEnabled) LOG.debug("Enhancing $mc with $provider")
        mc.withOrmlite = {Closure closure ->
            provider.withOrmlite('default', closure)
        }
        mc.withOrmlite << {String databaseName, Closure closure ->
            provider.withOrmlite(databaseName, closure)
        }
        mc.withOrmlite << {CallableWithArgs callable ->
            provider.withOrmlite('default', callable)
        }
        mc.withOrmlite << {String databaseName, CallableWithArgs callable ->
            provider.withOrmlite(databaseName, callable)
        }
    }
}
