/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.toolbox.shared.internal;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Objects;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;

/**
 * A dependency selector that filters transitive dependencies based on their scope. It is configurable from which level
 * applies, as it depend on "as project" and "as dependency" use cases.
 * <p>
 * <em>Note:</em> This filter does not assume any relationships between the scopes.
 * In particular, the filter is not aware of scopes that logically include other scopes.
 *
 * @see Dependency#getScope()
 */
public final class ScopeDependencySelector implements DependencySelector {
    /**
     * Selects dependencies by scope always (from root).
     */
    public static ScopeDependencySelector fromRoot(Collection<String> included, Collection<String> excluded) {
        return from(1, included, excluded);
    }

    /**
     * Selects dependencies by scope starting from direct dependencies.
     */
    public static ScopeDependencySelector fromDirect(Collection<String> included, Collection<String> excluded) {
        return from(2, included, excluded);
    }

    /**
     * Selects dependencies by scope starting from given depth (1=root, 2=direct, 3=transitives of direct ones...).
     */
    public static ScopeDependencySelector from(
            int applyFrom, Collection<String> included, Collection<String> excluded) {
        if (applyFrom < 1) {
            throw new IllegalArgumentException("applyFrom must be non-zero and positive");
        }
        return new ScopeDependencySelector(0, applyFrom, Integer.MAX_VALUE, included, excluded);
    }

    /**
     * Selects dependencies by scope starting from given depth (1=root, 2=direct, 3=transitives of direct ones...) to
     * given depth.
     */
    public static ScopeDependencySelector fromTo(
            int applyFrom, int applyTo, Collection<String> included, Collection<String> excluded) {
        if (applyFrom < 1) {
            throw new IllegalArgumentException("applyFrom must be non-zero and positive");
        }
        if (applyFrom > applyTo) {
            throw new IllegalArgumentException("applyTo must be greater or equal than applyFrom");
        }
        return new ScopeDependencySelector(0, applyFrom, applyTo, included, excluded);
    }

    private final int depth;

    private final int applyFrom;

    private final int applyTo;

    private final Collection<String> included;

    private final Collection<String> excluded;

    private ScopeDependencySelector(
            int depth, int applyFrom, int applyTo, Collection<String> included, Collection<String> excluded) {
        this.depth = depth;
        this.applyFrom = applyFrom;
        this.applyTo = applyTo;
        this.included = included;
        this.excluded = excluded;
    }

    @Override
    public boolean selectDependency(Dependency dependency) {
        requireNonNull(dependency, "dependency cannot be null");
        if (depth < applyFrom || depth > applyTo) {
            return true;
        }

        String scope = dependency.getScope();
        return (included == null || included.contains(scope)) && (excluded == null || !excluded.contains(scope));
    }

    @Override
    public DependencySelector deriveChildSelector(DependencyCollectionContext context) {
        requireNonNull(context, "context cannot be null");
        return new ScopeDependencySelector(depth + 1, applyFrom, applyTo, included, excluded);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (null == obj || !getClass().equals(obj.getClass())) {
            return false;
        }

        ScopeDependencySelector that = (ScopeDependencySelector) obj;
        return depth == that.depth
                && applyFrom == that.applyFrom
                && Objects.equals(included, that.included)
                && Objects.equals(excluded, that.excluded);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + depth;
        hash = hash * 31 + applyFrom;
        hash = hash * 31 + (included != null ? included.hashCode() : 0);
        hash = hash * 31 + (excluded != null ? excluded.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format(
                "%s(included: %s, excluded: %s, applied: %s)",
                getClass().getSimpleName(), included, excluded, depth >= applyFrom);
    }
}
