package com.yoursway.projectsync.core;

import java.io.File;

import org.eclipse.core.resources.IProject;

public class Move {
    
    private final IProject project;
    private final File parentFolder;

    public Move(IProject project, File parentFolder) {
        if (project == null)
            throw new NullPointerException("project is null");
        if (parentFolder == null)
            throw new NullPointerException("parentFolder is null");
        this.project = project;
        this.parentFolder = parentFolder;
    }
    
    public IProject getProject() {
        return project;
    }
    
    public File getParentFolder() {
        return parentFolder;
    }
    
}
