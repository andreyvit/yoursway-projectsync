package com.yoursway.projectsync.ui.internal;

import com.ibm.icu.text.Collator;
import com.yoursway.projectsync.core.WorkingSetUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetNewWizard;
import org.eclipse.ui.dialogs.SelectionDialog;

public class SimpleWorkingSetSelectionDialog extends SelectionDialog {
	
	private static class WorkingSetLabelProvider extends LabelProvider {
		
		private Map<ImageDescriptor, Image> fIcons;
		
		public WorkingSetLabelProvider() {
			fIcons= new Hashtable<ImageDescriptor, Image>();
		}
		
		public void dispose() {
			Iterator<Image> iterator= fIcons.values().iterator();
			while (iterator.hasNext()) {
				Image icon= iterator.next();
				icon.dispose();
			}
			super.dispose();
		}
		
		public Image getImage(Object object) {
			Assert.isTrue(object instanceof IWorkingSet);
			IWorkingSet workingSet= (IWorkingSet)object;
			ImageDescriptor imageDescriptor= workingSet.getImageDescriptor();
			if (imageDescriptor == null)
				return null;
			
			Image icon= fIcons.get(imageDescriptor);
			if (icon == null) {
				icon= imageDescriptor.createImage();
				fIcons.put(imageDescriptor, icon);
			}
			
			return icon;
		}
		
		public String getText(Object object) {
			Assert.isTrue(object instanceof IWorkingSet);
			IWorkingSet workingSet= (IWorkingSet)object;
			return workingSet.getName();
		}
		
	}
	
	private class Filter extends ViewerFilter {
		
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return isCompatible((IWorkingSet)element);
		}
				
