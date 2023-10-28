/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Oliver, Marc R. Hoffmann, Jan Wloka - initial API and implementation
 *    Sébastien Perpignane - refactoring to support ReportAllMojo
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jacoco.report.IReportGroupVisitor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractMultiModulesReportMojo
		extends AbstractReportMojo {

	/**
	 * A list of execution data files to include in the report from each
	 * project. May use wildcard characters (* and ?). When not specified all
	 * *.exec files from the target folder will be included.
	 */
	@Parameter
	List<String> dataFileIncludes;

	/**
	 * A list of execution data files to exclude from the report. May use
	 * wildcard characters (* and ?). When not specified nothing will be
	 * excluded.
	 */
	@Parameter
	List<String> dataFileExcludes;

	/**
	 * Output directory for the reports. Note that this parameter is only
	 * relevant if the goal is run from the command line or from the default
	 * build lifecycle. If the goal is run indirectly as part of a site
	 * generation, the output directory configured in the Maven Site Plugin is
	 * used instead.
	 */
	@Parameter(defaultValue = "${project.reporting.outputDirectory}/jacoco-aggregate")
	private File outputDirectory;

	@Override
	boolean canGenerateReportRegardingDataFiles() {
		return true;
	}

	@Override
	boolean canGenerateReportRegardingClassesDirectory() {
		return true;
	}

	abstract List<MavenProject> getExecutionDataProjects();

	abstract List<MavenProject> getReportProjects();

	abstract String getDefaultDataFileIncludeExpression();

	@Override
	void loadExecutionData(final ReportSupport support) throws IOException {
		// https://issues.apache.org/jira/browse/MNG-5440
		if (dataFileIncludes == null) {
			dataFileIncludes = Arrays
					.asList(getDefaultDataFileIncludeExpression());
		}

		final FileFilter filter = new FileFilter(dataFileIncludes,
				dataFileExcludes);
		loadExecutionData(support, filter, project.getBasedir());
		for (final MavenProject dependency : getExecutionDataProjects()) {
			getLog().warn("Trying to load execution data for project "
					+ dependency.getName());
			loadExecutionData(support, filter, dependency.getBasedir());
		}
	}

	void loadExecutionData(final ReportSupport support,
			final FileFilter filter, final File basedir) throws IOException {
		for (final File execFile : filter.getFiles(basedir)) {
			support.loadExecutionData(execFile);
		}
	}

	@Override
	File getOutputDirectory() {
		return outputDirectory;
	}

	abstract boolean isIncludeCurrentProject();

	@Override
	void createReport(final IReportGroupVisitor visitor,
			final ReportSupport support) throws IOException {
		final IReportGroupVisitor group = visitor.visitGroup(title);
		if (isIncludeCurrentProject()) {
			processProject(support, group, project);
		}
		for (final MavenProject dependency : getReportProjects()) {
			processProject(support, group, dependency);
		}
	}

	void processProject(final ReportSupport support,
			final IReportGroupVisitor group, final MavenProject project)
			throws IOException {
		support.processProject(group, project.getArtifactId(), project,
				getIncludes(), getExcludes(), sourceEncoding);
	}

	public File getReportOutputDirectory() {
		return outputDirectory;
	}

	public void setReportOutputDirectory(final File reportOutputDirectory) {
		if (reportOutputDirectory != null && !reportOutputDirectory
				.getAbsolutePath().endsWith("jacoco-aggregate")) {
			outputDirectory = new File(reportOutputDirectory,
					"jacoco-aggregate");
		} else {
			outputDirectory = reportOutputDirectory;
		}
	}

	public String getOutputName() {
		return "jacoco-aggregate/index";
	}

}
