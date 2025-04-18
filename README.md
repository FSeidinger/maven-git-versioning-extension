# Maven Git Versioning Extension [![Sparkline](https://stars.medv.io/qoomon/maven-git-versioning-extension.svg?)](https://stars.medv.io/qoomon/maven-git-versioning-extension)

[![Maven Central](https://img.shields.io/maven-central/v/me.qoomon/maven-git-versioning-extension.svg)](https://search.maven.org/artifact/me.qoomon/maven-git-versioning-extension)
[![Changelog](https://badgen.net/badge/changelog/%E2%98%85/blue)](CHANGELOG.md)
[![Build Workflow](https://github.com/qoomon/maven-git-versioning-extension/workflows/Build/badge.svg)](https://github.com/qoomon/maven-git-versioning-extension/actions?query=workflow%3ABuild)
[![LGTM Grade](https://img.shields.io/lgtm/grade/java/github/qoomon/maven-git-versioning-extension)](https://lgtm.com/projects/g/qoomon/maven-git-versioning-extension)

![Example](docs/MavenGitVersioningExtension.png)

**ℹ Also available as [Gradle Plugin](https://github.com/qoomon/gradle-git-versioning-plugin)**

This extension can virtually set project version and properties, based on current **Git status**

ℹ **No POM files will be modified, version and properties are modified in memory only**

* Get rid of…
    * editing `pom.xml`
    * managing project versions within files and Git tags
    * git merge conflicts

## Usage

⚠️ minimal required maven version is `3.6.3`

### Add Extension to Maven Project

create or update `${rootProjectDir}/.mvn/extensions.xml` file

```xml

<extensions xmlns="https://maven.apache.org/EXTENSIONS/1.0.0" xmlns:xsi="https://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="https://maven.apache.org/EXTENSIONS/1.0.0 https://maven.apache.org/xsd/core-extensions-1.0.0.xsd">

    <extension>
        <groupId>me.qoomon</groupId>
        <artifactId>maven-git-versioning-extension</artifactId>
        <version>7.1.2</version>
    </extension>

</extensions>
```

## Configure Extension

ℹ Consider [CI/CD](#cicd-setup) section when running this extension in a CI/CD environment

Create `${rootProjectDir}/.mvn/maven-git-versioning-extension.xml`.

You can configure the version and properties adjustments for specific branches and tags.

**Example:** `maven-git-versioning-extension.xml`

```xml

<configuration xmlns="https://github.com/qoomon/maven-git-versioning-extension"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="https://github.com/qoomon/maven-git-versioning-extension https://qoomon.github.io/maven-git-versioning-extension/configuration-7.0.0.xsd">

    <refs>
        <ref type="branch">
            <pattern>.+</pattern>
            <version>${ref}-SNAPSHOT</version>
            <properties>
                <foo>${ref}</foo>
            </properties>
        </ref>

        <ref type="tag">
            <pattern><![CDATA[v(?<version>.*)]]></pattern>
            <version>${ref.version}</version>
        </ref>
    </refs>

    <!-- optional fallback configuration in case of no matching ref configuration-->
    <rev>
        <version>${commit}</version>
    </rev>

</configuration>
```

### Configuration Elements

- `<disable>` global disable(`true`)/enable(`false`) extension, default is `false`.
    - Can be overridden by command option, see [Parameters & Environment Variables](#parameters--environment-variables).

- `<describeTagPattern>` An arbitrary regex to match tag names for git describe command (has to be a **full match pattern** e.g. `v.+`), default is `.*`
- `<updatePom>` Enable(`true`)/disable(`false`) version and properties update in original pom file, default is `false`
    - Can be overridden by command option, see [Parameters & Environment Variables](#parameters--environment-variables).

- `<refs considerTagsOnBranches="BOOLEAN">` List of ref configurations, ordered by priority. First matching
  configuration will be used.
    - `considerTagsOnBranches` By default, tags pointing at current commit will be ignored if HEAD is attached to a branch. 
     - If this option is `true` tags will always be taken into account.
       <br><br>

    - `<ref type="TYPE">` specific ref patch definition.
      - *required* `type` Ref type indicates which kind of ref will be matched against `pattern`, can be `branch` or `tag`
      - `<pattern>` An arbitrary regex to match ref names
        - has to be a **full match pattern** e.g. `main` or `feature/.+`
          <br><br>

      - `<describeTagPattern>` An arbitrary regex to match tag names for git describe command
        - has to be a **full match pattern** e.g. `v.+`)
        - will override global `<describeTagPattern>` value
          <br><br>

      - `<version>` The new version format, see [Format Placeholders](#format-placeholders)
      - `<properties>`
        - `<name>value</name>` A property definition to update the value of a property.
          - `<name>` The property name
          - `value` The new value format of the property, see [Format Placeholders](#format-placeholders)
            <br><br>

      - `<updatePom>` Enable(`true`) or disable(`false`) version and properties update in original pom file
        - will override global `<updatePom>` value

- `<rev>` Rev configuration will be used if no ref configuration is matching current git situation.
    - same as `<ref>` configuration, except `type` attribute and `<pattern>` element.

- `<relatedProjects>` Add external projects as related project to update their versions as well.
   ```xml
   <relatedProjects>
       <project>
           <groupId>me.qoomon</groupId>
           <artifactId>base</artifactId>
       </project>
   </relatedProjects>
   ```

### Format Placeholders

ℹ `….slug` placeholders means all `/` characters will be replaced by `-`.

ℹ Final `version` will be slugified automatically, so no need to use `${….slug}` placeholders in `<version>` format.

ℹ define placeholder default value (placeholder is not defined) like this `${name:-DEFAULT_VALUE}`<br>
e.g `${env.BUILD_NUMBER:-0}` or `${env.BUILD_NUMBER:-local}`

ℹ define placeholder overwrite value (placeholder is defined) like this `${name:+OVERWRITE_VALUE}`<br>
e.g `${dirty:-SNAPSHOT}` resolves to `-SNAPSHOT` instead of `-DIRTY`

###### Placeholders

- `${env.VARIABLE}` Value of environment variable `VARIABLE`
- `${property.name}` Value of commandline property `-Dname=value`
      <br><br>

- `${version}` `<version>` set in `pom.xml` e.g. '1.0.0-SNAPSHOT'
- `${version.release}` like `${version}` without `-SNAPSHOT` postfix e.g. '1.0.0'
      <br><br>

- `${ref}` `${ref.slug}` ref name (branch or tag name or commit hash)
- Ref Pattern Groups
    - Content of regex groups in `<ref><pattern>` can be addressed like this:
    - `${ref.GROUP_NAME}` `${ref.GROUP_NAME.slug}`
    - `${ref.GROUP_INDEX}` `${ref.GROUP_INDEX.slug}`
    - Named Group Example
        ```xml
        <ref type="branch">
            <pattern><![CDATA[feature/(?<feature>.+)]]></pattern>
            <version>${ref.feature}-SNAPSHOT</version>
        </ref>
        ```
        <br>

- `${commit}` commit hash '0fc20459a8eceb2c4abb9bf0af45a6e8af17b94b'
- `${commit.short}` commit hash (7 characters) e.g. '0fc2045'
- `${commit.timestamp}` commit timestamp (epoch seconds) e.g. '1560694278'
- `${commit.timestamp.year}` commit year e.g. '2021'
- `${commit.timestamp.year.2digit}` 2-digit commit year.g. '21'
- `${commit.timestamp.month}` commit month of year e.g. '12'
- `${commit.timestamp.day}` commit day of month e.g. '23'
- `${commit.timestamp.hour}` commit hour of day (24h)e.g. '13'
- `${commit.timestamp.minute}` commit minute of hour e.g. '59'
- `${commit.timestamp.second}` commit second of minute e.g. '30'
- `${commit.timestamp.datetime}` commit timestamp formatted as `yyyyMMdd.HHmmss`e.g. '20190616.161442'
      <br><br>

- `${describe}` Will resolve to `git describe` output
- `${describe.distance}` The distance count to last matching tag
- `${describe.tag}` The matching tag of `git describe`
- Describe Tag Pattern Groups
    - Content of regex groups in `<describeTagPattern>` can be addressed like this:
    - `${describe.tag.GROUP_NAME}` `${describe.tag.GROUP_NAME.slug}`
    - `${describe.tag.GROUP_INDEX}` `${describe.tag.GROUP_INDEX.slug}`
    - Named Group Example
        ```xml
        <ref type="branch">
            <pattern>main</pattern>
            <describeTagPattern><![CDATA[v(?<version>.*)]]></describeTagPattern>
            <version>${describe.tag.version}-SNAPSHOT</version>
        </ref>
        ```
        <br> 

- `${dirty}` If repository has untracked files or uncommitted changes this placeholder will resolve to `-DIRTY`, otherwise it will resolve to an empty string.
    - ℹ May lead to performance issue on very large projects (10,000+ files)
- `${dirty.snapshot}` Like `${dirty}`, but will resolve to `-SNAPSHOT`
      <br><br>

- `${value}` Original value of matching property (Only available within property format)


### Parameters & Environment Variables

- Disable Extension
    - **Environment Variables**
     - `export VERSIONING_DISABLE=true`
    - **Command Line Parameters**
     - `mvn … -Dversioning.disable`

- Provide **branch** or **tag** name
    - **Environment Variables**
     - `export VERSIONING_GIT_REF=$PROVIDED_REF` e.g. `refs/heads/main`, `refs/tags/v1.0.0` or `refs/pull/1000/head`
     - `export VERSIONING_GIT_BRANCH=$PROVIDED_BRANCH_NAME` e.g. `main` or `refs/heads/main`
     - `export VERSIONING_GIT_TAG=$PROVIDED_TAG_NAME` e.g. `v1.0.0` or `refs/tags/v1.0.0`
    - **Command Line Parameters**
     - `mvn … -Dgit.ref=$PROVIDED_REF`
     - `mvn … -Dgit.branch=$PROVIDED_BRANCH_NAME`
     - `mvn … -Dgit.tag=$PROVIDED_TAG_NAME`

  ℹ Especially useful for **CI builds** see [Miscellaneous Hints](#miscellaneous-hints)

- Update `pom.xml`
    - **Environment Variables**
     - `export VERSIONING_UPDATE_POM=true`
    - **Command Line Parameters**
     - `mvn … -Dversioning.updatePom`

## Provided Project Properties

- `git.commit` e.g. '0fc20459a8eceb2c4abb9bf0af45a6e8af17b94b'
- `git.commit.short` e.g. '0fc2045'
- `git.commit.timestamp` e.g. '1560694278'
- `git.commit.timestamp.datetime` e.g. '2019-11-16T14:37:10Z'

- `git.ref` `git.ref.slug` HEAD ref name (branch or tag name or commit hash)

---

## IDE Setup

### IntelliJ

For a flawless experience you need to disable this extension during project import. 
Disable it by adding `-Dversioning.disable=true` to Maven Importer VM options (Preferences > Build, Execution, Deployment > Build Tools > Maven > Importing > VM options for importer).

## CI/CD Setup

Most CI/CD systems do checkouts in a detached HEAD state so no branch information is available, however they provide environment variables with this information. 
You can provide those, by using [Parameters & Environment Variables](#parameters--environment-variables). 

### Native Support

* GitHub Actions: if `$GITHUB_ACTIONS == true`, `GITHUB_REF` is considered
* GitLab CI: if `$GITLAB_CI == true`, `CI_COMMIT_BRANCH` and `CI_COMMIT_TAG` are considered
* Circle CI: if `$CIRCLECI == true`, `CIRCLE_BRANCH` and `CIRCLE_TAG` are considered
* Jenkins: if `JENKINS_HOME` is set, `BRANCH_NAME` and `TAG_NAME` are considered

### Manual Setup

Set following environment variables before running your `mvn` command

```shell
export VERSIONING_GIT_REF=$PROVIDED_REF;
```

`$PROVIDED_REF` value examples: `refs/heads/main`, `refs/tags/v1.0.0` or `refs/pull/1000/head`

or

```shell
export VERSIONING_GIT_BRANCH=$PROVIDED_BRANCH;
export VERSIONING_GIT_TAG=$PROVIDED_TAG;
```

`$PROVIDED_BRANCH` value examples: `main`, `refs/heads/main` or `refs/pull/1000/head`

`$PROVIDED_TAG` value examples: `v1.0.0` or `refs/tags/v1.0.0`

---

## Miscellaneous Hints

### Commandline To Print Project Version

`mvn help:evaluate -Dexpression=project.version -q -DforceStdout`

### Reproducible builds

The [maven reproducible builds feature](https://maven.apache.org/guides/mini/guide-reproducible-builds.html) can be easily supported with this extension, by using the commit timestamp as build timestamps.

```xml

<properties>
    <project.build.outputTimestamp>${git.commit.timestamp.datetime}</project.build.outputTimestamp>
</properties>
```

---

## Build & Release

```shell
gpg --import private.key
gpg --list-keys
```

```shell
  mvn verify
  # Publishes this plugin to local Maven
  mvn install
  # Run integration tests after install, 
  # integration tests will run with LATEST version of extension installed
  mvn failsafe:integration-test
  # Publishes this plugin to OSS Nexus.
  GPG_TTY=$(tty) mvn clean deploy -P release -Dgpg.keyname=???
```

##### Debug

`mvn help:evaluate -Dexpression=project.version -Dorg.slf4j.simpleLogger.log.me.qoomon.maven.gitversioning=debug`