		private boolean isCompatible(IWorkingSet set) {
			if (set.isAggregateWorkingSet() || !set.isSelfUpdating())
				return false;
			
			if (!set.isVisible())
				return false;
			
			if (!set.isEditable())
				return false;
			
			return true;
		}
		
	}

    private final IWorkingSet[] fWorkingSets;
	private final IWorkingSet[] fInitialSelection;
	private final ArrayList<IWorkingSet> fCreatedWorkingSets;
	
	private CheckboxTableViewer fTableViewer;
	private IWorkingSet[] fCheckedElements;
	
	private Button fSelectAll;
	private Button fDeselectAll;
	private Button fNewWorkingSet;

	public SimpleWorkingSetSelectionDialog(Shell shell, String[] workingSetIds, IWorkingSet[] initialSelection) {
		super(shell);
		
		setTitle("Working Set Selection");
		setHelpAvailable(false);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		fWorkingSets= WorkingSetConfigurationBlock.filter(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets(), workingSetIds);
		fInitialSelection= initialSelection;
		fCheckedElements= fInitialSelection;
		fCreatedWorkingSets= new ArrayList<IWorkingSet>();
	}
	
	protected final Control createDialogArea(Composite parent) {
		Composite composite= (Composite)super.createDialogArea(parent);
		composite.setFont(parent.getFont());

		createMessageArea(composite);
		Composite inner= new Composite(composite, SWT.NONE);
		inner.setFont(composite.getFont());
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		inner.setLayout(layout);
		
		Composite tableComposite= new Composite(inner, SWT.NONE);
		tableComposite.setFont(composite.getFont());
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		tableComposite.setLayout(layout);
		
		fTableViewer= createTableViewer(tableComposite);
		createRightButtonBar(inner);
		
		createBottomButtonBar(composite);
		
		return composite;
	}

	public IWorkingSet[] getSelection() {
		return fCheckedElements;
	}
	
	protected CheckboxTableViewer createTableViewer(Composite parent) {
		CheckboxTableViewer result= CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.MULTI);
		result.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				checkedStateChanged();
			}
		});
		GridData data= new GridData(GridData.FILL_BOTH);
		data.heightHint= convertHeightInCharsToPixels(20);
		data.widthHint= convertWidthInCharsToPixels(50);
		result.getTable().setLayoutData(data);
		result.getTable().setFont(parent.getFont());

		result.addFilter(createTableFilter());
		result.setLabelProvider(createTableLabelProvider());
		result.setSorter(createTableSorter());
		result.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object element) {
				return (Object[])element;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		
		result.setInput(fWorkingSets);
		result.setCheckedElements(fInitialSelection);
		
		return result;
	}

	protected ViewerSorter createTableSorter() {
		return new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				IWorkingSet w1= (IWorkingSet)e1;
				IWorkingSet w2= (IWorkingSet)e2;
				return Collator.getInstance().compare(w1.getLabel(), w2.getLabel());
			}
		};
	}

	protected LabelProvider createTableLabelProvider() {
		return new WorkingSetLabelProvider();
	}

	protected ViewerFilter createTableFilter() {
		return new Filter();
	}
	
	protected void createRightButtonBar(Composite parent) {
		Composite buttons= new Composite(parent, SWT.NONE);
		buttons.setFont(parent.getFont());
		buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		buttons.setLayout(layout);

		createButtonsForRightButtonBar(buttons);
	}

	protected void createButtonsForRightButtonBar(Composite bar) {
		fSelectAll= new Button(bar, SWT.PUSH);
		fSelectAll.setText("Select &All"); 
		fSelectAll.setFont(bar.getFont());
		setButtonLayoutData(fSelectAll);
		fSelectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectAll();
			}
		});
		
		fDeselectAll= new Button(bar, SWT.PUSH);
		fDeselectAll.setText("&Deselect All"); 
		fDeselectAll.setFont(bar.getFont());
		setButtonLayoutData(fDeselectAll);
		fDeselectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deselectAll();
			}
		});
		
		new Label(bar, SWT.NONE);
		
		fNewWorkingSet= new Button(bar, SWT.PUSH);
		fNewWorkingSet.setText("&New..."); 
		fNewWorkingSet.setFont(bar.getFont());
		setButtonLayoutData(fNewWorkingSet);
		fNewWorkingSet.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IWorkingSet workingSet= newWorkingSet();
				if (workingSet != null) {
					
				}
			}
		});
	}
	
	protected void createBottomButtonBar(Composite parent) {
	}
	
	protected void checkedStateChanged() {
		List<Object> elements= Arrays.asList(fTableViewer.getCheckedElements());
		fCheckedElements= elements.toArray(new IWorkingSet[elements.size()]);
	}
	
	protected void selectAll() {
		fTableViewer.setAllChecked(true);
		checkedStateChanged();
	}
	
	protected void deselectAll() {
		fTableViewer.setAllChecked(false);
		checkedStateChanged();
	}
	
	protected IWorkingSet newWorkingSet() {
		IWorkingSetManager manager= PlatformUI.getWorkbench().getWorkingSetManager();
		
		//can only allow to create java working sets at the moment, see bug 186762
//		IWorkingSetNewWizard wizard= manager.createWorkingSetNewWizard(fWorkingSetIds);
//		if (wizard == null)
//			return;
		
		IWorkingSetNewWizard wizard= manager.createWorkingSetNewWizard(new String[] {WorkingSetUtils.JavaWorkingSetUpdater_ID});
		
		WizardDialog dialog= new WizardDialog(getShell(), wizard);
		dialog.create();
		if (dialog.open() == Window.OK) {
			IWorkingSet workingSet= wizard.getSelection();
			Filter filter= new Filter();
			if (filter.select(null, null, workingSet)) {
				addNewWorkingSet(workingSet);
				checkedStateChanged();
				manager.addWorkingSet(workingSet);
				fCreatedWorkingSets.add(workingSet);
				return workingSet;
			}
		}
		
		return null;
	}

	protected void addNewWorkingSet(IWorkingSet workingSet) {
		fTableViewer.add(workingSet);
		fTableViewer.setSelection(new StructuredSelection(workingSet), true);
		fTableViewer.setChecked(workingSet, true);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void cancelPressed() {
		IWorkingSetManager manager= PlatformUI.getWorkbench().getWorkingSetManager();
		for (int i= 0; i < fCreatedWorkingSets.size(); i++) {
			manager.removeWorkingSet(fCreatedWorkingSets.get(i));
		}
		
		super.cancelPressed();
	}
}