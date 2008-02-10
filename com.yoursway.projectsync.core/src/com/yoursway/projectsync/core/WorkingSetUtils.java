package com.yoursway.projectsync.core;

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.internal.ui.workingsets.WorkingSetModel;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

public class WorkingSetUtils {
    
    /**
     * Add the <code>element</code> to the given working set if possible.
     * 
     * @param element
     *            the element to add
     * @param workingSet
     *            the working set to add the element to
     */
    public static void addToWorkingSet(IAdaptable element, IWorkingSet workingSet) {
        IAdaptable[] adaptedNewElements = workingSet.adaptElements(new IAdaptable[] { element });
        if (adaptedNewElements.length == 1) {
            IAdaptable[] elements = workingSet.getElements();
            IAdaptable[] newElements = new IAdaptable[elements.length + 1];
            System.arraycopy(elements, 0, newElements, 0, elements.length);
            newElements[newElements.length - 1] = adaptedNewElements[0];
            workingSet.setElements(newElements);
        }
    }
    
    /**
     * Filters the given working sets such that the following is true:
     * for each IWorkingSet s in result: s.getId() is element of workingSetIds
     * 
     * @param workingSets the array to filter
     * @param workingSetIds the acceptable working set ids
     * @return the filtered elements
     */
    public static IWorkingSet[] filter(IWorkingSet[] workingSets, String[] workingSetIds) {
        ArrayList<IWorkingSet> result= new ArrayList<IWorkingSet>();
        for (IWorkingSet workingSet : workingSets)
            if (hasAnyOfIds(workingSet, workingSetIds))
                result.add(workingSet);
        return result.toArray(new IWorkingSet[result.size()]);
    }
    
    private static boolean hasAnyOfIds(IWorkingSet set, String[] workingSetIds) {
        for (String workingSetId : workingSetIds)
            if (workingSetId.equals(set.getId()))
                return true;
        return false;
    }
    
    public static IWorkingSet[] getJavaWorkingSets() {
        return filter(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets(), WORKING_SET_IDS);
    }
    
    public static IWorkingSet lookupWorkingSet(String name) {
        IWorkingSetManager wsm = PlatformUI.getWorkbench().getWorkingSetManager();
        IWorkingSet ws = wsm.getWorkingSet(name);
        if (ws == null || !hasAnyOfIds(ws, WORKING_SET_IDS)) {
            ws = wsm.createWorkingSet(name, new IAdaptable[] {});
            ws.setId(WorkingSetUtils.JavaWorkingSetUpdater_ID);
            wsm.addWorkingSet(ws);
            addToPackageExplorer(ws);
        } 
        return ws;
    }

    @SuppressWarnings("restriction")
    private static void addToPackageExplorer(final IWorkingSet ws) {
        Runnable runnable = new Runnable() {
            public void run() {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                PackageExplorerPart part = (PackageExplorerPart) page.findView(JavaUI.ID_PACKAGES);
                WorkingSetModel model = part.getWorkingSetModel();
                IWorkingSet[] active = model.getActiveWorkingSets();
                IWorkingSet[] expanded = new IWorkingSet[active.length + 1];
                System.arraycopy(active, 0, expanded, 0, active.length);
                expanded[active.length] = ws;
                model.setActiveWorkingSets(expanded);
            }
        };
        Display.getDefault().asyncExec(runnable);
    }

    public static final String JavaWorkingSetUpdater_ID = "org.eclipse.jdt.ui.JavaWorkingSetPage";
    
    public static String[] WORKING_SET_IDS = new String[] { JavaWorkingSetUpdater_ID,
            "org.eclipse.ui.resourceWorkingSetPage" }; //$NON-NLS-1$
    
}
