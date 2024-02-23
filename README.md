END OF LIFECYCLE NOTICE
===

LX Studio has been superceded by the [Chromatik](https://chromatik.co/) Digital Lighting Workstation, which uses the same underlying [LX](https://github.com/heronarts/LX) libraries with a new, modern UI layer. [Chromatik](https://chromatik.co/) continues in the tradition of LX Studio as an open and extensible framework for custom development. It is available for macOS, Windows, and Linux. More information can be found on the website.

---

**BY DOWNLOADING OR USING THE LX STUDIO SOFTWARE OR ANY PART THEREOF, YOU AGREE TO THE TERMS AND CONDITIONS OF THE [LX STUDIO SOFTWARE LICENSE AND DISTRIBUTION AGREEMENT](http://lx.studio/license).**

Please note that LX Studio is not open-source software. The license grants permission to use this software freely in non-commercial applications. Commercial use is subject to a total annual revenue limit of $25K on any and all projects associated with the software. If this licensing is obstructive to your needs or you are unclear as to whether your desired use case is compliant, contact me to discuss proprietary licensing: mark@heronarts.com

---

![LX Studio](https://raw.github.com/heronarts/LXStudio/master/assets/screenshot.jpg)

[LX Studio](http://lx.studio/) is a digital lighting workstation, bringing concepts from digital audio workstations and modular synthesis into the realm of LED lighting control. Generative patterns, interactive inputs, and flexible parameter-driven modulation — a rich environment for lighting composition and performance.

### Getting Started ###

LX Studio runs using the Processing 4 framework. This version of the project directly embeds those dependencies and may be run from within a Java IDE,
for larger projects in which the Processing IDE is insufficient. The example project here can be run either using the full Processing-based UI,
or alternatively in a headless CLI-only mode.

To get started, clone this repository and import the project into an IDE like Eclipse or IntelliJ. Configuration files for both are readily
available in the repository.

Documentation is available on the [LX Studio Wiki &rarr;](https://github.com/heronarts/LXStudio/wiki)

Consult the [LX Studio API reference &rarr;](http://lx.studio/api/)

### Configure Your Runtime ###

Processing 4.0.1 runs on [Eclipse Temurin 17 (17.0.2+8)](https://adoptium.net/). It is highly recommend to use this JDK for consistency.

The core Processing libraries are not available in Maven central. The first time you setup your project, you must manually run `mvn validate` a single time to install the Processing runtime libraries into your local Maven repository. This is a one-time only step.

Running the project requires passing `-Djava.library.path=lib/processing-4.0.1/native` explicitly to the `java` command. Note that `native` is a symlink within the `processing-4.0.1` folder which should be pointed at the appropriate target platform folder.

This is pre-configured in the Eclipse launch configuration `LXStudioApp.launch`

If you change platforms, update the symlink using one of the following:
```
$ cd lib/processing-4.0.1
$ ln -hsf linux-aarch64 native
$ ln -hsf linux-amd64 native
$ ln -hsf linux-arm native
$ ln -hsf macos-aarch64 native
$ ln -hsf macos-x86_64 native
$ ln -hsf windows-amd64 native
```

On Windows, this command may be one of the following:
```
mklink /d native windows-amd64 (in CMD)
cmd /c mklink /d native windows-amd64 (in PowerShell)
```

### Contact and Collaboration ###

Building a big cool project? I'm probably interested in hearing about it! Want to solicit some help, request new framework features, or just ask a random question? Open an issue on the project or drop me a line: mark@heronarts.com

---

HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR PURPOSE, WITH RESPECT TO THE SOFTWARE.
