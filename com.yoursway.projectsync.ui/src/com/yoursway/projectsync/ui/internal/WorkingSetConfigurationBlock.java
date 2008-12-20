package com.yoursway.projectsync.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import com.ibm.icu.text.Collator;


public class WorkingSetConfigurationBlock {
	
	//Some static methods for convenience
	
	/**
	 * Retrieves a working set from the given <code>selection</code>
	 * or <b>null</b> if no working set could be retrieved.  
	 * 
	 * @param selection the selection to retrieve the working set from
	 * @return the selected working set or <b>null</b>
	 */
	public static IWorkingSet[] getSelectedWorkingSet(IStructuredSelection selection) {
		if (!(selection instanceof ITreeSelection))
			return null;
		
		ITreeSelection treeSelection= (ITreeSelection)selection;
		if (treeSelection.isEmpty())
			return null;
		
		List<?> elements= treeSelection.toList();
		if (elements.size() == 1) {
			Object element= elements.get(0);
			TreePath[] paths= treeSelection.getPathsFor(element);
			if (paths.length != 1)
				return null;
		
			TreePath path= paths[0];
			if (path.getSegmentCount() == 0)
				return null;
		
			Object candidate= path.getSegment(0);
			if (!(candidate instanceof IWorkingSet))
				return null;
				
			return new IWorkingSet[] {(IWorkingSet)candidate};
		} else {
			ArrayList<Object> result= new ArrayList<Object>();
			for (Iterator<?> iterator= elements.iterator(); iterator.hasNext();) {
				Object element= iterator.next();
				if (element instanceof IWorkingSet) {
					result.add(element);
				}
			}
			return result.toArray(new IWorkingSet[result.size()]);
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
		
		for (int i= 0; i < workingSets.length; i++) {
			if (accept(workingSets[i], workingSetIds))
				result.add(workingSets[i]);
		}
		
		return result.toArray(new IWorkingSet[result.size()]);
	}
	
	private static boolean accept(IWorkingSet set, String[] workingSetIDs) {
		for (int i= 0; i < workingSetIDs.length; i++) {
			if (workingSetIDs[i].equals(set.getId()))
				return true;
		}
		
		return false;
	}
	
	private static final String WORKINGSET_SELECTION_HISTORY= "workingset_selection_history"; //$NON-NLS-1$
	private static final int MAX_HISTORY_SIZE= 5;
	
	private Label fLabel;
	private Combo fWorkingSetCombo;
	private Button fConfigure;
	private IWorkingSet[] fSelectedWorkingSets;
	private String fMessage;
	private Button fEnableButton;
	private ArrayList<String> fSelectionHistory;
	private final IDialogSettings fSettings;
	private final String fEnableButtonText;
	private final String[] fWorkingSetIds;

	/**
	 * @param workingSetIds working set ids from which the user can choose
	 * @param enableButtonText the text shown for the enable button
	 * @param settings to store/load the selection history
	 */
	public WorkingSetConfigurationBlock(String[] workingSetIds, String enableButtonText, IDialogSettings settings) {
		Assert.isNotNull(workingSetIds);
		Assert.isNotNull(enableButtonText);
		Assert.isNotNull(settings);
		
		fWorkingSetIds= workingSetIds;
		fEnableButtonText= enableButtonText;
		fSelectedWorkingSets= new IWorkingSet[0];
		fSettings= settings;
		fSelectionHistory= loadSelectionHistory(settings, workingSetIds);
	}

	/**
	 * @param message the message to show to the user in the working set selection dialog
	 */
	public void setDialogMessage(String message) {
		fMessage= message;
	}
	
	/**
	 * @param selection the selection to present in the UI or <b>null</b>
	 */
	public void setSelection(IWorkingSet[] selection) {
		if (selection == null)
			selection= new IWorkingSet[0];
		
		fSelectedWorkingSets= selection;
		if (fWorkingSetCombo != null)
			updateSelectedWorkingSets();
	}
	
	/**
	 * @return the selected working sets
	 */
	public IWorkingSet[] getSelectedWorkingSets() {
		if (fEnableButton.getSelection()) {
			return fSelectedWorkingSets;
		} else {
			return new IWorkingSet[0];
		}
	}

	/**
	 * Add this block to the <code>parent</parent>
	 * 
	 * @param parent the parent to add the block to
	 */
	public void createContent(final Composite parent) {
		int numColumn= 3;
		
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		composite.setLayout(new GridLayout(numColumn, false));
		
		fEnableButton= new Button(composite, SWT.CHECK);
		fEnableButton.setText(fEnableButtonText);
		GridData enableData= new GridData(SWT.FILL, SWT.CENTER, true, false);
		enableData.horizontalSpan= numColumn;
		fEnableButton.setLayoutData(enableData);
		fEnableButton.setSelection(fSelectedWorkingSets.length > 0);
					
		fLabel= new Label(composite, SWT.NONE);
		fLabel.setText("W&orking sets:");
		
		fWorkingSetCombo= new Combo(composite, SWT.READ_ONLY | SWT.BORDER);
		GridData textData= new GridData(SWT.FILL, SWT.CENTER, true, false);
		textData.horizontalSpan= numColumn - 2;
		textData.horizontalIndent= 0;
		fWorkingSetCombo.setLayoutData(textData);
		
		fConfigure= new Button(composite, SWT.PUSH);
		fConfigure.setText("S&elect...");
		GridData configureData= new GridData(SWT.LEFT, SWT.CENTER, false, false);
		configureData.widthHint= getButtonWidthHint(fConfigure);
		fConfigure.setLayoutData(configureData);
		fConfigure.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				SimpleWorkingSetSelectionDialog dialog= new SimpleWorkingSetSelectionDialog(parent.getShell(), fWorkingSetIds, fSelectedWorkingSets);
				if (fMessage != null)
					dialog.setMessage(fMessage);

				if (dialog.open() == Window.OK) {
					IWorkingSet[] result= dialog.getSelection();
					if (result != null && result.length > 0) {
						fSelectedWorkingSets= result;
						PlatformUI.getWorkbench().getWorkingSetManager().addRecentWorkingSet(result[0]);
					} else {
						fSelectedWorkingSets= new IWorkingSet[0];
					}
					updateWorkingSetSelection();
				}
			}
		});
		
		fEnableButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEnableState(fEnableButton.getSelection());
			}
		});
		updateEnableState(fEnableButton.getSelection());
		
		fWorkingSetCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateSelectedWorkingSets();
			}
		});
		
		fWorkingSetCombo.setItems(getHistoryEntries());
		if (fSelectedWorkingSets.length == 0 && fSelectionHistory.size() > 0) {
			fWorkingSetCombo.select(historyIndex(fSelectionHistory.get(0)));
			updateSelectedWorkingSets();
		} else {
			updateWorkingSetSelection();
		}
	}
	
	private void updateEnableState(boolean enabled) {
		fLabel.setEnabled(enabled);
		fWorkingSetCombo.setEnabled(enabled && (fSelectedWorkingSets.length > 0 || getHistoryEntries().length > 0));
		fConfigure.setEnabled(enabled);
	}
	
	private void updateWorkingSetSelection() {
		if (fSelectedWorkingSets.length > 0) {
			fWorkingSetCombo.setEnabled(true);
			StringBuffer buf= new StringBuffer();

			buf.append(fSelectedWorkingSets[0].getLabel());
			for (int i= 1; i < fSelectedWorkingSets.length; i++) {
				IWorkingSet ws= fSelectedWorkingSets[i];
				buf.append(',').append(' ');
				buf.append(ws.getLabel());
			}

			String currentSelection= buf.toString();
			int index= historyIndex(currentSelection);
			historyInsert(currentSelection);
			if (index >= 0) {
				fWorkingSetCombo.select(index);
			} else {
				fWorkingSetCombo.setItems(getHistoryEntries());
				fWorkingSetCombo.select(historyIndex(currentSelection));
			} 
		}else {
			fEnableButton.setSelection(false);
			updateEnableState(false);
		}
	}

	private String[] getHistoryEntries() {
		String[] history= fSelectionHistory.toArray(new String[fSelectionHistory.size()]);
		Arrays.sort(history, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return Collator.getInstance().compare(o1, o2);
			}
		});
		return history;
	}

	private void historyInsert(String entry) {
		fSelectionHistory.remove(entry);
		fSelectionHistory.add(0, entry);
		storeSelectionHistory(fSettings);
	}

	private int historyIndex(String entry) {
		for (int i= 0; i < fWorkingSetCombo.getItemCount(); i++) {
			if (fWorkingSetCombo.getItem(i).equals(entry))
				return i;
		}
		
		return -1;
	}
	
	private void updateSelectedWorkingSets() {
		String item= fWorkingSetCombo.getItem(fWorkingSetCombo.getSelectionIndex());
		String[] workingSetNames= item.split(", "); //$NON-NLS-1$
		
		IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
		fSelectedWorkingSets= new IWorkingSet[workingSetNames.length];
		for (int i= 0; i < workingSetNames.length; i++) {					
			IWorkingSet set= workingSetManager.getWorkingSet(workingSetNames[i]);
			Assert.isNotNull(set);
			fSelectedWorkingSets[i]= set;
		}
	}
	
	private void storeSelectionHistory(IDialogSettings settings) {
		String[] history;
		if (fSelectionHistory.size() > MAX_HISTORY_SIZE) {
			List<String> subList= fSelectionHistory.subList(0, MAX_HISTORY_SIZE);
			history= subList.toArray(new String[subList.size()]);
		} else {
			history= fSelectionHistory.toArray(new String[fSelectionHistory.size()]);
		}
		settings.put(WORKINGSET_SELECTION_HISTORY, history);
	}
	
	private ArrayList<String> loadSelectionHistory(IDialogSettings settings, String[] workingSetIds) {
		String[] strings= settings.getArray(WORKINGSET_SELECTION_HISTORY);
		if (strings == null || strings.length == 0)
			return new ArrayList<String>();
		
		ArrayList<String> result= new ArrayList<String>();
		
		HashSet<String> workingSetIdsSet= new HashSet<String>(Arrays.asList(workingSetIds));
		
		IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
		for (int i= 0; i < strings.length; i++) {
			String[] workingSetNames= strings[i].split(", "); //$NON-NLS-1$
			boolean valid= true;
			for (int j= 0; j < workingSetNames.length && valid; j++) {				
				IWorkingSet workingSet= workingSetManager.getWorkingSet(workingSetNames[j]);
				if (workingSet == null) {
					valid= false;
				} else {
					if (!workingSetIdsSet.contains(workingSet.getId()))
						valid= false;
				}
			}
			if (valid) {
				result.add(strings[i]);
			}
		}
		
		return result;
	}

	private static int getButtonWidthHint(Button button) {
		button.setFont(JFaceResources.getDialogFont());
		
		GC gc = new GC(button);
		gc.setFont(button.getFont());
		FontMetrics fontMetrics= gc.getFontMetrics();
		gc.dispose();
		
		int widthHint= Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}
}