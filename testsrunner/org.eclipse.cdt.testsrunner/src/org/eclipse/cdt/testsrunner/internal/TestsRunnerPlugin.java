/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal;

import java.net.URL;
import java.util.HashSet;

import org.eclipse.cdt.testsrunner.internal.launcher.TestsRunnersManager;
import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class TestsRunnerPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.cdt.testsrunner"; //$NON-NLS-1$
	private static final IPath ICONS_PATH= new Path("$nl$/icons"); //$NON-NLS-1$

	/**
	 * Launch UI plug-in instance
	 */
	private static TestsRunnerPlugin plugin;
	
	private TestsRunnersManager testsRunnersManager = new TestsRunnersManager();
	private TestingSessionsManager testingSessionsManager = new TestingSessionsManager(testsRunnersManager);

	/**
	 * Constructor for LaunchUIPlugin.
	 * 
	 * @param descriptor
	 */
	public TestsRunnerPlugin() {
		super();
		plugin = this;
	}
	
	/**
	 * Returns the Java Debug UI plug-in instance
	 * 
	 * @return the Java Debug UI plug-in instance
	 */
	public static TestsRunnerPlugin getDefault() {
		return plugin;
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 *            the error message to log
	 */
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		
		setDefaultLaunchDelegates();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	public TestingSessionsManager getTestingSessionsManager() {
		return testingSessionsManager;
	}

	public TestsRunnersManager getTestsRunnersManager() {
		return testsRunnersManager;
	}

	static public ImageDescriptor getImageDescriptor(String relativePath) {
		return getDefault().getImageDescriptorImpl(relativePath);
	}

	private ImageDescriptor getImageDescriptorImpl(String relativePath) {
		IPath path= ICONS_PATH.append(relativePath);
		return createImageDescriptor(getDefault().getBundle(), path, true);
	}

	public static Image createAutoImage(String path) {
		return getDefault().createAutoImageImpl(path);
	}

	private Image createAutoImageImpl(String path) {
		Image image = getImageRegistry().get(path);
		if (image == null) {
			image = getImageDescriptor(path).createImage();
			getImageRegistry().put(path, image);
		}
		return image;
	}

	/**
	 * Creates an image descriptor for the given path in a bundle. The path can
	 * contain variables like $NL$. If no image could be found,
	 * <code>useMissingImageDescriptor</code> decides if either the 'missing
	 * image descriptor' is returned or <code>null</code>.
	 *
	 * @param bundle a bundle
	 * @param path path in the bundle
	 * @param useMissingImageDescriptor if <code>true</code>, returns the shared image descriptor
	 *            for a missing image. Otherwise, returns <code>null</code> if the image could not
	 *            be found
	 * @return an {@link ImageDescriptor}, or <code>null</code> iff there's
	 *         no image at the given location and
	 *         <code>useMissingImageDescriptor</code> is <code>true</code>
	 */
	private ImageDescriptor createImageDescriptor(Bundle bundle, IPath path, boolean useMissingImageDescriptor) {
		URL url= FileLocator.find(bundle, path, null);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		if (useMissingImageDescriptor) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
		return null;
	}
	
	private void setDefaultLaunchDelegate(ILaunchConfigurationType cfgType, String delegateId, String mode) {
		HashSet<String> debugSet = new HashSet<String>();
		debugSet.add(mode);
		try {
			if (cfgType.getPreferredDelegate(debugSet) == null) {
				ILaunchDelegate[] delegates = cfgType.getDelegates(debugSet);
				for (ILaunchDelegate delegate : delegates) {
					if (delegateId.equals(delegate.getId())) {
						cfgType.setPreferredDelegate(debugSet, delegate);
						break;
					}
				}
			}
		} catch (CoreException e) {
		}
	}
	
	private void setDefaultLaunchDelegates() {
		ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType configurationType = launchMgr.getLaunchConfigurationType("org.eclipse.cdt.testsrunner.launch.CTestsRunner");
		
		setDefaultLaunchDelegate(configurationType, "org.eclipse.cdt.testsrunner.launch.dsf.runTests", ILaunchManager.DEBUG_MODE);
		setDefaultLaunchDelegate(configurationType, "org.eclipse.cdt.testsrunner.launch.runTests", ILaunchManager.RUN_MODE);
	}
	
}
