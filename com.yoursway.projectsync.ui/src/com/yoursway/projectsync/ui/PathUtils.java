package com.yoursway.projectsync.ui;

import java.io.File;

public class PathUtils {
    
    public static File toFile(String path) {
        String homeDir = System.getProperty("user.home");
        if (homeDir != null && path.startsWith("~/"))
            path = homeDir + "/" + path.substring(2);
        return new File(path);
    }
    
    public static String toString(File file) {
        String path = file.getPath();
        String homeDir = System.getProperty("user.home");
        if (homeDir != null && path.startsWith(homeDir + "/"))
            path = "~/" + path.substring(homeDir.length() + 1);
        return path;
    }
    
}
