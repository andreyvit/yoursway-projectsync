package com.yoursway.projectsync.ui.handlers;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;

import com.yoursway.projectsync.core.IProjectSyncFeedback;
import com.yoursway.projectsync.core.ProjectSync;

public class SynchronizeNowHandler extends AbstractHandler {
    
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        final Shell shell = window.getShell();
        ProjectSync.sync(new IProjectSyncFeedback() {
            
            public void finished(final Collection<String> warnings) {
                shell.getDisplay().asyncExec(new Runnable() {
                    
                    public void run() {
                        if (!warnings.isEmpty()) {
                            StringBuilder message = new StringBuilder();
                            message.append("The following warnings were issued during synchronization:\n");
                            for (String warning : warnings)
                                message.append("Ñ ").append(warning).append("\n");
                            MessageDialog.openWarning(shell, "ProjectSync", message.toString());
                        }
                    }
                    
                });
            }
            
        });
        return null;
    }
}
