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
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.jacoco.report.IReportGroupVisitor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mojo(name = "report-all", threadSafe = true, inheritByDefault = false, aggregator = true)
public class ReportAllMojo extends ReportAggregateMojo {

	@Override
	void loadExecutionData(ReportSupport support) throws IOException {

		// https://issues.apache.org/jira/browse/MNG-5440
		if (dataFileIncludes == null) {
			dataFileIncludes = Arrays.asList("target/jacoco.exec");
		}

		final FileFilter filter = new FileFilter(dataFileIncludes,
				dataFileExcludes);
		loadExecutionData(support, filter, project.getBasedir());
	}

	@Override
	void createReport(final IReportGroupVisitor visitor,
			final ReportSupport support) throws IOException {
		final IReportGroupVisitor group = visitor.visitGroup(title);
		Predicate<MavenProject> projectFilter = mp -> mp.getCompileSourceRoots()
				.stream().map(File::new).allMatch(File::exists);
		for (final MavenProject subProject : eligibleProjects(projectFilter)) {
			processProject(support, group, subProject);
		}
	}

	List<MavenProject> eligibleProjects(Predicate<MavenProject> filter) {
		return project.getCollectedProjects().stream().filter(filter)
				.collect(Collectors.toList());
	}

}
