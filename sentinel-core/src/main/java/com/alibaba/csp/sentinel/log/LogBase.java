/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.log;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.csp.sentinel.util.PidUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author leyou
 */
public class LogBase {

    public static final String CONFIG_LOG_DIR = "csp.sentinel.log.dir";

    private static final String LOG_CHARSET = "utf-8";
    private static final String DIR_NAME = "logs" + File.separator + "csp";
    private static final String USER_HOME = "user.home";
    private static String logBaseDir;

    static {
        initLogBaseDir();
    }

    private static void initLogBaseDir() {
        if (!setCustomLogDir()) {
            String userHome = System.getProperty(USER_HOME);
            setDefaultLogBaseDir(userHome);
        }
    }

    private static boolean setCustomLogDir() {
        String dir = System.getProperty(CONFIG_LOG_DIR);
        if (StringUtil.isBlank(dir)) {
            return false;
        }
        if (!dir.endsWith(File.separator)) {
            dir += File.separator;
        }
        setAndTryCreateLogDir(dir);
        return true;
    }

    /**
     * Get log file base directory path, the returned path is guaranteed end with {@link File#separator}
     *
     * @return log file base directory path.
     */
    public static String getLogBaseDir() {
        return logBaseDir;
    }

    /**
     * Set default log directory. The directory will be created if it does not exist.
     *
     * @param baseDir base directory
     */
    protected static void setDefaultLogBaseDir(String baseDir) {
        if (!baseDir.endsWith(File.separator)) {
            baseDir += File.separator;
        }
        String path = baseDir + DIR_NAME + File.separator;
        setAndTryCreateLogDir(path);
    }

    private static void setAndTryCreateLogDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean ok = dir.mkdirs();
            if (!ok) {
                System.err.println("[LogBase] WARNING: failed to create log directory: " + path);
            }
        }
        logBaseDir = path;
        System.out.println("[LogBase] Sentinel log directory: " + path);
    }

    protected static Handler makeLogger(String logName, Logger heliumRecordLog) {
        CspFormatter formatter = new CspFormatter();
        String baseDir = logBaseDir;
        if (!logBaseDir.endsWith(File.separator)) {
            baseDir += File.separator;
        }
        String fileName = baseDir + logName + ".pid" + PidUtil.getPid();
        Handler handler = null;
        try {
            handler = new DateFileLogHandler(fileName + ".%d", 1024 * 1024 * 200, 1, true);
            handler.setFormatter(formatter);
            handler.setEncoding(LOG_CHARSET);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (handler != null) {
            LoggerUtils.disableOtherHandlers(heliumRecordLog, handler);
        }
        heliumRecordLog.setLevel(Level.ALL);
        return handler;
    }
}
