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
import eu.maveniverse.maven.toolbox.shared.internal.MavenConfiguration;
import eu.maveniverse.maven.toolbox.shared.internal.ToolboxImpl;
import eu.maveniverse.maven.toolbox.shared.internal.resolver.ScopeManagerImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.util.graph.visitor.DependencyGraphDumper;

@Mojo(name = "gav-tree", requiresProject = false, threadSafe = true)
public class GavTreeMojo extends AbstractMojo {
    /**
     * The artifact coordinates in the format {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}
     * to display tree for.
     */
    @Parameter(property = "gav", required = true)
    private String gav;

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
     * Set it for wanted way of working ("Maven3" or "Maven4").
     */
    @Parameter(property = "mavenLevel", defaultValue = "Maven3", required = true)
    private String mavenLevel;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Runtime runtime = Runtimes.INSTANCE.getRuntime();
        try (Context context = runtime.create(ContextOverrides.create().build())) {
            ScopeManagerImpl scopeManager = new ScopeManagerImpl(
                    mavenLevel.equalsIgnoreCase("maven3") ? MavenConfiguration.MAVEN3 : MavenConfiguration.MAVEN4);
            Toolbox toolbox = new ToolboxImpl(context, scopeManager);
            ResolutionScope resolutionScope = scopeManager
                    .getResolutionScope(scope)
                    .orElseThrow(() -> new IllegalArgumentException("unknown resolution scope"));

            CollectResult collectResult = toolbox.collect(
                    resolutionScope,
                    new Dependency(new DefaultArtifact(gav), ""),
                    null,
                    null,
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
