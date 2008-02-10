package com.yoursway.projectsync.core;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;

import org.eclipse.core.runtime.Assert;

public final class MonitoredFolder implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final File location;

    private final String workingSet;

    public MonitoredFolder(File location, String workingSet) {
        Assert.isNotNull(location);
        this.location = location;
        this.workingSet = workingSet;
    }

    public File location() {
        return location;
    }
    
    public String workingSet() {
        return workingSet;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MonitoredFolder other = (MonitoredFolder) obj;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        return true;
    }
    
    public void findProjects(Collection<File> paths, Collection<String> warnings) {
        File[] children = location.listFiles();
        if (children == null) {
            warnings.add("Folder " + location + " does not exist.");
            return;
        }
        for (File child : children)
            if (child.isDirectory())
                if (new File(child, ".project").isFile())
                    paths.add(child);
    }
    
    @Override
    public String toString() {
        return location.toString();
    }
    
}
