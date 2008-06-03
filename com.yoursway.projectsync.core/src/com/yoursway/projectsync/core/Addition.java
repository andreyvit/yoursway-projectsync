/**
 * 
 */
package com.yoursway.projectsync.core;

import java.io.File;

public class Addition {
    
    private final File foundProject;
    private final String workingSet;
    
    public Addition(File foundProject, String workingSet) {
        if (foundProject == null)
            throw new NullPointerException("foundProject is null");
        if (workingSet == null)
            throw new NullPointerException("workingSet is null");
        this.foundProject = foundProject;
        this.workingSet = workingSet;
    }
    
    public File getFoundProject() {
        return foundProject;
    }

    public String getWorkingSet() {
        return workingSet;
    }
    
}