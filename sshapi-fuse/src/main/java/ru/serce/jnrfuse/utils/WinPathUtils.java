/**
 * Copyright (c) 2020 The JavaSSH Project
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package ru.serce.jnrfuse.utils;

import jnr.posix.util.Platform;
import ru.serce.jnrfuse.FuseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Locating winfsp installation path
 */
public class WinPathUtils {

    /**
     * Best attempt to find the winfsp library
     *
     * @return winfsp installation paths or null if it can't be found
     * @throws FuseException if can't be found
     */
    public static String getWinFspPath() {
        if (!Platform.IS_WINDOWS) {
            throw new FuseException("WinFsp can only be configured on Windows");
        }

        // if path was set as a property, don't try any default value, just bail if not found
        String configuredPath = System.getProperty("jnrfuse.winfsp.path");
        if (configuredPath != null && !configuredPath.isEmpty()) {
            if (!libExists(configuredPath)) {
                throw new FuseException("Configured winfsp library (jnrfuse.winfsp.path property) " +
                        configuredPath + " can't be found");
            }
            return configuredPath;
        }

        // try extract from registry
        String registryPath = extractRegLibraryPath();
        if (libExists(registryPath)) {
            return registryPath;
        }

        // last ditch attempt
        String defaultPath = "C:\\Program Files (x86)\\WinFsp\\bin\\winfsp-x64.dll";
        if (libExists(defaultPath)) {
            return defaultPath;
        }
        // can't find it anywhere
        throw new FuseException("Can't find winfsp library. Please make sure that winfsp is installed or configure " +
                "the path to dll manually using the `jnrfuse.winfsp.path` property");
    }

    private static String extractRegLibraryPath() {
        String regInstallRecord = extractRegInstallRecord();
        if (regInstallRecord == null) {
            return null;
        }
        // 4 spaces seem to be a default separator
        String[] keyParts = regInstallRecord.split(" {4}");
        if (keyParts.length < 4) {
            return null;
        }
        String libraryPath = keyParts[3];
        if (!libraryPath.endsWith("\\")) {
            libraryPath += "\\";
        }

        String fileName = "winfsp-x64.dll";
        String osArch = System.getProperty("os.arch");
        if (osArch.equalsIgnoreCase("x86")) {
            fileName = "winfsp-x86.dll";
        }
        return libraryPath + "bin\\" + fileName;
    }

    private static String extractRegInstallRecord() {
        if (Platform.IS_64_BIT) {
            return WinPathUtils.getRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\WinFsp", "InstallDir");
        } else {
            return WinPathUtils.getRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\WinFsp", "InstallDir");
        }
    }

    /**
     * Used to retrieve the value of a key from the registry path.
     *
     * @param path The path to the key
     * @param key  The key to retrieve the value of
     * @return Returns the registry record or null
     */
    private static String getRegistryKey(String path, String key) {
        String reqCmd = String.format("reg query %s /v %s", path, key);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(reqCmd).getInputStream()))) {
            return br.lines().collect(Collectors.joining(""));
        } catch (Exception e) {
            // can't extract value for some reason
            return null;
        }
    }

    private static boolean libExists(String path) {
        try {
            return path != null && !path.isEmpty() && Files.exists(Paths.get(path));
        } catch (InvalidPathException e) {
            // registry might contain any kind of garbage
            return false;
        }
    }
}
