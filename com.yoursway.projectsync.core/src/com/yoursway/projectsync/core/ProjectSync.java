package com.yoursway.projectsync.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.IWorkingSet;

import com.yoursway.projectsync.folders.FolderList;

public class ProjectSync {
    
    static class Addition {
        
        private final File foundProject;
        private final MonitoredFolder folder;
        
        public Addition(File foundProject, MonitoredFolder folder) {
            this.foundProject = foundProject;
            this.folder = folder;
        }
        
        public File getFoundProject() {
            return foundProject;
        }
        
        public MonitoredFolder getFolder() {
            return folder;
        }
        
    }
    
    private static FolderList folderList() {
        return new FolderList();
    }
    
    public static Collection<MonitoredFolder> getFolders() {
        return folderList().get();
    }
    
    public static void addFolder(MonitoredFolder folder) {
        folderList().add(folder);
    }
    
    public static void removeFolder(MonitoredFolder folder) {
        folderList().remove(folder);
    }
    
    public static void clearFolders() {
        folderList().set(Collections.<MonitoredFolder> emptyList());
    }
    
    public static void sync(Collection<String> warnings) {
        if (warnings == null)
            warnings = new ArrayList<String>();
        
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot root = workspace.getRoot();
        Set<File> existingProjects = new HashSet<File>();
        for (IProject project : root.getProjects())
            existingProjects.add(project.getLocation().toFile());
        
        final ArrayList<Addition> additions = new ArrayList<Addition>();
        for (MonitoredFolder folder : getFolders()) {
            Set<File> foundProjects = new HashSet<File>();
            folder.findProjects(foundProjects, warnings);
            for (File location : foundProjects)
                if (!existingProjects.contains(location))
                    additions.add(new Addition(location, folder));
        }
        
        WorkspaceJob job = new WorkspaceJob("Adding missing projects") {
            
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                SubMonitor progress = SubMonitor.convert(monitor, "Adding missing projects",
                        additions.size() * 20);
                
                for (Addition addition : additions) {
                    File location = addition.getFoundProject();
                    String workingSetName = addition.getFolder().workingSet();
                    
                    String name = location.getName();
                    progress.setTaskName("Adding " + name);
                    IProjectDescription description = workspace.newProjectDescription(name);
                    description.setLocation(new Path(location.getAbsolutePath()));
                    IProject project = root.getProject(name);
                    if (project.exists()) {
                        //                        warnings.add("Project " + project + " already exists at " + project.getLocation() + ", but found at " 
                        //                                + location);
                        continue;
                    }
                    try {
                        project.create(description, progress.newChild(1));
                        project.open(progress.newChild(18));
                        
                        IWorkingSet ws = WorkingSetUtils.lookupWorkingSet(workingSetName);
                        WorkingSetUtils.addToWorkingSet(project, ws);
                        progress.worked(1);
                    } catch (CoreException e) {
                        e.printStackTrace();
                        //                        warnings.add("Cannot create project " + name + " because of " +
                        //                                e.getClass().getSimpleName() + ": " + e.getMessage());
                    }
                }
                
                return Status.OK_STATUS;
            }
            
        };
        job.schedule();
    }
    
}
