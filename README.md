# greenbird-configuration
Library that simplifies configuration and context loading for Spring based projects.

Projects using this library will benefit from:
- Automatic loading of Spring context definitions.
- Automatic loading of configuration properties from classpath and file system.
- Environments separated using regular Spring profiles.  
- Automatic overloading of configuration properties for the active profile(s).
- Profile-specific properties can either be defined in separate files or side-by-side in a single file.
- Configuration values can be referenced in other properties through property expansion.
- Cross pollination of Spring property placeholder values and `Environment` properties.  
- Visibility of loaded components and configuration properties through logging and JMX.
- Automatic loading of configuration and Spring definitions for all dependencies that are using greenbird-configuration.
- Transparent support for the Mule and Camel frameworks with the [mule-configuration] and [camel-configuration] extensions.

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
    <version>1.3.0</version>
</dependency>
```

You can also [download] the jar directly if you need too.

Snapshot builds are available from the Sonatype OSS [snapshot repository].

Include the jar as a runtime scope dependency and configure Spring as described below.

## Usage

### Quick start
The main functionality of greenbird-configuration is to automatically load and manage configuration properties and Spring context definitions for your application and your applications dependencies.

1.  Place the relevant files under the `/gb-conf` package in your classpath:  
    Files with the `*-<profile>.properties` name pattern (e.g. `myapp-default.properties`) will be loaded as configuration properties.    
    Files with the `*-context.xml` name pattern (e.g. `myapp-context.xml`) will be loaded as Spring context definition files.  
    All other files will be ignored.

2.  Activate greenbird-configuration in your project:  
    Add greenbird-configuration as a dependency as described above.    
    Then activate Spring classpath scanning for the `com.greenbird.configuration` package in your main Spring configuration file:  

    ```xml
	    <ctx:component-scan base-package="com.greenbird.configuration, com.optionally.your.package.here"/>
    ```
3.  Start using the imported Spring components and configuration properties in your project:  
    The beans defined in the imported Spring definitions are now available for regular use in your Spring context.

    The configuration properties are available to use in property expansion (`${my.property}`) in Spring definition files and `@Value` annotations.   
    They are also available as Spring environment properties (`Environment.getProperty(..)`).

### Details

#### The `gb-conf` folder
greenbird-configuration loads configuration properties and Spring context definitions from the classpath.  
The files needs to be placed under the root folder `gb-conf` in your classpath and follow the naming patterns described below.  
You can add files to sub-folders if you like. All compliant files will be loaded whether they are in a sub-folder or not.
Example setup with standard Maven directory layout where all files will be loaded:

```bash
src/main/resources/gb-conf/mymodule-default.properties
src/main/resources/gb-conf/mymodule-context.xml
src/main/resources/gb-conf/props/myothermodule-default.properties
src/main/resources/gb-conf/spring/myothermodule-context.xml
```

#### Loading configuration properties from the file system
It is possible to load configuration property files from the file system in addition to the classpath.
You can configure one or more folders on the file system to be scanned for configuration properties in the same way as 
the `gb-conf` classpath folders are scanned.  

Configuration loaded from the file system always takes precedence over the configuration loaded from the classpath.

You define what folders to scan by defining them in properties with names ending with `greenbird.config.dir`. 
So `greenbird.config.dir`, `foo.greenbird.config.dir` and `bar.greenbird.config.dir` can all be defined to load 
different folders.

There are three ways these properties can be defined:

1.   As environment variables. Note that *nix environments do not allow periods (.) in environment variable names but 
     you can use the synonym `greenbird_config_dir` instead. E.g. `export foo_greenbird_config_dir=/etc/myapp/myconfigfolder`.
2.   As Java system properties. E.g. starting your app with `-Dfoo.greenbird.config.dir=/etc/myapp/myconfigfolder -Dbar.greenbird.config.dir=/etc/myapp/myotherconfigfolder`.
3.   As configuration properties in your classpath configuration. E.g. adding `foo.greenbird.config.dir=/etc/myapp/myconfigfolder` to `/gb-conf/mymodule-prod.properties`.

The list above also define the order of precedence between the loading methods. 

#### Spring context definition files
These are regular Spring configuration files. The only special requirement we have for them is that they are named using the `*-context.xml` naming pattern.
In addition to vanilla Spring files you can also add other definition files that are based on Spring, E.g. Mule ESB flow definitions.

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

1. Find all property files related to the pseudo profile `preset` and load them.
2. Find all property files related to the default profile(s) and load them.
3. If any profiles are explicitly activated: Load all property files related to the activated profiles.

The last loaded property value will be used when a property is defined more than one time. 
In other words: Later loaded properties takes precedence over (overrides) earlier loaded ones. 

The `preset` pseudo profile (e.g. `*-preset.properties` files) should be used when you are creating a reusable library based on greenbird-configuration.
This enables you to define a sensible preset configuration for your library whilst the users of the library can override configuration values as needed.

greenbird-configuration will always load any property files related to the default profile(s), even when you have activated another profile.  
This is by design. It enables you to define default values for all properties in the file associated with the default profile and only add necessary overrides in the environment specific property files.

In the format section we described how you can overload properties in a single property file using a special profile tag syntax. 
We recommend that you either use the profile tag approach or the file name approach. Using both at the same time would be confusing.
Projects with a large amount of overrides would probably benefit from using separate files while other projects might benefit from keeping the property variants close together in a single file.
 
#### Visibility
With all this automatic loading of data it is important to be able to verify what has been loaded and from where. greenbird-configuration makes this information available in two ways.

##### Log report
On startup we log a configuration report on the INFO level via [SLF4J] on the `com.greenbird.configuration.report.ConfigurationReporter` log category.

If you activate INFO logging for this category you will be able to see output like this example from one of our unit tests:

```
***********************************************************************
GREENBIRD CONFIGURATION REPORT
***********************************************************************

