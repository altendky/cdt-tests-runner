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
package org.eclipse.cdt.codan.core.model;

import java.util.Collection;

public interface IProblem extends IProblemElement {
	String getName();

	String getId();

	boolean isEnabled();

	CodanSeverity getSeverity();

	String getMessagePattern();

	void setSeverity(CodanSeverity sev);

	void setEnabled(boolean checked);

	void setMessagePattern(String message);

	public void setProperty(Object key, Object value);

	public Object getProperty(Object key);

	public Collection<Object> getPropertyKeys();
}
