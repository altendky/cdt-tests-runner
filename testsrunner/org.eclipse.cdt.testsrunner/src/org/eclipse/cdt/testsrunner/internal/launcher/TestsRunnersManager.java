/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.testsrunner.internal.Activator;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.cdt.testsrunner.model.ITestsRunnerInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * TODO: Add descriptions
 * 
 */
public class TestsRunnersManager {
	
	private static final String TESTS_RUNNER_EXTENSION_POINT_ID = "org.eclipse.cdt.testsrunner.TestsRunner"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_FEATURES_ELEMENT = "features"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_FEATURE_MULTIPLE_TEST_FILTER_ATTRIBUTE = "multipleTestFilter"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_FEATURE_TESTING_TIME_MEASUREMENT_ATTRIBUTE = "testingTimeMeasurement"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_DESCRIPTION_ATTRIBUTE = "description"; //$NON-NLS-1$

	private TestsRunnerInfo[] testsRunners = null;

	
	public class TestsRunnerInfo implements ITestsRunnerInfo {
		private IConfigurationElement element;

		public TestsRunnerInfo(IConfigurationElement element) {
			this.element = element;
		}

		public String getName() {
			return element.getAttribute(TESTS_RUNNER_NAME_ATTRIBUTE);
		}

		public String getId() {
			return element.getAttribute(TESTS_RUNNER_ID_ATTRIBUTE);
		}

		public String getDescription() {
			String result = element.getAttribute(TESTS_RUNNER_DESCRIPTION_ATTRIBUTE);
			return result == null ? "" : result; //$NON-NLS-1$
		}

		public ITestsRunner instantiateTestsRunner() {
			try {
				Object object = element.createExecutableExtension(TESTS_RUNNER_CLASS_ATTRIBUTE);
				if (object instanceof ITestsRunner) {
					return (ITestsRunner)object;
				}
			} catch (CoreException e) {
				Activator.log(e);
			}
			return null;
		}
		
		private IConfigurationElement getFeatures() {
			IConfigurationElement[] featuresElements = element.getChildren(TESTS_RUNNER_FEATURES_ELEMENT);
			if (featuresElements.length == 1) {
				return featuresElements[0];
			}
			return null;
		}
		
		private boolean getBooleanFeatureValue(String featureName, boolean defaultValue) {
			IConfigurationElement features = getFeatures();
			if (features != null) {
				String attrValue = features.getAttribute(featureName);
				if (attrValue != null) {
					return Boolean.parseBoolean(attrValue);
				}
			}
			return defaultValue;
		}
		
		public boolean isAllowedMultipleTestFilter() {
			return getBooleanFeatureValue(TESTS_RUNNER_FEATURE_MULTIPLE_TEST_FILTER_ATTRIBUTE, false);
		}
		
		public boolean isAllowedTestingTimeMeasurement() {
			return getBooleanFeatureValue(TESTS_RUNNER_FEATURE_TESTING_TIME_MEASUREMENT_ATTRIBUTE, false);
		}
	}
	

	public TestsRunnersManager() {
	}

	public TestsRunnerInfo[] getTestsRunnersInfo() {
		if (testsRunners == null) {
			// Initialize tests runners info
			List<TestsRunnerInfo> testsRunnersList = new ArrayList<TestsRunnerInfo>();
			for (IConfigurationElement element : Platform.getExtensionRegistry().getConfigurationElementsFor(TESTS_RUNNER_EXTENSION_POINT_ID)) {
				testsRunnersList.add(new TestsRunnerInfo(element));
			}
			testsRunners = testsRunnersList.toArray(new TestsRunnerInfo[testsRunnersList.size()]);
		}
		return testsRunners;
	}
	
	public TestsRunnerInfo getTestsRunner(String testsRunnerId) {
		if (testsRunnerId!=null) {
			for (TestsRunnerInfo testsRunner : getTestsRunnersInfo()) {
				if (testsRunner.getId().equals(testsRunnerId)) {
					return testsRunner;
				}
			}
		}
		return null;
	}
	
}
