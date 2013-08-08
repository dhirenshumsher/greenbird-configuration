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

1.  Place the relevant files under the `/gb-config` package in your classpath:  
    Files with the `*-<profile>.properties` name pattern (e.g. `myapp-default.properties`) will be loaded as configuration properties.    
    Files with the `*-context.xml` name pattern (e.g. `myapp-context.xml`) will be loaded as Spring context definition files.  
    All other files will be ignored.

2.  Activate greenbird-configuration in your project:  
    Add greenbird-configuration as a dependency as described above.    
    Then activate Spring classpath scanning for the `com.greenbird.configuration` package in your main Spring configuration file:  

    ```xml
        <ctx:component-scan base-package="com.greenbird.configuration"/>
    ```

3.  Start using the imported Spring components and configuration properties in your project:  
    The beans defined in the imported Spring definitions are now available for regular use in your Spring context.

    The configuration properties are available to use in property expansion (`${my.property}`) in Spring definition files and `@Value` annotations.   
    They are also available as Spring environment properties (`Environment.getProperty(..)`).

### Details

#### The `gb-config` folder
greenbird-configuration loads configuration properties and Spring context definitions from the classpath.  
The files needs to be placed under the root folder `gb-config` in your classpath and follow the naming patterns described below.  
You can add the files to sub-folders if you like. All compliant files will be loaded whether they are in a sub-folder or not.
Example setup with standard Maven directory layout where all files will be loaded:

```bash
src/main/resources/gb-conf/mymodule-default.properties
src/main/resources/gb-conf/mymodule-context.xml
src/main/resources/gb-conf/props/myothermodule-default.properties
src/main/resources/gb-conf/spring/myothermodule-context.xml
```

#### Spring context definition files
These are regular Spring configuration files. The only special requirement we have for them is that they are named using the `*-context.xml` naming pattern.
In addition to vanilla Spring files you can also add other context files that are based on Spring, E.g. Mule ESB flow definitions.

#### Configuration property files

##### Format
The configuration property files are regular Java property files that support the following syntactic additions:

1.  Property expansion  
    You can reference the value of other properties using the syntax `#{property.name}`:

    ```properties
    props.a=A
    props.b=#{props.a}B
    ```
    ...will give props.a the value "A" and props.b the value "AB". 

2.  Profile overrides  
    You can define different values for a property depending on the currently active Spring profile(s) using the syntax `@profile.property`:

    ```properties
    myprop=A
    @test.myprop=B
    @prod.myprop=C
    ```

    ...will give myprop the value "A" by default, "B" when the test profile is active and "C" when the prod profile is active.

The syntactic additions are borrowed from the [Constretto] project.

##### Spring profile support
The configuration property files must follow the naming pattern `*-<profile>.properties` where `<profile>` is the name of the [Spring profile] the properties should be active for.

Spring profiles is a mechanism introduced in Spring 3.1 that enables you to use different bean configurations in different deployment environments.  
The regular way of telling Spring what profile(s) are active is by setting a system or environment variable called `spring.profiles.active`.

greenbird-configuration extends this mechanism so that you can also differentiate your configuration properties based on the profile setting.

Spring has the notion of default profiles that are activated when you have not explicitly activated profiles. 
The name of the default profile is configurable but by default it is "default".

greenbird-configuration loads properties in this manner:

1. Find all property files related to the default profile(s) and load them.
2. If any profiles are explicitly activated: Load all property files related to the activated profiles.

The last loaded property value will be used when a property is defined more than one time. 
In other words: Later loaded properties takes precedence over (overrides) earlier loaded ones. 

greenbird-configuration will always load any property files related to the default profile(s), even when you have activated another profile.  
This is by design. It enables you to define default values for all properties in the file associated with the default profile and only add necessary overrides in the environment specific property files.

In the format section we described how you can overload properties in a single property file using a special profile tag syntax. 
We recommend that you either use the profile tag approach or the file name approach. Using both at the same time would be confusing.
Projects with a large amount of overrides would probably benefit from using separate files while other projects might benefit from keeping the property variants close together in a single file. 

## History
- [1.0.0-SNAPSHOT]: Initial release.

[1.0.0-SNAPSHOT]:      https://github.com/greenbird/greenbird-configuration/issues?milestone=1&state=closed
[Apache 2.0]:          http://www.apache.org/licenses/LICENSE-2.0.html
[build-badge]:         https://build.greenbird.com/job/greenbird-configuration/badge/icon
[build-link]:          https://build.greenbird.com/job/greenbird-configuration/
[CausedByMatcher]:     https://github.com/greenbird/greenbird-configuration/blob/master/src/main/java/com/greenbird/test/matchers/CausedByMatcher.java
[Constretto]:          http://constretto.github.io/
[download]:            http://search.maven.org/#search|ga|1|greenbird-configuration
[greenbird]:           http://greenbird.com/
[issue-tracker]:       https://github.com/greenbird/greenbird-configuration/issues
[Maven]:               http://maven.apache.org/
[projects]:            http://greenbird.github.io/
[snapshot repository]: https://oss.sonatype.org/content/repositories/snapshots/com/greenbird/greenbird-configuration
[Spring profile]:      http://blog.springsource.com/2011/02/11/spring-framework-3-1-m1-released/
