/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import org.eclipse.cdt.codan.core.CodanPreferencesLoader;
import org.eclipse.cdt.codan.core.PreferenceConstants;
import org.eclipse.cdt.codan.core.model.CodanSeverity;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemCategory;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class ProblemsTreeEditor extends CheckedTreeEditor {
	private CodanPreferencesLoader codanPreferencesLoader = new CodanPreferencesLoader();

	public ProblemsTreeEditor() {
		super();
	}

	class ProblemsCheckStateProvider implements ICheckStateProvider {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ICheckStateProvider#isChecked(java.lang
		 * .Object)
		 */
		public boolean isChecked(Object element) {
			if (element instanceof IProblem) {
				IProblem p = (IProblem) element;
				return p.isEnabled();
			}
			if (element instanceof IProblemCategory) {
				// checked if at least one is checked (buy grayed)
				IProblemCategory p = (IProblemCategory) element;
				Object[] children = p.getChildren();
				for (int i = 0; i < children.length; i++) {
					Object object = children[i];
					if (isChecked(object)) {
						return true;
					}
				}
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ICheckStateProvider#isGrayed(java.lang.
		 * Object)
		 */
		public boolean isGrayed(Object element) {
			if (element instanceof IProblem) {
				return false;
			}
			if (element instanceof IProblemCategory) {
				// checked if at least one is checked (buy grayed)
				IProblemCategory p = (IProblemCategory) element;
				Object[] children = p.getChildren();
				boolean all_checked = true;
				boolean all_unchecked = true;
				for (int i = 0; i < children.length; i++) {
					Object object = children[i];
					if (isChecked(object)) {
						all_unchecked = false;
					} else {
						all_checked = false;
					}
				}
				if (all_checked || all_unchecked)
					return false;
				return true;
			}
			return false;
		}
	}

	class ProblemsContentProvider implements IContentProvider,
			ITreeContentProvider {
		public void dispose() {
			// TODO Auto-generated method stub
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Object[])
				return (Object[]) parentElement;
			if (parentElement instanceof IProblemCategory) {
				return ((IProblemCategory) parentElement).getChildren();
			}
			if (parentElement instanceof IProblemProfile) {
				return ((IProblemProfile) parentElement).getRoot()
						.getChildren();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
	}

	public void checkStateChanged(CheckStateChangedEvent event) {
		Object element = event.getElement();
		if (element instanceof IProblem) {
			((IProblem) element).setEnabled(event.getChecked());
		}
	}

	class ProblemsLabelProvider extends BaseLabelProvider implements
			IBaseLabelProvider, ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IProblem) {
				IProblem p = (IProblem) element;
				if (columnIndex == 0)
					return p.getName();
				if (columnIndex == 1)
					return p.getSeverity().toString();
			}
			if (element instanceof IProblemCategory) {
				IProblemCategory p = (IProblemCategory) element;
				if (columnIndex == 0)
					return p.getName();
			}
			return null;
		}
	}

	public ProblemsTreeEditor(Composite parent, IProblemProfile profile) {
		super(PreferenceConstants.P_PROBLEMS, "Problems", parent);
		setEmptySelectionAllowed(true);
		getTreeViewer().getTree().setHeaderVisible(true);
		// getTreeViewer().getTree().
		getTreeViewer().setContentProvider(new ProblemsContentProvider());
		getTreeViewer().setCheckStateProvider(new ProblemsCheckStateProvider());
		// column Name
		TreeViewerColumn column1 = new TreeViewerColumn(getTreeViewer(),
				SWT.NONE);
		column1.getColumn().setWidth(300);
		column1.getColumn().setText("Name");
		column1.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof IProblem) {
					IProblem p = (IProblem) element;
					return p.getName();
				}
				if (element instanceof IProblemCategory) {
					IProblemCategory p = (IProblemCategory) element;
					return p.getName();
				}
				return null;
			}
		});
		// column Severity
		TreeViewerColumn column2 = new TreeViewerColumn(getTreeViewer(),
				SWT.NONE);
		column2.getColumn().setWidth(100);
		column2.getColumn().setText("Severity");
		column2.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof IProblem) {
					IProblem p = (IProblem) element;
					return p.getSeverity().toString();
				}
				return null;
			}
		});
		column2.setEditingSupport(new EditingSupport(getTreeViewer()) {
			protected boolean canEdit(Object element) {
				return element instanceof IProblem;
			}

			protected CellEditor getCellEditor(Object element) {
				return new ComboBoxCellEditor(getTreeViewer().getTree(),
						CodanSeverity.stringValues());
			}

			protected Object getValue(Object element) {
				return ((IProblem) element).getSeverity().intValue();
			}

			protected void setValue(Object element, Object value) {
				int index = ((Integer) value).intValue();
				CodanSeverity val = CodanSeverity.values()[index];
				((IProblem) element).setSeverity(val);
				getTreeViewer().update(element, null);
			}
		});
		getTreeViewer().setAutoExpandLevel(2);
		codanPreferencesLoader.setInput(profile);
		getViewer().setInput(profile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.internal.ui.preferences.CheckedTreeEditor#doLoad()
	 */
	@Override
	protected void doLoad() {
		if (getTreeControl() != null) {
			IProblem[] probs = codanPreferencesLoader.getProblems();
			for (int i = 0; i < probs.length; i++) {
				String id = probs[i].getId();
				String s = getPreferenceStore().getString(id);
				codanPreferencesLoader.setProperty(id, s);
			}
			getViewer().setInput(codanPreferencesLoader.getInput());
		}
	}

	@Override
	protected void doLoadDefault() {
		if (getTreeControl() != null) {
			IProblem[] probs = codanPreferencesLoader.getProblems();
			for (int i = 0; i < probs.length; i++) {
				String id = probs[i].getId();
				String s = getPreferenceStore().getDefaultString(id);
				codanPreferencesLoader.setProperty(id, s);
			}
			getViewer().setInput(codanPreferencesLoader.getInput());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.internal.ui.preferences.CheckedTreeEditor#doStore()
	 */
	@Override
	protected void doStore() {
		codanPreferencesLoader.setInput(getViewer().getInput());
		IProblem[] probs = codanPreferencesLoader.getProblems();
		for (int i = 0; i < probs.length; i++) {
			String id = probs[i].getId();
			String s = codanPreferencesLoader.getProperty(id);
			getPreferenceStore().setValue(id, s);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.internal.ui.preferences.CheckedTreeEditor#
	 * modelFromString(java.lang.String)
	 */
	@Override
	protected Object modelFromString(String s) {
		return codanPreferencesLoader.getInput();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.internal.ui.preferences.CheckedTreeEditor#modelToString
	 * (java.lang.Object)
	 */
	@Override
	protected String modelToString(Object model) {
		return "";
	}
}
