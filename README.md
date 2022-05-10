# Flavius LEDPortal

This is a fork of the excellent [LX Studio](http://lx.studio/) by Heronarts, Please read the [copyright and licensing notices](#lx-studio-notices) at the bottom of this readme.

[![Telecortex-FractalShark](img/Telecortex-FractalShark.gif)](https://www.instagram.com/p/CbS3AbflKWw)

[![LedPortal Want It](img/ledportal_want_it.gif)](https://www.instagram.com/p/CJTP5TdHwyo)

[![LEDJing at SolidState](img/Solid%20State%20LeapMotion.gif)](https://www.youtube.com/watch?v=Ui-maztzuMk)

![Render Compromise Side](img/render_compromise_side.png)![Render Compromise Front](img/render_compromise_front.png)

LEDPortal is an interactive LED art project started by the Flavius theme camp for Burning Seed in 2020.

Some videos of development progress:

- <https://www.instagram.com/p/CI5hzQbnzGE>
- <https://www.instagram.com/p/CC-x_NOnNkP>
- <https://www.instagram.com/p/CBuyp0GHbRQ>

## Running LedPortal

There are two ways to do this, you can bring your own Java JDK or use the JDK bundled with Processing. You should try with Processing Java first. You will also need to ensure you are using the correct Java JDK by setting the `JAVA_HOME` environment variable.

### Prerequisites

- Maven (if not using IntelliJ / Eclipse)
- Java JDK 17 preferably from [Adoptium](https://adoptium.net/)

#### Adoptium Ubuntu Install

```bash
wget "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.3%2B7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.3_7.tar.gz"
sudo tar zxvf OpenJDK17U-jdk_x64_linux_hotspot_17.0.3_7.tar.gz -C /usr/lib/jvm
```

#### Set Java Home

If you have multiple versions of Java installed, you will need to explicitly set the `JAVA_HOME` environment variable for any shell you use to build / run this project.

#### on MacOS

You can list possible Java homes with

```bash
/usr/libexec/java_home -V
```

```txt
...
17.0.3, x86_64:     "OpenJDK 17.0.3"        /Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
...
```

and set your JAVA_HOME to use JDK 11 with

```bash
export JAVA_HOME="`/usr/libexec/java_home -v 17`"
```

#### Setting Java Home on Ubuntu

```bash
sudo update-alternatives --install "/usr/bin/java" "java" "/usr/lib/jvm/jdk-17.0.3+7/bin/java" 1
sudo update-alternatives --set "java" "/usr/lib/jvm/jdk-17.0.3+7/bin/java"
```

#### Validating

You can ensure your shell has the correct version set by running

```bash
java -version
```

you should see

```txt
openjdk version "17.0.3" 2022-04-19
OpenJDK Runtime Environment Temurin-17.0.3+7 (build 17.0.3+7)
OpenJDK 64-Bit Server VM Temurin-17.0.3+7 (build 17.0.3+7, mixed mode, sharing)
```

## Using IntelliJ / Eclipse

The build config files have been provided for IntelliJ and Eclipse, so your IDE should will handle dependency resolution and building for you, however if you want to have the versatility of VSCode with maven, then you will need to see the next sections for how to install those dependencies

## Using VSCode

LEDPortal is based on LXStudio, which in turn is based on Processing. Additionally, some animations require external dependencies like video or gif libraries. The configurations provided in `.settings/` and `LXStudioApp.launch` should allow you to build and run LEDPortal using the libraries provided in this repository under `lib/`.

## Debugging

### Vanilla LXStudio

if you're having issues with LEDPortal-IDE, it may be an upstream issue. Try getting LXStudio-IDE working first.

### Processing Libraries

If a library doesn't work or goes out of date, you can also try installing the library through the Processing library interface (**Sketch** → **Import Library** → **Add Library**), or downloading it manually, then test that it works by running one of the examples that the library provides.

### Serial ports not working on Ubuntu

Try adding the user to the `tty` and `dialout` groups

```bash
sudo usermod -a -G tty $USER
sudo usermod -a -G dialout $USER
sudo reboot now
```

Create a new Processing 3 sketch and test your serial port is detected

```java
import processing.serial.*;

void setup()  {
  // print a list of the serial ports:
  printArray(Serial.list());
}
```

#### Video not working on Ubuntu

Open the Processing IDE, and go **file -> examples -> video -> movile -> loop**. Run the sketch to test your gstreamer install

If that doesn't work, ensure you have gstreamer libaries installed

```bash
sudo apt install gstreamer1.0-x libgstreamer-plugins-base1.0-dev libgstreamer-plugins-good1.0-dev
```

do not install plugins-bad!!

you may also need some codecs to play certain videos

```bash
sudo apt-get install ubuntu-restricted-extras ffmpeg vlc
```

install processing from source

```bash
git clone
cd processing
```

### Video not working on Windows

get the appropriate gstreamer installer from one of these locations:

- <https://gstreamer.freedesktop.org/data/pkg/windows/1.4.0/gstreamer-1.0-x86_64-1.4.0.msi>
- <https://gstreamer.freedesktop.org/data/pkg/windows/1.8.0/gstreamer-1.0-x86_64-1.8.0.msi>
- <https://gstreamer.freedesktop.org/data/pkg/windows/1.4.0/gstreamer-1.0-x86-1.4.0.msi>

You may need to also add an entry "C:\gstreamer\1.0\x86_64\bin" to the PATH environment variable <https://github.com/gstreamer-java/gst1-java-core/issues/15>

## LX Studio Notices

**BY DOWNLOADING OR USING THE LX STUDIO SOFTWARE OR ANY PART THEREOF, YOU AGREE TO THE TERMS AND CONDITIONS OF THE [LX STUDIO SOFTWARE LICENSE AND DISTRIBUTION AGREEMENT](http://lx.studio/license).**

Please note that LX Studio is not open-source software. The license grants permission to use this software freely in non-commercial applications. Commercial use is subject to a total annual revenue limit of $25K on any and all projects associated with the software. If this licensing is obstructive to your needs or you are unclear as to whether your desired use case is compliant, contact me to discuss proprietary licensing: mark@heronarts.com

---

![LX Studio](https://raw.github.com/heronarts/LXStudio/master/assets/screenshot.jpg)

[LX Studio](http://lx.studio/) is a digital lighting workstation, bringing concepts from digital audio workstations and modular synthesis into the realm of LED lighting control. Generative patterns, interactive inputs, and flexible parameter-driven modulation — a rich environment for lighting composition and performance.

### Getting Started

LX Studio runs using the Processing 4 framework. This version of the project directly embeds those dependencies and may be run from within a Java IDE,
for larger projects in which the Processing IDE is insufficient. The example project here can be run either using the full Processing-based UI,
or alternatively in a headless CLI-only mode.

To get started, clone this repository and import the project into an IDE like Eclipse or IntelliJ. Configuration files for both are readily
available in the repository.

Documentation is available on the [LX Studio Wiki &rarr;](https://github.com/heronarts/LXStudio/wiki)

Consult the [LX Studio API reference &rarr;](http://lx.studio/api/)

### Configure Your Runtime

Processing 4.0 beta 8 runs on [Eclipse Temurin 17 (17.0.2+8)](https://adoptium.net/). It is highly recommend to use this JDK for consistency.

Running the project requires passing `-Djava.library.path=lib/processing-4.0b8/native` explicitly to the `java` command. Note that `native` is a symlink within the `processing-4.0b8` folder which should be pointed at the appropriate target platform folder.

This is pre-configured in the Eclipse launch configuration `LXStudioApp.launch`

If you change platforms, update the symlink using one of the following:

```bash
ln -hsf lib/processing-4.0b8/linux-aarch64 lib/processing-4.0b8/native
ln -hsf lib/processing-4.0b8/linux-amd64 lib/processing-4.0b8/native
ln -hsf lib/processing-4.0b8/linux-arm lib/processing-4.0b8/native
ln -hsf lib/processing-4.0b8/macos-aarch64 lib/processing-4.0b8/native
ln -hsf lib/processing-4.0b8/macos-x86_64 lib/processing-4.0b8/native
ln -hsf lib/processing-4.0b8/windows-amd64 lib/processing-4.0b8/native
```

or on linux:

```bash
mkdir natives
cd natives
ln -s ../lib/processing-4.0b8/linux-amd64 linux-amd64
```

or the equivalent `mklnk` command in cmd.exe on Windows:

```powershell
mkdir natives
cd natives
cmd
  mklink windows-amd64 ..\lib\processing-4.0b8\windows-amd64
```

or for some versions of macos:

```bash
mkdir natives
cd natives
ln -s ../lib/processing-4.0b8/macos-x86_64 macosx-universal
```

### Contact and Collaboration

Building a big cool project? I'm probably interested in hearing about it! Want to solicit some help, request new framework features, or just ask a random question? Open an issue on the project or drop me a line: mark@heronarts.com

---

HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR PURPOSE, WITH RESPECT TO THE SOFTWARE.
