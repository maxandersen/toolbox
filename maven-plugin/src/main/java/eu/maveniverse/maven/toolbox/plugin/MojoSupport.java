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
import eu.maveniverse.maven.toolbox.shared.Output;
import eu.maveniverse.maven.toolbox.shared.Slf4jOutput;
import eu.maveniverse.maven.toolbox.shared.ToolboxCommando;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support class for all Mojos.
 */
public abstract class MojoSupport extends AbstractMojo {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Output output = new Slf4jOutput(logger);

    @Parameter(property = "failOnLogicalFailure", defaultValue = "true")
    protected boolean failOnLogicalFailure;

    @Parameter(defaultValue = "${settings}", readonly = true, required = true)
    protected Settings settings;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        Runtime runtime = Runtimes.INSTANCE.getRuntime();
        try (Context context = runtime.create(ContextOverrides.create().build())) {
            boolean result = doExecute(ToolboxCommando.create(runtime, context));
            if (!result && failOnLogicalFailure) {
                throw new MojoFailureException("Operation failed");
            }
        } catch (RuntimeException e) {
            throw new MojoExecutionException(e);
        } catch (Exception e) {
            throw new MojoFailureException(e);
        }
    }

    protected abstract boolean doExecute(ToolboxCommando toolboxCommando) throws Exception;
}