SPRING PROFILES
---------------
Active profiles:  prod, other, testprofile
Default profiles: default

AUTO-LOADED CONFIGURATION FILES
-------------------------------
file [/dev/configuration/target/test-classes/gb-conf/greenbird-default.properties]
file [/dev/configuration/target/test-classes/gb-conf/greenbird-testprofile.properties]

CONFIGURATION PROPERTIES
------------------------------------
default.test.property     = testProfileValue
environment.test.property = envValueProd
pojo.test.property        = pojoValue
test.newPassword          = *****
test.property             = valueProd
test.property.2           = value2Other
test.pw.old               = *****
test.reference            = valueProd-reference
test.uuid                 = random.GREENBIRD_CONFIG_UUID.test.GREENBIRD_CONFIG_UUID

AUTO-LOADED SPRING DEFINITION FILES
-----------------------------------
file [/dev/configuration/target/test-classes/gb-conf/sub_config_1/greenbird-context.xml]
file [/dev/configuration/target/test-classes/gb-conf/sub_config_2/greenbird-context.xml]
file [/dev/configuration/target/classes/gb-conf/greenbird-configuration-context.xml]

BEANS IN CONTEXT
----------------
com.greenbird.configuration:
  ConfigPojoTestBean (pojoTestBean)
  ConfigTestBean (configTestBean)
  ConfigurationMBean (configurationMBean)
  ConfigurationPropertyPlaceholderConfigurer (configurationPropertyPlaceholderConfigurer)
  ConfigurationReporter (configurationReporter)
  ContextTestBean1 (contextTestBean1)
  ContextTestBean2 (contextTestBean2)
  ResourceFinder (resourceFinder)
  SpringContextLoader$$EnhancerByCGLIB$$3d82d5dd (springContextLoader)

com.greenbird.configuration.sub:
  BeanToBeReported (beanToBeReported1, beanToBeReported2)
  OtherBeanToBeReported (otherBeanToBeReported)

