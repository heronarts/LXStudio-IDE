# Flavius LEDPortal

This is a fork of the excellent [LX Studio](http://lx.studio/) by Heronarts, Please read the [copyright and licensing notices](#lx-studio-notices) at the bottom of this readme.

## LEDPortal

[![LEDJing at SolidState](img/Solid%20State%20LeapMotion.gif)](https://www.youtube.com/watch?v=Ui-maztzuMk)

![Render Compromise Side](img/render_compromise_side.png)![Render Compromise Front](img/render_compromise_front.png)

LEDPortal is an interactive LED art project started by the Flavius theme camp for Burning Seed in 2020.

Some videos of development progress:

- <https://www.instagram.com/p/CC-x_NOnNkP>
- <https://www.instagram.com/p/CBuyp0GHbRQ>

### Prerequisites

This has only been tested on MacOS, if you're a Java wizard, you may be able to get it working on other platforms

- Install [Processing](https://processing.org/)
- Install Processing Video library: **Sketch** → **Import Library** → **Add Library**
- Java JDK 1.8 (included with Processing on MacOS)
- Maven (if not using IntelliJ / Eclipse)

### Determine Java Home

If you have multiple versions of Java installed, you will need to explicitly set the `$JAVA_HOME` environment variable for any shell you use to build / run this project.

On MacOS, you can list possible Java homes with

```bash
/usr/libexec/java_home -V
```

```txt
Matching Java Virtual Machines (4):
  14.0.1, x86_64:    "AdoptOpenJDK 14" /Library/Java/JavaVirtualMachines/adoptopenjdk-14.jdk/Contents/Home
  12.0.1, x86_64:    "OpenJDK 12.0.1"  /Library/Java/JavaVirtualMachines/openjdk-12.0.1.jdk/Contents/Home
  11.0.2, x86_64:    "Java SE 11.0.2"  /Library/Java/JavaVirtualMachines/jdk-11.0.2.jdk/Contents/Home
  1.8.0_222, x86_64: "AdoptOpenJDK 8"  /Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home
```

In this case, we want to set `JAVA_HOME="/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home"`

### Installing libraries

**With IntelliJ / Eclipse:** build config files provided, your IDE will handle this for you.

**With VScode / Maven:**

To build `pom.xml` with Maven, you will need to install the following 3rd party (not available on mvnrepository) libraries, whose jars are provided in `lib/`:

- [heronarts.lx](https://github.com/heronarts/lx)
- [heronarts.p3lx](https://github.com/heronarts/p3lx)
- heronarts.lxstudio (private)

These jars need to be installed into your local maven repository (e.g. `~/.m2/repository`) manually. The `groupId`, `artifactId` and `version` fields should match what's in `pom.xml`
You can do this with the following commands (exact jar locations could change):

```bash
export PROJ_VERSION="0.2.1-SNAPSHOT"
mvn install:install-file -Dfile=lib/lxstudio-${PROJ_VERSION}-jar-with-dependencies.jar -DgroupId=heronarts -DartifactId=lxstudio -Dversion=${PROJ_VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=lib/lx-${PROJ_VERSION}-jar-with-dependencies.jar -DgroupId=heronarts -DartifactId=lx -Dversion=${PROJ_VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=lib/p3lx-${PROJ_VERSION}-jar-with-dependencies.jar -DgroupId=heronarts -DartifactId=p3lx -Dversion=${PROJ_VERSION} -Dpackaging=jar
```

You can also clone into the source code repositories (where available) and `mvn install` if you want to modify them.

### Compiling

**With IntelliJ / Eclipse:** build config files provided, your IDE will handle this for you.

**With VScode / Maven:**

You can compile this repository with the following commands, you may need to adjust your java home

```bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home"
mvn compiler:compile assembly:single
```

This is provided as a VSCode build task in `.vscode/tasks.json`

### Usage

**With IntelliJ / Eclipse:** Open this repo in your IDE and hit run.

**With VScode / Maven:**

```bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home"
java -cp "target/lxstudio-ide-0.2.1-SNAPSHOT-jar-with-dependencies.jar:lib/processing-3.5.4/core.jar:lib/processing-3.5.4/gluegen-rt.jar:lib/processing-3.5.4/jogl-all.jar" heronarts.lx.app.LXStudioApp
```

This is provided as a VSCode build task in `.vscode/tasks.json`

## LX Studio Notices

**BY DOWNLOADING OR USING THE LX STUDIO SOFTWARE OR ANY PART THEREOF, YOU AGREE TO THE TERMS AND CONDITIONS OF THE [LX STUDIO SOFTWARE LICENSE AND DISTRIBUTION AGREEMENT](http://lx.studio/license).**

Please note that LX Studio is not open-source software. The license grants permission to use this software freely in non-commercial applications. Commercial use is subject to a total annual revenue limit of $25K on any and all projects associated with the software. If this licensing is obstructive to your needs or you are unclear as to whether your desired use case is compliant, contact me to discuss proprietary licensing: mark@heronarts.com

---

![LX Studio](https://raw.github.com/heronarts/LXStudio/master/assets/screenshot.jpg)

[LX Studio](http://lx.studio/) is a digital lighting workstation, bringing concepts from digital audio workstations and modular synthesis into the realm of LED lighting control. Generative patterns, interactive inputs, and flexible parameter-driven modulation — a rich environment for lighting composition and performance.

### Getting Started

LX Studio runs using the Processing 3 framework. This version of the project directly embeds those dependencies and may be run from within a Java IDE,
for larger projects in which the Processing IDE is insufficient. The example project here can be run either using the full Processing-based UI,
or alternatively in a headless CLI-only mode.

To get started, clone this repository and import the project into an IDE like Eclipse or IntelliJ. Configuration files for both are readily
available in the repository.

Documentation is available on the [LX Studio Wiki &rarr;](https://github.com/heronarts/LXStudio/wiki)

Consult the [LX Studio API reference &rarr;](http://lx.studio/api/)

### Contact and Collaboration

Building a big cool project? I'm probably interested in hearing about it! Want to solicit some help, request new framework features, or just ask a random question? Open an issue on the project or drop me a line: mark@heronarts.com

---

HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR PURPOSE, WITH RESPECT TO THE SOFTWARE.
