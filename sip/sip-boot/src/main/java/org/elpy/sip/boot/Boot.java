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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.elpy.sip.boot.log.BootLogger;

/**
 *
 * @author Alexander Akhtyamov
 */
public class Boot {

    private static final String SIP_RUN_PROPERTIES_LOCATION = "bin/run.properties";
    private static final String SIP_PLATFORM_CORE_CLASS_NAME = "org.elpy.sip.core.Core";

    private static final String SIP_CORE_METHOD_SET_MODULE_API_CLASS_LOADER = "setModuleApiClassLoader";
    private static final String SIP_CORE_METHOD_SET_MODULE_API_LOG_CLASS_LOADER = "setModuleAPILogClassLoader";
    private static final String SIP_CORE_METHOD_SET_MODULE_API_LOG_LIB_CLASS_LOADER = "setModuleAPILogLibClassLoader";
    private static final String SIP_CORE_METHOD_SET_COMPONENT_API_CLASS_LOADER = "setComponentAPIClassLoader";
    private static final String SIP_CORE_METHOD_SET_COMPONENT_API_LIB_CLASS_LOADER = "setComponentAPILibClassLoader";
    private static final String SIP_CORE_METHOD_SET_CORE_CLASS_LOADER = "setCoreClassLoader";
    private static final String SIP_CORE_METHOD_START = "start";

    private final BootLogger logger;

    private final File homeDir;
    private final Properties fileProps;
    private Object core;

    private Boot(File homeDir, Properties fileProps) throws Exception {
        File logDir = new File(System.getProperty(SystemProperties.SIP_LOG_DIR_KEY));
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        String today = formatter.format(LocalDate.now());
        File logFile = new File(logDir, "boot." + today + ".log");
        logFile.createNewFile();
        this.logger = new BootLogger(logFile, true);
        this.fileProps = fileProps;
        this.homeDir = homeDir;
        logger.write(String.format("System variable %s is set to: %s", SystemProperties.SIP_HOME_DIR_KEY,
                homeDir.getAbsolutePath()));
        logger.write(String.format("System variable %s is set to: %s", SystemProperties.SIP_LOG_DIR_KEY,
                logDir.getAbsolutePath()));
    }

    private void continueLoading() throws Exception {
        try {
            _continueLoading();
        } catch (Exception e) {
            logger.write(String.format("Exception thrown during loading: %s", e.getMessage()), e);
            throw e;
        }
    }

    private void _continueLoading() throws Exception {
        setSystemVarsFromProps();
        setSystemPathVarsFromDefaults();
        createAndConfigureAndStartCore();
    }

    private void createAndConfigureAndStartCore() throws Exception {
        logger.write("Creating Platform ClassLoaders...");
        URLClassLoader moduleAPIClassLoader = createClassLoader("ModuleApiClassLoader",
                ClassLoader.getSystemClassLoader(), System.getProperty(SystemProperties.SIP_LIB_MODULE_API_DIR_KEY));
        URLClassLoader moduleAPILogClassLoader = createClassLoader("ModuleAPILogClassLoader", moduleAPIClassLoader,
                System.getProperty(SystemProperties.SIP_LIB_MODULE_LOG_DIR_KEY));
        URLClassLoader moduleAPILogLibClassLoader = createClassLoader("ModuleAPILogLibClassLoader",
                moduleAPILogClassLoader, System.getProperty(SystemProperties.SIP_LIB_MODULE_LIB_DIR_KEY));
        URLClassLoader componentAPIClassLoader = createClassLoader("ComponentAPIClassLoader", moduleAPILogClassLoader,
                System.getProperty(SystemProperties.SIP_LIB_COMPONENT_API_DIR_KEY));
        URLClassLoader componentAPILibClassLoader = createClassLoader("ComponentAPILibClassLoader",
                componentAPIClassLoader, System.getProperty(SystemProperties.SIP_LIB_COMPONENT_LIB_DIR_KEY));
        URLClassLoader coreClassLoader = createClassLoader("CoreClassLoader", componentAPILibClassLoader,
                System.getProperty(SystemProperties.SIP_LIB_CORE_DIR_KEY));
        logger.write("--> All Platform ClassLoaders are created");
        logger.write("Loading Platform Core...");
        Class<?> coreClass = coreClassLoader.loadClass(SIP_PLATFORM_CORE_CLASS_NAME);
        core = createCore(coreClass);
        logger.write("-->Platform Core loaded");
        logger.write("Configuring Platform Core...");
        injectClassLoaderToCore(coreClass, SIP_CORE_METHOD_SET_MODULE_API_CLASS_LOADER, moduleAPIClassLoader);
        injectClassLoaderToCore(coreClass, SIP_CORE_METHOD_SET_MODULE_API_LOG_CLASS_LOADER, moduleAPILogClassLoader);
        injectClassLoaderToCore(coreClass, SIP_CORE_METHOD_SET_MODULE_API_LOG_LIB_CLASS_LOADER, moduleAPILogLibClassLoader);
        injectClassLoaderToCore(coreClass, SIP_CORE_METHOD_SET_COMPONENT_API_CLASS_LOADER, componentAPIClassLoader);
        injectClassLoaderToCore(coreClass, SIP_CORE_METHOD_SET_COMPONENT_API_LIB_CLASS_LOADER, componentAPILibClassLoader);
        injectClassLoaderToCore(coreClass, SIP_CORE_METHOD_SET_CORE_CLASS_LOADER, coreClassLoader);
        logger.write("Platform Core configured...");
        logger.write("Starting Platform Core...");
        Method starter = coreClass.getMethod(SIP_CORE_METHOD_START);
        starter.invoke(core);
    }

