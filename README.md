# Toolbox

Toolbox is a multi-faceted (CLI and Maven Plugin) utility that contains useful commands and also showcase usage
of [Maveniverse MIMA](https://github.com/maveniverse/mima).

Build requirements:
* Java 21
* Maven 3.9.9+

Runtime requirement:
* Java 11+ (CLI and Maven Plugin)
* Maven 3.6.3+ (Maven Plugin)

Usage: use the help command/mojo to get help about Toolbox:
```
$ mvn eu.maveniverse.maven.plugins:toolbox:help -Ddetail
```
or
```
$ jbang toolbox@maveniverse --help
```
or you can download the CLI JAR from Maven Central and run it directly.
Finally, just go over sources to get the idea what is happening.

## About the project

The Toolbox project started with manifold aims:
* replace `MIMA CLI`, provide drop-in replacement, but also continue improving it.
* provide alternative for `maven-dependency-plugin`, offering similar (sub)set of Mojos
* showcase MIMA, demonstrate how it helps to write reusable Resolver code that runs embedded in Maven (as Mojos or Extensions), but also outside of Maven as CLI
* to experiment: was birthplace of Resolver 2.x `ScopeManager` (that is now part of Resolver 2.x, while Resolver 1.x circumvention is present in Toolbox that supports Maven 3.6+), fixing MNG-8041

Structure of the project:
* Module "shared" is a reusable library module, that depends on MIMA `Context` only (and Resolver APIs), and implements all the logic.
* Module "toolbox" is a Maven Plugin and a CLI at the same time, that exposes Toolbox operations as Mojos and commands. Each Mojo comes in two
"flavors": without prefix (i.e. "tree"), that requires project, and uses `MavenProject` to get the data for requests, and "gav-" 
prefixed ones (i.e. "gav-tree"), that do not require project, and is able to target any existing Artifact out there.