***********************************************************************
```

##### JMX
The configuration report data is also made available via [JMX] and can be accessed via a standalone JMX client or the administration console of most Java application servers.

Object name: `greenbird.configuration:name=greenbirdConfiguration,type=GreenbirdConfiguration`

Attributes:
- ActiveSpringProfiles (String)
- DefaultSpringProfiles (String)
- LoadedConfigurationFiles (List\<String>)
- PropertiesReport (String)
- LoadedSpringDefinitionFiles (List\<String>)
- BeansInContext (List\<String>)

##### Value masking
There are some configuration values that you do not want to expose. E.g. passwords.  
greenbird-configuration can mask (*****) sensitive values when reporting on properties.
By default any property with a name matching the regular expression `.*(\.pw|password|passwd|pwd).*` will have it's value masked in reports. 
The matching is case insensitive.

You can also tell greenbird-configuration to mask additional values by setting the `greenbird.config.mask.pattern` configuration property. 
The value must be a valid Java regular expression that matches the whole property name. The matching will be case insensitive.
E.g. if you want to mask values defining host names you could add the following configuration `greenbird.config.mask.pattern=.*host.*`.

#### Advanced loading
For your production code you will normally load greenbird-configuration by activating Spring classpath scanning for the 
`com.greenbird.configuration` package in your main Spring configuration file:  
                                                                        
```xml
    <ctx:component-scan base-package="com.greenbird.configuration, com.optionally.your.package.here"/>
```

This is normally the entire content of your main Spring configuration file. Everything else will be automatically loaded by greenbird-configuration. 	

Note that Spring does not support multiple `component-scan` beans, so consolidate your existing `base-package` attribute into a comma-seperated list, if any. 

If you need you can configure greenbird-configuration to load only some of it's sub-systems instead of all of them.
This is done through limiting the Spring classpath scanning to selected sub-packages. 
Here is an overview of the 4 sub-systems and their packages:

- Configuration properties (`com.greenbird.configuration.properties`): Automatic loading of configuration properties.
- Context definitions (`com.greenbird.configuration.context`): Automatic loading of Spring context definitions.
- Report (`com.greenbird.configuration.report`): Logging of loaded properties and components.
- JMX (`com.greenbird.configuration.jmx`): Exposing report data via [JMX].

You can control which of these subsystems are loaded by only specifying the packages you need. E.g.:
```xml
    <ctx:component-scan base-package="com.greenbird.configuration.properties, com.greenbird.configuration.report"/>
```
..will load configuration properties and log them, but will not load Spring definitions or expose data via [JMX].

This kind of explicit sub-system loading can be especially useful when testing. E.g. if you want to load only some specific Spring context for your test.
In such a scenario you will typically not load the context definitions sub-system but load Spring definition files explicitly in your tests instead.

## History
- [1.3.0]: Fixed bugs on beans report, updated documentation. Updated to latest parent POM. 
- [1.2.0]: Added possibility for 3rd party components to look up configuration as regular Java `Properties`. Fixed bugs where the report was failing on abstract Spring beans and dynamic proxies. Dependency and doc enchantments.
- [1.1.0]: Added support for loading configuration properties from the file system.
- [1.0.0]: Initial release.

[1.0.0]:               https://github.com/greenbird/greenbird-configuration/issues?milestone=1&state=closed
[1.1.0]:               https://github.com/greenbird/greenbird-configuration/issues?milestone=2&state=closed
[1.2.0]:               https://github.com/greenbird/greenbird-configuration/issues?q=milestone%3Ar1.2.0+is%3Aclosed
[1.3.0]:               https://github.com/greenbird/greenbird-configuration/issues?q=milestone%3Ar1.3.0+is%3Aclosed
[Apache 2.0]:          http://www.apache.org/licenses/LICENSE-2.0.html
[camel-configuration]: https://github.com/greenbird/camel-configuration
[build-badge]:         https://build.greenbird.com/job/greenbird-configuration/badge/icon
[build-link]:          https://build.greenbird.com/job/greenbird-configuration/
[Constretto]:          http://constretto.github.io/
[download]:            http://search.maven.org/#search|ga|1|greenbird-configuration
[greenbird]:           http://greenbird.com/
[issue-tracker]:       https://github.com/greenbird/greenbird-configuration/issues
[JMX]:                 http://www.oracle.com/technetwork/java/javase/tech/javamanagement-140525.html
[Maven]:               http://maven.apache.org/
[mule-configuration]:  https://github.com/greenbird/mule-configuration
[projects]:            http://greenbird.github.io/
[snapshot repository]: https://oss.sonatype.org/content/repositories/snapshots/com/greenbird/greenbird-configuration
[SLF4J]:               www.slf4j.org
[Spring profile]:      http://blog.springsource.com/2011/02/11/spring-framework-3-1-m1-released/
