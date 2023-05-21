# JOS &nbsp;<img src="src/main/resources/jos-icon.png" alt="JOS - N-body Simulation" width="36"/> &nbsp;N-body Simulation in Java

![GitHub repo size](https://img.shields.io/github/repo-size/trayanmomkov/jos)
![License](https://img.shields.io/github/license/trayanmomkov/jos)

**J**ava **O**bjects **S**imulation (**JOS**) is an N-body simulation system written in Java.

It offers **arbitrary precision** (alongside float and double) and three execution modes: CPU, GPU and Auto.

![JOS - N-body simulation](resources/JOS-N-Body-Simulation.png)

## Download
 - SourceForge: **[jos.jar](https://sourceforge.net/projects/jos-n-body/files/jos.jar/download)**
 
## Prerequisites
- JOS should be able to run on every system with **Java 1.8** or later installed, including Linux, Mac OS and Windows.
- If you want to use the GPU execution mode, be sure you have installed the **latest driver for your video card**.
 It should include OpenCL needed for the GPU.

## Sample simulations
[Accretion](samples/accretion.zip)

## Usage
0. Start the application by:
   1. Double-click on the jar file. (Execution permission needed.)
   2. If it doesn't start use the following command in the console: `java -jar jos.jar`
2. Click "**Generate objects**".
3. Click "**Start**".
4. Enjoy!

![JOS - N-body simulation](resources/N-body-simulation-steps.png)

You can run the simulation **without visualization**, for faster computations, and **play it later**.

## Video card compatibility
To use the GPU execution mode you must have OpenCL compatible video card.

List of video cards on which the system is tested:
 - **Intel HD Graphics 530**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **Intel HD Graphics 5500**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: red;">**NO**</span>
 - **Intel HD Graphics 620**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **Intel Iris Plus**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: red;">**NO**</span>
 - **Intel Iris Xe**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: red;">**NO**</span>
 - **Intel UHD 620**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **Intel UHD 630**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA Quadro K1100M**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 960**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 1050**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 1050 Ti**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 1060**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 1070**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 1070 Ti**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 1650**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 1660**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 1660 Ti**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 850M**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 950**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 960**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce GTX 960M**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce MX150**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce MX330**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce MX350**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce MX450**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce RTX 2060**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce RTX 2070**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce RTX 2080**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce RTX 3050**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce RTX 3050 Ti**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce RTX 3060**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce RTX 3060 Ti**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce RTX 3070**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **NVIDIA GeForce RTX 3070 Ti**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **AMD Ellesmere**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **AMD gfx902**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **AMD gfx1010**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>
 - **AMD gfx1031**: Float: <span style="color: green;">**OK**</span>, Double: <span style="color: green;">**OK**</span>

## Description
The application had been initially written in C++ and OpenGL in 2009.

The idea was to have a simulation system which can use different interaction laws to calculate the force emerging between the objects.

It was used with:
 - Coulomb's law
 - Variation of Method of mirror charges between electrically charged spheres
 - Newton's law of universal gravitation

Current version implements only **Newton's law** and is written in Java.

Three types of **number precision** are supported float (single precision), double precision
and arbitrary precision.
For this the application introduces an abstraction for numbers
 which allows you to choose which implementation to use: Java primitive types **float** and **double**,
  or arbitrary precision **[ApFloat](http://www.apfloat.org/apfloat_java/)**.

There are three **execution modes**: CPU, GPU and Auto.
 - CPU uses the main processor and supports all three number precisions.
 - GPU runs on video card. Most decent video cards support OpenCL and at least float.
 - AUTO mode switches from GPU to CPU when the number of objects drops (on merging) below particular threshold.

Current version **does not use Z coordinate** of the objects. This is done
for faster calculations but can be easily changed.

For GUI the application uses **Swing** and **Java 2D Graphics**. Current visualization is 2D only.

**[Aparapi](https://aparapi.com/)** library is used for GPU computations.

You can save/load simulation **properties** using **JSON** format.

You can also save/load the **simulation run** itself and play it later. This is done again in
JSON but the file is **GZipped** to be smaller. You can unzip it with any archiver supporting gzip and read the values for
a particular iteration and object.

### Simulation properties (input) file format
<pre style='color:#1f1c1b;background-color:#ffffff;'>
<b><span style='color:#644a9b;'>{</span></b>
  <span style='color:#0057ae;'>&quot;properties&quot;</span><b><span style='color:#644a9b;'>:{</span></b>
    <span style='color:#0057ae;'>&quot;numberOfIterations&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0</span><b><span style='color:#644a9b;'>,</span></b>                       <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Zero</span> <span style='color:#bf0303;'>means</span> <span style='color:#bf0303;'>no</span> <span style='color:#bf0303;'>limit</span>
    <span style='color:#0057ae;'>&quot;secondsPerIteration&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0.001</span><b><span style='color:#644a9b;'>,</span></b>
    <span style='color:#0057ae;'>&quot;numberOfObjects&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>2</span><b><span style='color:#644a9b;'>,</span></b>
    <span style='color:#0057ae;'>&quot;outputFile&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#bf0303;'>&quot;2022-Feb-07_00-26-53.json.gz&quot;</span><b><span style='color:#644a9b;'>,</span></b>  <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Where</span> <span style='color:#bf0303;'>to</span> <span style='color:#bf0303;'>save</span> <span style='color:#bf0303;'>the</span> <span style='color:#bf0303;'>simulation</span> <span style='color:#bf0303;'>run</span>
    <span style='color:#0057ae;'>&quot;saveToFile&quot;</span><b><span style='color:#644a9b;'>:</span></b><b>true</b><b><span style='color:#644a9b;'>,</span></b>                            <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Whether</span> <span style='color:#bf0303;'>to</span> <span style='color:#bf0303;'>save</span> <span style='color:#bf0303;'>the</span> <span style='color:#bf0303;'>simulation</span> <span style='color:#bf0303;'>run</span>
    <span style='color:#0057ae;'>&quot;saveEveryNthIteration&quot;</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#b08000;'>100</span><b><span style='color:#644a9b;'>,</span></b>                 <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Will</span> <span style='color:#bf0303;'>save</span> <span style='color:#bf0303;'>only</span> <span style='color:#bf0303;'>every</span> <span style='color:#bf0303;'>100th</span> <span style='color:#bf0303;'>iteration.</span> <span style='color:#bf0303;'>This</span> <span style='color:#bf0303;'>way</span> <span style='color:#bf0303;'>the</span> <span style='color:#bf0303;'>output</span> <span style='color:#bf0303;'>file</span> <span style='color:#bf0303;'>will</span> <span style='color:#bf0303;'>be</span> <span style='color:#bf0303;'>much</span> <span style='color:#bf0303;'>smaller.</span>
    <span style='color:#0057ae;'>&quot;saveMass&quot;</span><b><span style='color:#644a9b;'>:</span></b> <b>false</b><b><span style='color:#644a9b;'>,</span></b>                            <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Whether</span> <span style='color:#bf0303;'>to</span> <span style='color:#bf0303;'>save</span> <span style='color:#bf0303;'>the</span> <span style='color:#bf0303;'>mass</span> <span style='color:#bf0303;'>of</span> <span style='color:#bf0303;'>the</span> <span style='color:#bf0303;'>obejcts</span> <span style='color:#bf0303;'>in</span> <span style='color:#bf0303;'>the</span> <span style='color:#bf0303;'>output</span> <span style='color:#bf0303;'>file.</span>
    <span style='color:#0057ae;'>&quot;saveVelocity&quot;</span><b><span style='color:#644a9b;'>:</span></b> <b>false</b><b><span style='color:#644a9b;'>,</span></b>                        <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Whether</span> <span style='color:#bf0303;'>to</span> <span style='color:#bf0303;'>save</span> <span style='color:#bf0303;'>the</span> <span style='color:#bf0303;'>velocity.</span>
    <span style='color:#0057ae;'>&quot;saveAcceleration&quot;</span><b><span style='color:#644a9b;'>:</span></b> <b>false</b><b><span style='color:#644a9b;'>,</span></b>                    <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Whether</span> <span style='color:#bf0303;'>to</span> <span style='color:#bf0303;'>save</span> <span style='color:#bf0303;'>the</span> <span style='color:#bf0303;'>acceleration.</span>
    <span style='color:#0057ae;'>&quot;numberType&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#bf0303;'>&quot;DOUBLE&quot;</span><b><span style='color:#644a9b;'>,</span></b>                        <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>What</span> <span style='color:#bf0303;'>type</span> <span style='color:#bf0303;'>to</span> <span style='color:#bf0303;'>use</span> <span style='color:#bf0303;'>for</span> <span style='color:#bf0303;'>numbers.</span> <span style='color:#bf0303;'>GPU</span> <span style='color:#bf0303;'>version</span> <span style='color:#bf0303;'>works</span> <span style='color:#bf0303;'>with</span> <span style='color:#bf0303;'>DOUBLE</span> <span style='color:#bf0303;'>only</span>
    <span style='color:#0057ae;'>&quot;interactingLaw&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#bf0303;'>&quot;NEWTON_LAW_OF_GRAVITATION&quot;</span><b><span style='color:#644a9b;'>,</span></b> <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Current</span> <span style='color:#bf0303;'>version</span> <span style='color:#bf0303;'>(1.0.0)</span> <span style='color:#bf0303;'>supports</span> <span style='color:#bf0303;'>only</span> <span style='color:#bf0303;'>NEWTON_LAW_OF_GRAVITATION</span>
    <span style='color:#0057ae;'>&quot;precision&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>32</span><b><span style='color:#644a9b;'>,</span></b>                               <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Numbers</span> <span style='color:#bf0303;'>precision.</span> <span style='color:#bf0303;'>Used</span> <span style='color:#bf0303;'>by</span> <span style='color:#bf0303;'>BigDecimal</span> <span style='color:#bf0303;'>and</span> <span style='color:#bf0303;'>ApFloat</span>
    <span style='color:#0057ae;'>&quot;realTimeVisualization&quot;</span><b><span style='color:#644a9b;'>:</span></b><b>true</b><b><span style='color:#644a9b;'>,</span></b>                 <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Whether</span> <span style='color:#bf0303;'>to</span> <span style='color:#bf0303;'>visualize</span> <span style='color:#bf0303;'>during</span> <span style='color:#bf0303;'>simulation</span> <span style='color:#bf0303;'>run.</span> <span style='color:#bf0303;'>If</span> <span style='color:#bf0303;'>true</span><b><span style='color:#644a9b;'>,</span></b> <span style='color:#bf0303;'>runs</span> <span style='color:#bf0303;'>slower</span>
    <span style='color:#0057ae;'>&quot;playingSpeed&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0</span><b><span style='color:#644a9b;'>,</span></b>                             <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>If</span> <span style='color:#bf0303;'>playingSpeed</span> <span style='color:#bf0303;'>&lt;</span> <span style='color:#bf0303;'>0</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>every</span> <span style='color:#bf0303;'>iteration</span> <span style='color:#bf0303;'>sleep</span> <span style='color:#bf0303;'>playingSpeed</span> <span style='color:#bf0303;'>milliseconds.</span>
                                                  <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>If</span> <span style='color:#bf0303;'>playingSpeed</span> <span style='color:#bf0303;'>&gt;=</span> <span style='color:#b08000;'>0</span><span style='color:#bf0303;'>:</span> <span style='color:#bf0303;'>visualize</span> <span style='color:#bf0303;'>every</span> <span style='color:#bf0303;'>playingSpeed</span> <span style='color:#bf0303;'>milliseconds.</span>
    <span style='color:#bf0303;'>&quot;bounceFromScreenBorders&quot;</span><span style='color:#bf0303;'>:</span> <b>false</b><b><span style='color:#644a9b;'>,</span></b>             <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>If</span> <span style='color:#bf0303;'>true</span><b><span style='color:#644a9b;'>,</span></b> <span style='color:#bf0303;'>objects</span> <span style='color:#bf0303;'>will</span> <span style='color:#bf0303;'>bounce</span> <span style='color:#bf0303;'>from</span> <span style='color:#bf0303;'>the</span> <span style='color:#bf0303;'>screen</span> <span style='color:#bf0303;'>borders.</span>
    <span style='color:#0057ae;'>&quot;mergeOnCollision&quot;</span><b><span style='color:#644a9b;'>:</span></b> <b>false</b><b><span style='color:#644a9b;'>,</span></b>
    <span style='color:#0057ae;'>&quot;coefficientOfRestitution&quot;</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>&quot;0.8&quot;</span><b><span style='color:#644a9b;'>,</span></b>            <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Coefficient</span> <span style='color:#bf0303;'>of</span> <span style='color:#bf0303;'>restitution.</span> <span style='color:#bf0303;'>1</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>elastic</span><b><span style='color:#644a9b;'>,</span></b> <span style='color:#bf0303;'>0</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>perfectly</span> <span style='color:#bf0303;'>inelastic</span><b><span style='color:#644a9b;'>,</span></b> <span style='color:#bf0303;'>above</span> <span style='color:#bf0303;'>1</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>objects</span> <span style='color:#bf0303;'>will</span> <span style='color:#bf0303;'>gain</span> <span style='color:#bf0303;'>energy</span> <span style='color:#bf0303;'>on</span> <span style='color:#bf0303;'>collision.</span>
    <span style='color:#0057ae;'>&quot;minimumDistance&quot;</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>&quot;10.0&quot;</span><b><span style='color:#644a9b;'>,</span></b>                    <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>If distance between objects centers < minimumDistance, minimumDistance will be used on acceleration calculation.</span>
    <span style='color:#bf0303;'>&quot;initialObjects&quot;</span><span style='color:#bf0303;'>:</span><b><span style='color:#006e28;'>[</span></b>
      <b><span style='color:#644a9b;'>{</span></b>
        <span style='color:#0057ae;'>&quot;id&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#bf0303;'>&quot;0&quot;</span><b><span style='color:#644a9b;'>,</span></b>                                 <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Unique</span> <span style='color:#bf0303;'>id</span> <span style='color:#bf0303;'>of</span> <span style='color:#bf0303;'>the</span> <span style='color:#bf0303;'>object</span>
        <span style='color:#0057ae;'>&quot;x&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>-200</span><b><span style='color:#644a9b;'>,</span></b>                                 <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Position</span> <span style='color:#bf0303;'>of</span> <span style='color:#bf0303;'>the</span> <span style='color:#bf0303;'>centre</span>
        <span style='color:#0057ae;'>&quot;y&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;z&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;velocityX&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>-4</span><b><span style='color:#644a9b;'>,</span></b>                           <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Metre</span> <span style='color:#bf0303;'>per</span> <span style='color:#bf0303;'>second</span> <span style='color:#bf0303;'>(m/s)</span>
        <span style='color:#0057ae;'>&quot;velocityY&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>-2.5</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;velocityZ&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0.0</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;accelerationX&quot;</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>&quot;-8.003695&quot;</span><b><span style='color:#644a9b;'>,</span></b>             <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Current</span> <span style='color:#bf0303;'>acceleration</span>
        <span style='color:#0057ae;'>&quot;accelerationY&quot;</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>&quot;34.10966&quot;</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;accelerationZ&quot;</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>&quot;0&quot;</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;mass&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>1E17</span><b><span style='color:#644a9b;'>,</span></b>                              <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Kilogram</span> <span style='color:#bf0303;'>(kg)</span>
        <span style='color:#0057ae;'>&quot;radius&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>80</span>                               <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Metre</span> <span style='color:#bf0303;'>(m)</span>
        <span style='color:#bf0303;'>&quot;color&quot;</span><span style='color:#bf0303;'>:</span><span style='color:#bf0303;'>&quot;0000FF&quot;</span><b><span style='color:#644a9b;'>,</span></b>                         <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Blue</span>
      <b><span style='color:#644a9b;'>}</span></b><b><span style='color:#006e28;'>,</span></b>
      <b><span style='color:#644a9b;'>{</span></b>
        <span style='color:#0057ae;'>&quot;id&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#bf0303;'>&quot;1&quot;</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;x&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>200</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;y&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;z&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;velocityX&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>-1.5</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;velocityY&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>3</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;velocityZ&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0.0</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;accelerationX&quot;</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>&quot;-9.364132&quot;</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;accelerationY&quot;</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>&quot;14.401725&quot;</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;accelerationZ&quot;</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>&quot;0&quot;</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;mass&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>2E17</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;radius&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>70</span><b><span style='color:#644a9b;'>,</span></b>
        <span style='color:#0057ae;'>&quot;color&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#bf0303;'>&quot;FF0000&quot;</span><b><span style='color:#644a9b;'>,</span></b>                         <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Red</span>
      <b><span style='color:#644a9b;'>}</span></b>
    <b><span style='color:#006e28;'>]</span></b>
  <b><span style='color:#644a9b;'>}</span></b>
<b><span style='color:#644a9b;'>}</span></b>
</pre>

## Source code description (for developers)
There is a lot of things which are not finished or implemented (such as viewport navigation),
but the main functionality is there.

For GPU mode, the main
calculations are translated from byte code to OpenCL. This is done by
Aparapi library. This code is located in methods:
 - SimulationLogicImpl.**calculateNewValues**(int i);
 - CollisionCheck.**run**();

!!! DO NOT CHANGE THESE METHODS and methods called from them if you don't have experience with Aparapi library!!!

Even adding a simple **break;** statement will cause translation failure and the code will execute on the CPU. Read more here: 
[Aparapi Java Kernel Guidelines](https://aparapi.com/documentation/kernel-guidelines.html).

There is **Number** interface which almost entirely duplicates BigDecimal methods signatures.
 I've written it in 2015, but I was
not aware of java.lang.Number which was introduced a year earlier I think. 
However java.lang.Number is very simple and doesn't help in our case.

The interface has three implementations:
 - FloatNumberImpl
 - DoubleNumberImpl
 - ApfloatNumberImpl

I'll not explain the parts of the system in details here. Instead, let me
give you some simple steps for **compiling and running** the project.

JOS is a **maven** project. I've used **Java Swing** and IntelliJ GUI builder for the GUI.
You should be able to import it as a maven project in your favorite IDE.
 - To run it from source code you can use: `mvn clean compile exec:java`
 - To package as a single JAR file with dependencies use:
   - `mvn clean install`
   - `mvn assembly:single`
 - You can run the JAR with: `java -jar jos.jar`

## Contributing to JOS
### TODO List
- Implement simulation generator using formula for placing objects in more
complex structures or create GUI for that.
- Implement tests.
- Fix viewport navigation during visualization.
- Implement video recording of the simulation.
- Implement 3D visualization.

 If you can implement anything from the TODO list, or you want to fix a bug
 you are welcome to contribute.
   
To do that, follow these steps:

1. Fork this repository.
2. Create a branch: `git checkout -b <branch_name>`.
3. Make your changes and commit them: `git commit -m '<commit_message>'`
4. Push to the original branch: `git push origin <project_name>/<location>`
5. Create a pull request.

Alternatively see the GitHub documentation on [creating a pull request](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request).

## Contact
If you find an error, something looks incorrect or just have a suggestion please write me.

![Image](resources/a.png)

Trayan Momkov

## License
[Apache License 2.0](LICENSE)