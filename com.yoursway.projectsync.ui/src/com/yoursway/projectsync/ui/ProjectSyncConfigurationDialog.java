package com.yoursway.projectsync.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

import com.yoursway.projectsync.core.MonitoredFolder;
import com.yoursway.projectsync.core.ProjectSync;

public class ProjectSyncConfigurationDialog extends Dialog {

    private Composite master;
    private TableViewer tableViewer;
    private Table table;
    private DetailComposite detail;
    private Composite buttonBar;
    private Button removeButton;
    private MonitoredFolder folder;
    private Label spacer;
    private Button clearButton;
    private Group detailGroup;
    
    public ProjectSyncConfigurationDialog(Shell parentShell) {
        super(parentShell);
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("ProjectSync configuration");
    }
    
    @Override
    protected Point getInitialSize() {
        return new Point(500, 400);
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Close", true);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        master = new Composite(parent, SWT.NONE);
        master.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        master.setLayout(GridLayoutFactory.swtDefaults().create());
        
        Label description = new Label(master, SWT.WRAP);
        description.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        description
                .setText("ProjectSync will scan the list of folders set up here. For every subfoler, a working set " +
                		"will be created. The contents of every subfolder will be scanned for Eclipse projects, " +
                		"and all the found projects will be imported and added to the corresponding working set.");
        
        Label tableCaption = new Label(master, SWT.NONE);
        tableCaption.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        tableCaption.setText("Working sets are defined inside these folders:");
        
        table = new Table(master, SWT.V_SCROLL | SWT.BORDER);
        table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        
        buttonBar = new Composite(master, SWT.NONE);
        buttonBar.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        buttonBar.setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());
        
        spacer = new Label(buttonBar, SWT.NONE);
        spacer.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        
        clearButton = new Button(buttonBar, SWT.NONE);
        clearButton.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).create());
        clearButton.setText("Remove All");
        
        removeButton = new Button(buttonBar, SWT.NONE);
        removeButton.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).create());
        removeButton.setText("Remove");
        
        detailGroup = new Group(master, SWT.NONE);
        detailGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        detailGroup.setLayout(GridLayoutFactory.fillDefaults().create());
        
        detail = new DetailComposite(detailGroup, SWT.NONE);
        detail.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        
        tableViewer = new TableViewer(table);
        tableViewer.setContentProvider(new IStructuredContentProvider() {
            
            public Object[] getElements(Object inputElement) {
                List<Object> result = new ArrayList<Object>();
                result.add(new Stub());
                Collection<MonitoredFolder> folders = ProjectSync.getFolders();
                for (MonitoredFolder folder : folders)
                    if (folder != null)
                        result.add(folder);
                return result.toArray();
            }
            
            public void dispose() {
            }
            
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
            
        });
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                Object first = selection == null ? null : selection.getFirstElement();
                setSelectedFolder(first instanceof Stub ? null : (MonitoredFolder) first);
            }
            
        });
        tableViewer.setInput(this);
        
        removeButton.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                doUpdate();
            }
            
        });
        
        clearButton.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                ProjectSync.clearFolders();
                tableViewer.refresh();
                tableViewer.setSelection(new StructuredSelection());
            }
            
        });
        
        setSelectedFolder(null);
        
        return master;
    }
    
    protected void setSelectedFolder(MonitoredFolder folder) {
        this.folder = folder;
        if (folder == null)
            detail.setNew();
        else
            detail.set(folder);
    }
    
    public void init(IWorkbench workbench) {
    }
    
    private void doUpdate() {
        //        Job update = new Job("Updating monitored folder") {
        //
        //            @Override
        //            protected IStatus run(IProgressMonitor monitor) {
        //                // TODO Auto-generated method stub
        //                return null;
        //            }
        //            
        //        };
        MonitoredFolder newFolder = detail.get();
        if (newFolder == null)
            return;
        if (folder != null)
            ProjectSync.removeFolder(folder);
        ProjectSync.addFolder(newFolder);
        folder = newFolder;
        tableViewer.refresh();
        tableViewer.setSelection(new StructuredSelection(newFolder), true);
    }
    
    class DetailComposite extends Composite {
        
        private Text path;
        private Button browse;
//        private Text workingSet;
        private boolean isSetting;
        
        public DetailComposite(Composite parent, int style) {
            super(parent, style);
            setLayout(GridLayoutFactory.swtDefaults().numColumns(3).create());
            
            Label pathLabel = new Label(this, SWT.NONE);
            pathLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false,
                    false).create());
            pathLabel.setText("Folder:");
            
            path = new Text(this, SWT.BORDER);
            path.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false)
                    .create());
            path.addModifyListener(new ModifyListener() {
                
                public void modifyText(ModifyEvent e) {
                    changed();
                }
                
            });
            
            browse = new Button(this, SWT.NONE);
            browse.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).create());
            browse.setText("Browse");
            browse.addSelectionListener(new SelectionAdapter() {
                
                @Override
                public void widgetSelected(SelectionEvent e) {
                    DirectoryDialog dialog = new DirectoryDialog(getShell());
                    String chosen = dialog.open();
                    if (chosen != null && chosen.length() > 0)
                        path.setText(chosen);
                }
                
            });
            
//            Label workingSetLabel = new Label(this, SWT.NONE);
//            workingSetLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(
//                    false, false).create());
//            workingSetLabel.setText("Add to working set:");
//            
//            workingSet = new Text(this, SWT.BORDER);
//            workingSet.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true,
//                    false).create());
//            workingSet.addModifyListener(new ModifyListener() {
//                
//                public void modifyText(ModifyEvent e) {
//                    changed();
//                }
//                
//            });
            
            //            String[] workingSetIds= new String[] {Activator.JavaWorkingSetUpdater_ID, "org.eclipse.ui.resourceWorkingSetPage"}; //$NON-NLS-1$
            //            WorkingSetConfigurationBlock block = new WorkingSetConfigurationBlock(workingSetIds, "Add to working set", Activator.getDialogSettings("workingSetsBlock"));
            //            block.createContent(this);
        }
        
        protected void changed() {
            if (isSetting)
                return;
            isSetting = true;
            doUpdate();
            isSetting = false;
        }
        
        public void setNew() {
            if (isSetting)
                return;
            isSetting = true;
            path.setText("");
//            workingSet.setText("");
            isSetting = false;
        }
        
        public void set(MonitoredFolder folder) {
            if (isSetting)
                return;
            isSetting = true;
            path.setText(PathUtils.toString(folder.location()));
//            String ws = folder.workingSet();
//            workingSet.setText(ws == null ? "" : ws);
            isSetting = false;
        }
        
        public MonitoredFolder get() {
            File file = PathUtils.toFile(path.getText());
            if (!file.exists())
                return null;
//            String ws = workingSet.getText();
//            if ((ws = ws.trim()).length() == 0 || "(none)".equalsIgnoreCase(ws))
//                ws = null;
            return new MonitoredFolder(file);
        }
        
    }
    
    static class Stub {
        
        @Override
        public String toString() {
            return "Choose to add a new folder";
        }
        
    }
    
}
