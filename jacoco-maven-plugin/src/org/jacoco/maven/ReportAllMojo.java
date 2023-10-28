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

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mojo(name = "report-all", threadSafe = true, inheritByDefault = false, aggregator = true)
public class ReportAllMojo extends AbstractMultiModulesReportMojo {// ReportAggregateMojo

	protected boolean isIncludeCurrentProject() {
		return false;
	}

	@Override
	String getDefaultDataFileIncludeExpression() {
		return "target/jacoco.exec";
	}

	@Override
	List<MavenProject> getExecutionDataProjects() {
		return eligibleProjects(getExecutionDataProjectPredicate());
	}

	@Override
	List<MavenProject> getReportProjects() {
		return eligibleProjects(getReportProjectPredicate());
	}

	private Predicate<MavenProject> getExecutionDataProjectPredicate() {
		return mp -> mp.equals(project);
	}

	private Predicate<MavenProject> getReportProjectPredicate() {
		return mp -> mp.getCompileSourceRoots().stream().map(File::new)
				.allMatch(File::exists);
	}

	List<MavenProject> eligibleProjects(Predicate<MavenProject> filter) {
		return project.getCollectedProjects().stream().filter(filter)
				.collect(Collectors.toList());
	}

	public String getName(final Locale locale) {
		return "JaCoCo All";
	}

}
