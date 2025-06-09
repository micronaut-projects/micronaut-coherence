# Micronaut Coherence

[![Maven Central](https://img.shields.io/maven-central/v/io.micronaut.coherence/micronaut-coherence.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.micronaut.coherence%22%20AND%20a:%22micronaut-coherence%22)
[![Build Status](https://github.com/micronaut-projects/micronaut-coherence/workflows/Java%20CI/badge.svg)](https://github.com/micronaut-projects/micronaut-coherence/actions)
[![Revved up by Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A)](https://ge.micronaut.io/scans)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=micronaut-projects_micronaut-coherence&metric=alert_status)](https://sonarcloud.io/project/overview?id=micronaut-projects_micronaut-coherence)

This project provides integrations between [Micronaut](http://micronaut.io) and [Oracle Coherence](https://coherence.community).

## Supported Versions

| Micronaut Coherence | Micronaut | Java       | Community Edition                  | Commercial Edition            |
|---------------------|-----------|------------|------------------------------------|-------------------------------|
| `v5.0.x`            | 4.x       | `17`, `21` | `22.06.x`, `25.03.x`, `14.1.2-0-x` | `14.1.1.2206.x`, `14.1.2-0.x` |
| `v3.10.x`           | 3.x       | `17`       | `22.06.x`                          | `14.1.1.2206.x`               |

## Documentation

See the [Documentation](https://micronaut-projects.github.io/micronaut-coherence/latest/guide/) for more information.

See the [Snapshot Documentation](https://micronaut-projects.github.io/micronaut-coherence/snapshot/guide/) for the current development docs.

## Snapshots and Releases

Snapshots are automatically published to [Sonatype OSS]("https://s01.oss.sonatype.org/content/repositories/snapshots/") using [GitHub Actions](https://github.com/micronaut-projects/micronaut-coherence/actions).

See the documentation in the [Micronaut Docs](https://docs.micronaut.io/latest/guide/#usingsnapshots) for how to configure your build to use snapshots.

Releases are published to Maven Central via [GitHub Actions](https://github.com/micronaut-projects/micronaut-coherence/actions).

Releases are completely automated. To perform a release use the following steps:

* [Publish the draft release](https://github.com/micronaut-projects/micronaut-coherence/releases). There should be already a draft release created, edit and publish it. The Git Tag should start with `v`. For example `v1.0.0`.
* [Monitor the Workflow](https://github.com/micronaut-projects/micronaut-coherence/actions?query=workflow%3ARelease) to check it passed successfully.
* Celebrate!
