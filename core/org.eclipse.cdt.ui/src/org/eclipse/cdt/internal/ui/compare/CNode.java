/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.compare;

import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

/**
 * 
 */
class CNode extends DocumentRangeNode implements ITypedElement {

	public CNode(DocumentRangeNode parent, int type, String id, IDocument doc, int start, int length) {
		super(parent, type, id, doc, start, length);
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public CNode(DocumentRangeNode parent, int type, String id, int start, int length) {
		this(parent, type, id, parent.getDocument(), start, length);
	}

	public String getName() {
		return getId();
	}

	public String getType() {
		return "c2"; //$NON-NLS-1$
	}

	public Image getImage() {
		ImageDescriptor descriptor = CElementImageProvider.getImageDescriptor(getTypeCode());
		return CUIPlugin.getImageDescriptorRegistry().get(descriptor);
	}
}
