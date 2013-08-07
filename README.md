# greenbird-configuration
Library that simplifies configuration and context loading for Spring based projects.

Projects using this library will benefit from:
- Automatic loading of Spring context definitions.
- Automatic loading of configuration properties.
- Visibility of loaded components and configuration properties through logging and JMX.
- Environments separated using regular Spring profiles.  
- Automatic overloading of configuration properties for the active profile(s).
- Profile-specific properties can either be defined in separate files or side-by-side in a single file.
- Configuration values can be referenced in other properties through property expansion.
- Cross pollination of Spring property placeholder values and `Environment` properties.  

Part of the [greenbird] Open Source Java [projects].

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].

[![Build Status][build-badge]][build-link]

## Table of contents
- [License](#license)
- [Obtain](#obtain)
- [Usage](#usage)
- [History](#history)

## License
[Apache 2.0]

## Obtain
The project is based on [Maven] and is available on the central Maven repository.

Example dependency config:

```xml
<dependency>
    <groupId>com.greenbird</groupId>
    <artifactId>greenbird-configuration</artifactId>
    <version>1.0.0</version>
</dependency>
```

You can also [download] the jar directly if you need too.

Snapshot builds are available from the Sonatype OSS [snapshot repository].

Include the jar as a runtime scope dependency and configure Spring as described below.

## Usage

### Quick start
The main functionality of greenbird-configuration is to automatically load and manage configuration properties and Spring context definitions for your application.

#### 1. Place the relevant files under the `/gb-config` package in your classpath.
Files with the `*.properties` name pattern will be loaded as configuration properties.
Files with the `*-context.xml` name pattern will be loaded as Spring context definition files.
All other files will be ignored.

#### 2. Activate greenbird-configuration in your project
Add greenbird-configuration as a dependency as described above and activate Spring classpath scanning for the `com.greenbird.configuration` package in your main Spring configuration file:

```xml
    <ctx:component-scan base-package="com.greenbird.configuration"/>
```

#### 3. Start using the imported Spring components and configuration properties in your project
The beans defined in the imported Spring definitions are now available for regular use in your Spring context.

The configuration properties are available to use in property expansion in Spring definition files and `@Value` annotations. They are also available as Spring environment properties (`Environment.getProperty(..)`)

### Details
TODO


## History
- [1.0.0-SNAPSHOT]: Initial release.

[1.0.0-SNAPSHOT]:      https://github.com/greenbird/greenbird-configuration/issues?milestone=1&state=closed
[Apache 2.0]:          http://www.apache.org/licenses/LICENSE-2.0.html
[build-badge]:         https://build.greenbird.com/job/greenbird-configuration/badge/icon
[build-link]:          https://build.greenbird.com/job/greenbird-configuration/
[CausedByMatcher]:     https://github.com/greenbird/greenbird-configuration/blob/master/src/main/java/com/greenbird/test/matchers/CausedByMatcher.java
[download]:            http://search.maven.org/#search|ga|1|greenbird-configuration
[greenbird]:           http://greenbird.com/
[issue-tracker]:       https://github.com/greenbird/greenbird-configuration/issues
[Maven]:               http://maven.apache.org/
[projects]:            http://greenbird.github.io/
[snapshot repository]: https://oss.sonatype.org/content/repositories/snapshots/com/greenbird/greenbird-configuration
