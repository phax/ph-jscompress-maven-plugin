# ph-jscompress-maven-plugin

A Maven plugin to compress JS files at build time using Google Closure.

# Maven configuration

```xml
  <plugin>
    <groupId>com.helger.maven</groupId>
    <artifactId>ph-jscompress-maven-plugin</artifactId>
    <version>2.2.3</version>
    <executions>
      <execution>
        <goals>
          <goal>jscompress</goal>
        </goals>
      </execution>
    </executions>
    <configuration>
      <forceCreation>false</forceCreation>
      <sourceDirectory>${basedir}/src/main/webapp</sourceDirectory>
    </configuration>
  </plugin>
```


# News and noteworthy

* v2.2.4 - work in progress
    * Updated to Google Closure v20201102
* v2.2.3 - 2020-03-11
    * Updated to Google Closure v20200224
* v2.2.2 - 2019-07-15
    * Updated to Google Closure v20190709
* v2.2.1 - 2018-08-06
    * Updated to Google Closure v20180716
* v2.2.0 - 2018-01-08
    * Requires a JDK 1.8 runtime to be used
    * Updated to Google Closure v20180101
* v2.1.1 - 2015-12-01
    * Updated to Google Closure v20150901 
* v2.1.0 - 2015-08-31
    * requires at least Maven 3.x. v2.0.2 is the last version supporting Maven 2.x
    * Updated to Google Closure v20150729
* v2.0.2 - 2015-07-01
    * Updated to Google Closure v20150609
* v2.0.1 - 2015-06-01
    * Updated to Google Closure v20150505
* v2.0.0 - 2015-03-09
    * requires a JDK 1.7 runtime to be used
* v1.1.0 - 2014-08-26  

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodingStyleguide.md) |
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a> |
Kindly supported by [YourKit Java Profiler](https://www.yourkit.com)