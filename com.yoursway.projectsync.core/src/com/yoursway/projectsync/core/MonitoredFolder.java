package com.yoursway.projectsync.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;

public final class MonitoredFolder implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final File location;
    
    public MonitoredFolder(File location) {
        Assert.isNotNull(location);
        this.location = location;
    }
    
    public File location() {
        return location;
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
    
    public void findProjects(Collection<Addition> paths, Collection<String> warnings) {
        File[] children = location.listFiles();
        if (children == null) {
            warnings.add("Folder " + location + " does not exist.");
            return;
        }
        for (File child : children)
            if (child.isDirectory()) {
                String workingSet = child.getName();
                scanChildren(child, paths, warnings, workingSet, 3);
            }
    }
    
    private void scanChildren(File parent, Collection<Addition> paths, Collection<String> warnings,
            String workingSet, int depth) {
        File[] children = parent.listFiles();
        if (children != null)
            for (File child : children)
                if (child.isDirectory())
                    if (isProjectDirectory(child))
                        paths.add(new Addition(child, workingSet));
                    else if (depth > 0)
                        scanChildren(child, paths, warnings, workingSet, depth - 1);
    }
    
    static boolean isProjectDirectory(File directory) {
        return new File(directory, ".project").isFile();
    }
    
    static boolean isSubdiretory(File parent, File possibleSubdir) {
        if (possibleSubdir == null)
            return false;
        if (parent.equals(possibleSubdir))
            return true;
        return isSubdiretory(parent, possibleSubdir.getParentFile());
    }
    
    public void findProjects2(Collection<Move> moves, Collection<String> warnings, IProject project,
            Collection<String> workingSets) throws IOException {
        File[] children = location.listFiles();
        if (children == null) {
            warnings.add("Folder " + location + " does not exist.");
            return;
        }
        for (File child : children)
            if (child.isDirectory()) {
                String workingSet = child.getName();
                if (workingSets.contains(workingSet)) {
                    IPath projectLocation = project.getLocation();
                    if (projectLocation != null
                            && isSubdiretory(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile()
                                    .getCanonicalFile(), projectLocation.toFile().getCanonicalFile()))
                        moves.add(new Move(project, getNewProjectsDirectory(child)));
                }
            }
    }
    
    private File getNewProjectsDirectory(File workingSetDir) {
        File file = new File(workingSetDir, "default");
        if (file.isDirectory() && !isProjectDirectory(file))
            return file;
        
        List<File> candidateChildren = new ArrayList<File>();
        File[] children = workingSetDir.listFiles();
        if (children != null)
			for (File child : children)
				if (child.isDirectory() && !isBogusDirectory(child)
						&& !isProjectDirectory(child))
					candidateChildren.add(child);
        
        if (candidateChildren.size() == 1)
            return candidateChildren.iterator().next();
        else
            return workingSetDir;
    }
    
    private boolean isBogusDirectory(File child) {
		String name = child.getName();
		return name.matches("^[._](git|svn|hg|bzr)$");
	}

	@Override
    public String toString() {
        return location.toString();
    }
    
    public void findMoves(List<Move> moves, List<String> warnings, IProject project,
            Collection<String> workingSets) {
        try {
            findProjects2(moves, warnings, project, workingSets);
        } catch (IOException e) {
            e.printStackTrace();
            warnings.add("Exception " + e.getClass().getSimpleName() + " while processing " + location + ": "
                    + e.getMessage());
        }
    }
    
}
