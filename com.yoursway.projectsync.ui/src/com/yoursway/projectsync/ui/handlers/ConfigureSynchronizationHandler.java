package com.yoursway.projectsync.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.yoursway.projectsync.ui.ProjectSyncConfigurationDialog;

public class ConfigureSynchronizationHandler extends AbstractHandler {
    
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        ProjectSyncConfigurationDialog dialog = new ProjectSyncConfigurationDialog(window.getShell());
        dialog.open();
        return null;
    }
}
