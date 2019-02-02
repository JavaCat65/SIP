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
package org.elpy.sip.core;

import java.net.URLClassLoader;

/**
 *
 * @author Alexander Akhtyamov
 */
public class Core {
    private URLClassLoader moduleApiClassLoader;
    private URLClassLoader moduleAPILogClassLoader;
    private URLClassLoader moduleAPILogLibClassLoader;
    private URLClassLoader componentAPIClassLoader;
    private URLClassLoader componentAPILibClassLoader;
    private URLClassLoader coreClassLoader;
    
    private void checkClassLoaders() {
        if(moduleApiClassLoader==null) {
            throw new IllegalStateException("Module API ClassLoader is not set");
        }
        if(moduleAPILogClassLoader==null) {
            throw new IllegalStateException("Module API with Logging libs ClassLoader is not set");
        }
        if(moduleAPILogLibClassLoader==null) {
            throw new IllegalStateException("Module API with Logging and other libs ClassLoader is not set");
        }
        if(componentAPIClassLoader==null) {
            throw new IllegalStateException("Platform Component API ClassLoader is not set");
        }
        if(componentAPILibClassLoader==null) {
            throw new IllegalStateException("Platform Component API with libs ClassLoader is not set");
        }
        if(coreClassLoader==null) {
            throw new IllegalStateException("Core ClassLoader is not set");
        }
    }
    
    public void start() {
        checkClassLoaders();
    }

    public URLClassLoader getModuleApiClassLoader() {
        return moduleApiClassLoader;
    }

    public void setModuleApiClassLoader(URLClassLoader moduleApiClassLoader) {
        this.moduleApiClassLoader = moduleApiClassLoader;
    }

    public URLClassLoader getModuleAPILogClassLoader() {
        return moduleAPILogClassLoader;
    }

    public void setModuleAPILogClassLoader(URLClassLoader moduleAPILogClassLoader) {
        this.moduleAPILogClassLoader = moduleAPILogClassLoader;
    }

    public URLClassLoader getModuleAPILogLibClassLoader() {
        return moduleAPILogLibClassLoader;
    }

    public void setModuleAPILogLibClassLoader(URLClassLoader moduleAPILogLibClassLoader) {
        this.moduleAPILogLibClassLoader = moduleAPILogLibClassLoader;
    }

    public URLClassLoader getComponentAPIClassLoader() {
        return componentAPIClassLoader;
    }

    public void setComponentAPIClassLoader(URLClassLoader componentAPIClassLoader) {
        this.componentAPIClassLoader = componentAPIClassLoader;
    }

    public URLClassLoader getComponentAPILibClassLoader() {
        return componentAPILibClassLoader;
    }

    public void setComponentAPILibClassLoader(URLClassLoader componentAPILibClassLoader) {
        this.componentAPILibClassLoader = componentAPILibClassLoader;
    }

    public URLClassLoader getCoreClassLoader() {
        return coreClassLoader;
    }

    public void setCoreClassLoader(URLClassLoader coreClassLoader) {
        this.coreClassLoader = coreClassLoader;
    }
}
