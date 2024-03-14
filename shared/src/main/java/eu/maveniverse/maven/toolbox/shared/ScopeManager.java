/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.toolbox.shared;

import java.util.Collection;
import java.util.Optional;

/**
 * Scope manager.
 */
public interface ScopeManager {
    /**
     * The label.
     */
    String getId();

    /**
     * Returns the "system" scope, if exists.
     * <p>
     * This is a special scope. In this scope case, Resolver should handle it specially, as it has no POM (so is
     * always a leaf on graph), is not in any repository, but is actually hosted on host OS file system. On resolution
     * resolver merely checks is file present or not.
     */
    Optional<DependencyScope> getSystemScope();

    /**
     * Returns a specific dependency scope by label.
     * <p>
     * Note: despite returns optional, this method may throw as well, if manager set in "strict" mode.
     */
    Optional<DependencyScope> getDependencyScope(String id);

    /**
     * Returns the "universe" (all) of dependency scopes.
     */
    Collection<DependencyScope> getDependencyScopeUniverse();

    /**
     * Returns a specific resolution scope by label.
     * <p>
     * Note: despite returns optional, this method may throw as well, if manager set in "strict" mode.
     */
    Optional<ResolutionScope> getResolutionScope(String id);

    /**
     * Returns the "universe" (all) of resolution scopes.
     */
    Collection<ResolutionScope> getResolutionScopeUniverse();
}
