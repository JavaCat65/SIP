/* 
 * Copyright 2019 Alexander Akhtyamov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elpy.sip.boot;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexander Akhtyamov
 */
public class SystemProperties {
    protected static final String SIP_HOME_DIR_KEY = "sip.home.dir";
    protected static final String SIP_LOG_DIR_KEY = "sip.log.dir";
    protected static final String SIP_LIB_MODULE_API_DIR_KEY = "sip.lib.module.api.dir";
    protected static final String SIP_LIB_MODULE_LOG_DIR_KEY = "sip.lib.log.dir";
    protected static final String SIP_LIB_MODULE_LIB_DIR_KEY = "sip.lib.module.lib.dir";
    protected static final String SIP_LIB_COMPONENT_API_DIR_KEY = "sip.lib.component.api.dir";
    protected static final String SIP_LIB_COMPONENT_LIB_DIR_KEY = "sip.lib.component.lib.dir";
    protected static final String SIP_LIB_CORE_DIR_KEY = "sip.lib.core.dir";
    
    public static Map<String,String> getDefaultRelPathVars(){
        Map<String,String> result = new HashMap<>();
        result.put(SIP_LOG_DIR_KEY, "log");
        result.put(SIP_LIB_MODULE_API_DIR_KEY, "lib/module-api");
        result.put(SIP_LIB_MODULE_LOG_DIR_KEY, "lib/log");
        result.put(SIP_LIB_MODULE_LIB_DIR_KEY, "lib/module-lib");
        result.put(SIP_LIB_COMPONENT_API_DIR_KEY, "lib/component-api");
        result.put(SIP_LIB_COMPONENT_LIB_DIR_KEY, "lib/component-lib");
        result.put(SIP_LIB_CORE_DIR_KEY, "lib/core");
        return result;
    }
}
