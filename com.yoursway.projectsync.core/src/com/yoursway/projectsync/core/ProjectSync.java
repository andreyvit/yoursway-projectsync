package com.yoursway.projectsync.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    
    public static void sync(final IProjectSyncFeedback feedback) {
        final List<String> warnings = new ArrayList<String>();
        
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot root = workspace.getRoot();
        final Set<File> existingProjects = new HashSet<File>();
        for (IProject project : root.getProjects())
            existingProjects.add(project.getLocation().toFile());
        
        final List<Addition> additions = new ArrayList<Addition>();
        for (MonitoredFolder folder : getFolders())
            folder.findProjects(additions, warnings);
        
        WorkspaceJob job = new WorkspaceJob("Adding missing projects") {
            
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                SubMonitor progress = SubMonitor.convert(monitor, "Adding missing projects",
                        additions.size() * 20);
                
                for (Addition addition : additions) {
                    File location = addition.getFoundProject();
                    String workingSetName = addition.getWorkingSet();
                    
                    String name = location.getName();
                    progress.setTaskName("Adding " + name);

                    IProject project = root.getProject(name);
                    try {
                        if (!project.exists()) {
                            IProjectDescription description = workspace.newProjectDescription(name);
                            description.setLocation(new Path(location.getAbsolutePath()));
                            project.create(description, progress.newChild(1));
                            project.open(progress.newChild(18));
                        }
                        
                        IWorkingSet ws = WorkingSetUtils.lookupWorkingSet(workingSetName);
                        WorkingSetUtils.addToWorkingSet(project, ws);
                        progress.worked(1);
                    } catch (CoreException e) {
                        e.printStackTrace();
                        warnings.add("Error processing project " + name + " because of " +
                                e.getClass().getSimpleName() + ": " + e.getMessage());
                    }
                }
                return Status.OK_STATUS;
            }
            
        };
        job.schedule();
    }
    
}
