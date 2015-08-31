#ph-jscompress-maven-plugin

A Maven plugin to compress JS files at build time using Google Closure.
Since v2.0.0 this plugin requires a JDK 1.7 runtime to be used.
Since v2.1.0 this plugin requires at least 3.x. v2.0.2 is the last version supporting Maven 2.x.  

## Maven configuration
```xml
      <plugin>
        <groupId>com.helger.maven</groupId>
        <artifactId>ph-jscompress-maven-plugin</artifactId>
        <version>2.1.0</version>
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

---

On Twitter: <a href="https://twitter.com/philiphelger">Follow @philiphelger</a>