    private Object createCore(Class<?> coreClass) throws Exception {
        Constructor<?> constructor = coreClass.getConstructor();
        return constructor.newInstance();
    }

    private void injectClassLoaderToCore(Class<?> coreClass, String methodName, URLClassLoader classLoader) throws Exception{
        logger.write(String.format("---->Injecting ClassLoader %s to core method: %s", classLoader.getName(), methodName));
        Method setter = coreClass.getMethod(methodName, URLClassLoader.class);
        setter.invoke(core, classLoader);
        logger.write(String.format("-->ClassLoader %s successfully injected to core", classLoader.getName()));
    }

    private URLClassLoader createClassLoader(String name, ClassLoader parent, String rootDirPath) throws Exception {
        File rootDir = new File(rootDirPath);
        if (!rootDir.isDirectory()) {
            throw new Exception(String.format("Directory does not exist: %s", rootDir.getAbsolutePath()));
        }
        logger.write(String.format("Creating %s ClassLoader using libs from %s", name, rootDirPath));
        File[] libs = rootDir.listFiles();
        URL[] libURLs = new URL[libs.length];
        for (int i = 0; i < libs.length; i++) {
            File lib = libs[i];
            URL url = lib.toURI().toURL();
            logger.write(String.format("----> Adding URL to %s ClassLoader: %s", name, url.toString()));
            libURLs[i] = url;
        }
        URLClassLoader result = new URLClassLoader(name, libURLs, parent);
        logger.write(String.format("--> ClassLoader created: %s", name));
        return result;
    }

    private void setSystemVarsFromProps() {
        logger.write("Setting system properties from run.properties");
        Set<Object> keys = fileProps.keySet();
        keys.stream().forEach(keyObject -> {
            String key = keyObject.toString();
            String value = System.getProperty(key);
            if (value != null) {// already set
                logger.write(String.format("--> System property is already set, skipping: %s=%s ", key, value));
                return;
            }
            value = fileProps.getProperty(key);
            logger.write(String.format("----> Setting system property from run.properties: %s=%s ", key, value));
            System.setProperty(key, value);
        });
        logger.write("--> All system properties from run.properties are set");
    }

    private void setSystemPathVarsFromDefaults() throws Exception {
        logger.write("Setting system path properties from defaults");
        Map<String, String> relPathVars = SystemProperties.getDefaultRelPathVars();
        Set<String> keys = relPathVars.keySet();
        for (String key : keys) {
            String value = System.getProperty(key);
            if (value != null) {// already set
                logger.write(String.format("--> System property is already set, skipping: %s=%s ", key, value));
                continue;
            }
            File file = new File(homeDir, relPathVars.get(key));
            if (!file.exists()) {
                throw new Exception(String.format("File(Folder) %s does not exist (specified by %s system variable)",
                        file.getAbsolutePath(), key));
            }
            value = file.getCanonicalPath();
            logger.write(String.format("----> Setting system property from defaults: %s=%s ", key, value));
            System.setProperty(key, value);
        }
        logger.write("--> All system properties from defaults are set");
    }

    public static void main(String... args) throws Exception {
        String homeDirString = System.getProperty(SystemProperties.SIP_HOME_DIR_KEY);
        if (homeDirString == null) {
            throw new Exception(String.format("System variable %s is not set", SystemProperties.SIP_HOME_DIR_KEY));
        }
        File homeDir = new File(homeDirString);
        if (!homeDir.exists()) {
            throw new Exception(String.format("File(Folder) %s does not exist (specified by %s system variable)",
                    homeDir.getAbsolutePath(), SystemProperties.SIP_HOME_DIR_KEY));
        }
        homeDir = homeDir.getCanonicalFile();

        System.setProperty(SystemProperties.SIP_HOME_DIR_KEY, homeDir.getCanonicalPath());

        File propertiesFile = new File(homeDir, SIP_RUN_PROPERTIES_LOCATION);
        if (!propertiesFile.isFile()) {
            throw new Exception(String.format("Properties file %s does not exist", propertiesFile.getAbsolutePath()));
        }
        Properties fileProps = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(propertiesFile);
            fileProps.load(is);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception ignored) {
            }
        }

        Map<String, String> relPathVars = SystemProperties.getDefaultRelPathVars();

        setLogDirPath(homeDir, fileProps, relPathVars);

        Boot boot = new Boot(homeDir, fileProps);
        boot.continueLoading();
    }

    private static void setLogDirPath(File homeDir, Properties fileProps, Map<String, String> relPathVars)
            throws Exception {
        String logDirString = System.getProperty(SystemProperties.SIP_LOG_DIR_KEY);
        if (logDirString != null) {// already set
            return;
        }
        logDirString = fileProps.getProperty(SystemProperties.SIP_LOG_DIR_KEY);
        if (logDirString != null) {// set from properties file
            File file = new File(logDirString);
            if (!file.exists()) {
                throw new Exception(String.format("File (folder) specified by %s system variable does not exist: %s",
                        SystemProperties.SIP_LOG_DIR_KEY, file.getAbsolutePath()));
            }
            System.setProperty(SystemProperties.SIP_LOG_DIR_KEY, file.getCanonicalPath());
            return;
        }
        logDirString = relPathVars.get(SystemProperties.SIP_LOG_DIR_KEY);
        if (logDirString != null) {// set defaults
            File file = new File(homeDir, logDirString);
            if (!file.exists()) {
                throw new Exception(String.format("File (folder) specified by %s system variable does not exist: %s",
                        SystemProperties.SIP_LOG_DIR_KEY, file.getAbsolutePath()));
            }
            System.setProperty(SystemProperties.SIP_LOG_DIR_KEY, file.getCanonicalPath());
            return;
        }
        throw new Exception("Log directory location is not specified anywhere");
    }
}
