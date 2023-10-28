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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Mojo(name = "report-aggregate", threadSafe = true)
public class ReportAggregateMojo extends AbstractMultiModulesReportMojo {

	@Parameter(defaultValue = "false")
	private boolean includeCurrentProject;

	/**
	 * The projects in the reactor.
	 */
	@Parameter(property = "reactorProjects", readonly = true)
	private List<MavenProject> reactorProjects;

	protected boolean isIncludeCurrentProject() {
		return includeCurrentProject;
	}

	public String getName(final Locale locale) {
		return "JaCoCo Aggregate";
	}

	@Override
	String getDefaultDataFileIncludeExpression() {
		return "target/*.exec";
	}

	@Override
	List<MavenProject> getExecutionDataProjects() {
		return findDependencies(Artifact.SCOPE_COMPILE, Artifact.SCOPE_RUNTIME,
				Artifact.SCOPE_PROVIDED, Artifact.SCOPE_TEST);
	}

	@Override
	List<MavenProject> getReportProjects() {
		return findDependencies(Artifact.SCOPE_COMPILE, Artifact.SCOPE_RUNTIME,
				Artifact.SCOPE_PROVIDED);
	}

	private List<MavenProject> findDependencies(final String... scopes) {
		final List<MavenProject> result = new ArrayList<>();
		final List<String> scopeList = Arrays.asList(scopes);
		for (final Dependency dependency : project.getDependencies()) {
			if (scopeList.contains(dependency.getScope())) {
				final MavenProject project = findProjectFromReactor(dependency);
				if (project != null) {
					result.add(project);
				}
			}
		}
		return result;
	}

	/**
	 * Note that if dependency specified using version range and reactor
	 * contains multiple modules with same artifactId and groupId but of
	 * different versions, then first dependency which matches range will be
	 * selected. For example in case of range <code>[0,2]</code> if version 1 is
	 * before version 2 in reactor, then version 1 will be selected.
	 */
	private MavenProject findProjectFromReactor(final Dependency d) {
		final VersionRange depVersionAsRange;
		try {
			depVersionAsRange = VersionRange
					.createFromVersionSpec(d.getVersion());
		} catch (final InvalidVersionSpecificationException e) {
			throw new AssertionError(e);
		}

		for (final MavenProject p : reactorProjects) {
			final DefaultArtifactVersion pv = new DefaultArtifactVersion(
					p.getVersion());
			if (p.getGroupId().equals(d.getGroupId())
					&& p.getArtifactId().equals(d.getArtifactId())
					&& depVersionAsRange.containsVersion(pv)) {
				return p;
			}
		}
		return null;
	}

}
