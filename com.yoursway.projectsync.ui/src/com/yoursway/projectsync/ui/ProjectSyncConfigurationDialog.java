package com.yoursway.projectsync.ui;

import java.io.File;
import java.util.Collection;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

import com.yoursway.projectsync.core.MonitoredFolder;
import com.yoursway.projectsync.core.ProjectSync;
import com.yoursway.projectsync.ui.internal.Activator;

public class ProjectSyncConfigurationDialog extends Dialog {

	private static final String DESCRIPTION = "Keeps your projects organized under “[rpath]/<WorkingSet>/<Project>”:\n\n"
			+ "• imports any missing projects found under [hpath] (and assigns a working set);\n\n"
			+ "• moves all newly created projects into “[rpath]/<WorkingSet>/”.\n\n\n";
	private Composite master;
	private Label description;
	private Text path;
	private Button browse;

	public ProjectSyncConfigurationDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.MAX
				| SWT.RESIZE | getDefaultOrientation());
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		String version = Activator.getDefault().getBundle().getVersion()
				.toString();
		newShell.setText("YourSway ProjectSync " + version + " Settings");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 350);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Totally Awesome", true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		master = (Composite) super.createDialogArea(parent);

		detail = createDetailComposite(master, SWT.NONE);
		detail.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
				.create());

		description = new Label(master, SWT.WRAP);
		description.setLayoutData(GridDataFactory.fillDefaults().indent(0, 8)
				.grab(true, false).create());

		Link webLink = new Link(master, SWT.NONE);
		webLink.setLayoutData(GridDataFactory.fillDefaults().indent(0, 8).grab(
				true, false).create());
		webLink
				.setText("Please visit <A>www.yoursway.com/free/ProjectSync/</A> for support, updates and more info.");
		webLink.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Program.launch("http://www.yoursway.com/free/ProjectSync/");
			}
		});

		Collection<MonitoredFolder> collection = ProjectSync.getFolders();
		if (collection.isEmpty())
			setSelectedFolder(null);
		else
			setSelectedFolder(collection.iterator().next());
		updateDescription();

		return master;
	}

	private void updateDescription() {
		String path = PathUtils.toFile(getCurrentPath().trim()).getPath();
		if (path.endsWith("/") && path.length() > 1)
			path = path.substring(0, path.length() - 1);
		String hpath, rpath;
		if (path.length() == 0) {
			hpath = "the specified folder";
			rpath = "<SpecifiedFolder>";
		} else {
			hpath = "“" + path + "”";
			rpath = path;
		}
		description.setText(DESCRIPTION.replace("[hpath]", hpath).replace(
				"[rpath]", rpath));
	}

	protected void setSelectedFolder(MonitoredFolder folder) {
		if (folder == null)
			setNew();
		else
			set(folder);
	}

	public void init(IWorkbench workbench) {
	}

	private void doUpdate() {
		MonitoredFolder newFolder = get();
		if (newFolder == null)
			ProjectSync.clearFolders();
		else
			ProjectSync.setFolder(newFolder);
		updateDescription();
	}

	private boolean isSetting;
	private Composite detail;

	public Composite createDetailComposite(Composite parent, int style) {
		Composite result = new Composite(parent, style);
		result
				.setLayout(GridLayoutFactory.swtDefaults().numColumns(3)
						.create());

		Label pathLabel = new Label(result, SWT.NONE);
		pathLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL,
				SWT.CENTER).grab(false, false).create());
		pathLabel.setText("Working Sets Folder:");

		path = new Text(result, SWT.BORDER);
		path.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL,
				SWT.CENTER).grab(true, false).create());
		path.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				changed();
			}

		});

		browse = new Button(result, SWT.NONE);
		browse.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL,
				SWT.CENTER).create());
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
		return result;
	}

	public String getCurrentPath() {
		return path.getText();
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
		// workingSet.setText("");
		isSetting = false;
	}

	public void set(MonitoredFolder folder) {
		if (isSetting)
			return;
		isSetting = true;
		path.setText(PathUtils.toString(folder.location()));
		// String ws = folder.workingSet();
		// workingSet.setText(ws == null ? "" : ws);
		isSetting = false;
	}

	public MonitoredFolder get() {
		File file = PathUtils.toFile(path.getText());
		if (!file.exists())
			return null;
		// String ws = workingSet.getText();
		// if ((ws = ws.trim()).length() == 0 ||
		// "(none)".equalsIgnoreCase(ws))
		// ws = null;
		return new MonitoredFolder(file);
	}

}
