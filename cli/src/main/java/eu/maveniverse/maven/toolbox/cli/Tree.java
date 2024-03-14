/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.toolbox.cli;

import eu.maveniverse.maven.mima.context.Context;
import eu.maveniverse.maven.toolbox.shared.ResolutionScope;
import eu.maveniverse.maven.toolbox.shared.Toolbox;
import eu.maveniverse.maven.toolbox.shared.internal.ToolboxImpl;
import eu.maveniverse.maven.toolbox.shared.internal.resolver.ScopeManagerImpl;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.util.graph.visitor.DependencyGraphDumper;
import picocli.CommandLine;

/**
 * Collects given GAV and output its dependency tree.
 */
@CommandLine.Command(name = "tree", description = "Displays dependency tree")
public final class Tree extends ResolverCommandSupport {

    @CommandLine.Parameters(index = "0", description = "The GAV to graph")
    private String gav;

    @CommandLine.Option(
            names = {"--resolutionScope"},
            defaultValue = "main-runtime",
            description = "Resolution scope to resolve (default main-runtime)")
    private String resolutionScope;

    @CommandLine.Option(
            names = {"--verboseTree"},
            defaultValue = "false",
            description = "Whether the displayed tree needs to be verbose or not (default false)")
    private boolean verboseTree;

    @CommandLine.Option(
            names = {"--mavenLevel"},
            defaultValue = "Maven3",
            description = "The Maven level to use (default Maven3)")
    private String mavenLevel;

    @CommandLine.Option(
            names = {"--boms"},
            defaultValue = "",
            split = ",",
            description = "Comma separated list of BOMs to apply")
    private String[] boms;

    @Override
    protected Integer doCall(Context context) throws Exception {
        java.util.List<Dependency> managedDependencies = importBoms(context, boms);
        Artifact gav = parseGav(this.gav, managedDependencies);

        ScopeManagerImpl scopeManager = createScopeManager(mavenLevel);
        ResolutionScope resolutionScope = toResolutionScope(scopeManager, this.resolutionScope);
        Toolbox toolbox = new ToolboxImpl(context, scopeManager);
        CollectResult collectResult = toolbox.collect(
                resolutionScope,
                new Dependency(gav, ""),
                null,
                managedDependencies,
                context.remoteRepositories(),
                verboseTree);
        collectResult.getRoot().accept(new DependencyGraphDumper(this::info));
        return 0;
    }
}
