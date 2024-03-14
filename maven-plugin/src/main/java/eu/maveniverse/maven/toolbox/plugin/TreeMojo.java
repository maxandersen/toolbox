/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.toolbox.plugin;

import eu.maveniverse.maven.mima.context.Context;
import eu.maveniverse.maven.mima.context.ContextOverrides;
import eu.maveniverse.maven.mima.context.Runtime;
import eu.maveniverse.maven.mima.context.Runtimes;
import eu.maveniverse.maven.toolbox.shared.ResolutionScope;
import eu.maveniverse.maven.toolbox.shared.Toolbox;
import eu.maveniverse.maven.toolbox.shared.internal.ToolboxImpl;
import eu.maveniverse.maven.toolbox.shared.internal.resolver.ScopeManagerImpl;
import java.util.stream.Collectors;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.util.graph.visitor.DependencyGraphDumper;

@Mojo(name = "tree", threadSafe = true)
public class TreeMojo extends AbstractMojo {
    /**
     * The resolution scope to display, accepted values are "main-runtime", "main-compile", "test-runtime" or
     * "test-compile".
     */
    @Parameter(property = "scope", defaultValue = "main-runtime", required = true)
    private String scope;

    /**
     * Set it {@code true} for verbose tree.
     */
    @Parameter(property = "verbose", defaultValue = "false", required = true)
    private boolean verbose;

    /**
     * Set it for wanted {@link ScopeManagerImpl.MavenLevel} way of working.
     */
    @Parameter(property = "mavenLevel", defaultValue = "Maven4WithSystem", required = true)
    private String mavenLevel;

    @Component
    private MavenProject mavenProject;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Runtime runtime = Runtimes.INSTANCE.getRuntime();
        try (Context context = runtime.create(ContextOverrides.create().build())) {
            ScopeManagerImpl scopeManager = new ScopeManagerImpl(ScopeManagerImpl.MavenLevel.valueOf(mavenLevel));
            Toolbox toolbox = new ToolboxImpl(context, scopeManager);
            ResolutionScope resolutionScope = scopeManager
                    .getResolutionScope(scope)
                    .orElseThrow(() -> new IllegalArgumentException("unknown resolution scope"));

            ArtifactTypeRegistry artifactTypeRegistry =
                    context.repositorySystemSession().getArtifactTypeRegistry();
            CollectResult collectResult = toolbox.collect(
                    resolutionScope,
                    RepositoryUtils.toArtifact(mavenProject.getArtifact()),
                    mavenProject.getDependencies().stream()
                            .map(d -> RepositoryUtils.toDependency(d, artifactTypeRegistry))
                            .collect(Collectors.toList()),
                    mavenProject.getDependencyManagement().getDependencies().stream()
                            .map(d -> RepositoryUtils.toDependency(d, artifactTypeRegistry))
                            .collect(Collectors.toList()),
                    context.remoteRepositories(),
                    verbose);
            collectResult.getRoot().accept(new DependencyGraphDumper(getLog()::info));
        } catch (DependencyCollectionException e) {
            throw new MojoExecutionException(e);
        } catch (IllegalArgumentException e) {
            throw new MojoFailureException(e);
        }
    }
}
