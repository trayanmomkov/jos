# JOS &nbsp;<img src="src/main/resources/jos-icon.png" alt="JOS - N-body Simulation" width="36"/> &nbsp;N-body Simulation in Java

![GitHub repo size](https://img.shields.io/github/repo-size/trayanmomkov/jos)
![License](https://img.shields.io/github/license/trayanmomkov/jos)

**J**ava **O**bjects **S**imulation (**JOS**) is an N-body simulation system written in Java.

![JOS - N-body simulation](resources/JOS-N-Body-Simulation.png)

## Download
 - GPU version: **[jos.jar](https://sourceforge.net/projects/jos-n-body/files/jos.jar/download)** - Faster. Main calculations run on video card. Limited precision. Compatible video card required.
 - CPU version: **[jos-cpu.jar](https://sourceforge.net/projects/jos-n-body/files/jos-cpu.jar/download)** - Slower. Runs on CPU. Arbitrary precision.
 
## Prerequisites
- JOS should run on every system with **Java 8** or later installed, including Linux, Mac OS and Windows.
- For GPU version, be sure you have installed **latest driver for your video card**. It should include OpenCL needed for the GPU.

## Usage
0. Start the application by:
   1. Double-click on the jar file.
   2. If it doesn't start use the following command in the console: `java -jar jos.jar`
2. Click "**Generate objects**".
3. Click "**Start**".
4. Enjoy!

![JOS - N-body simulation](resources/N-body-simulation-steps.png)

You can run the simulation **without visualization**, for faster computations, and **play it later**.

## Video card compatibility
GPU version **jos.jar** does not run on every video card.
 If you experience any problem, please try the CPU version: [jos-cpu.jar](https://sourceforge.net/projects/jos-n-body/files/jos-cpu.jar/download).

Please help me to create a list with compatible video cards. Tell me what is yours
 and whether there is a problem or everything is running fine.
 
If you see the following message when you start a simulation:
`Simulation logic execution mode = GPU` then everything is fine.

List of video cards on which the system is tested:
 - **NVIDIA Quadro K1100M**: <span style="color: green;">**OK**</span>
 - **Intel UHD Graphics 620** (Whiskey Lake): <span style="color: red;">**PROBLEMS**</span>

## Description
The application had been initially written in C++ and OpenGL in 2009.

The idea was to have a simulation system which can use different interaction laws to calculate the force emerging between the objects.

It was used with:
 - Coulomb's law
 - Variation of Method of mirror charges between electrically charged spheres
 - Newton's law of universal gravitation

Current version implements only **Newton's law** and is written in Java.

For GUI it uses **Swing** and **Java 2D Graphics**.

GPU version (master branch) **does not use Z coordinate** of the objects. This is done
for faster calculations but can be easily changed.

CPU version (arbitrary_precision branch) **use Z coordinate**.

Current visualization is 2D only.

**[Aparapi](https://aparapi.com/)** library is used for GPU computations.

The version which is running only on the CPU (jos-cpu.jar) introduces an abstraction for numbers
 which allows you to choose which implementation to use: primitive type **double**,
  common **BigDecimal** or arbitrary precision **[ApFloat](http://www.apfloat.org/apfloat_java/)**.

If you need precise numbers try ApFloat. My experience shows that it 
is faster than BigDecimal.

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
      <span style='color:#0057ae;'>&quot;numberType&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#bf0303;'>&quot;DOUBLE&quot;</span><b><span style='color:#644a9b;'>,</span></b>                        <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>What</span> <span style='color:#bf0303;'>type</span> <span style='color:#bf0303;'>to</span> <span style='color:#bf0303;'>use</span> <span style='color:#bf0303;'>for</span> <span style='color:#bf0303;'>numbers.</span> <span style='color:#bf0303;'>GPU</span> <span style='color:#bf0303;'>version</span> <span style='color:#bf0303;'>works</span> <span style='color:#bf0303;'>with</span> <span style='color:#bf0303;'>DOUBLE</span> <span style='color:#bf0303;'>only</span>
      <span style='color:#0057ae;'>&quot;interactingLaw&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#bf0303;'>&quot;NEWTON_LAW_OF_GRAVITATION&quot;</span><b><span style='color:#644a9b;'>,</span></b> <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Current</span> <span style='color:#bf0303;'>version</span> <span style='color:#bf0303;'>(1.0.0)</span> <span style='color:#bf0303;'>supports</span> <span style='color:#bf0303;'>only</span> <span style='color:#bf0303;'>NEWTON_LAW_OF_GRAVITATION</span>
      <span style='color:#0057ae;'>&quot;precision&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>32</span><b><span style='color:#644a9b;'>,</span></b>                               <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Numbers</span> <span style='color:#bf0303;'>precision.</span> <span style='color:#bf0303;'>Used</span> <span style='color:#bf0303;'>by</span> <span style='color:#bf0303;'>BigDecimal</span> <span style='color:#bf0303;'>and</span> <span style='color:#bf0303;'>ApFloat</span>
      <span style='color:#0057ae;'>&quot;scale&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>16</span><b><span style='color:#644a9b;'>,</span></b>                                   <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Only</span> <span style='color:#bf0303;'>used</span> <span style='color:#bf0303;'>by</span> <span style='color:#bf0303;'>BigDecimal</span>
      <span style='color:#0057ae;'>&quot;realTimeVisualization&quot;</span><b><span style='color:#644a9b;'>:</span></b><b>true</b><b><span style='color:#644a9b;'>,</span></b>                 <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Whether</span> <span style='color:#bf0303;'>to</span> <span style='color:#bf0303;'>visualize</span> <span style='color:#bf0303;'>during</span> <span style='color:#bf0303;'>simulation</span> <span style='color:#bf0303;'>run.</span> <span style='color:#bf0303;'>If</span> <span style='color:#bf0303;'>true</span><b><span style='color:#644a9b;'>,</span></b> <span style='color:#bf0303;'>runs</span> <span style='color:#bf0303;'>slower</span>
      <span style='color:#0057ae;'>&quot;playingSpeed&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>33</span><b><span style='color:#644a9b;'>,</span></b>                            <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>If</span> <span style='color:#bf0303;'>playingSpeed</span> <span style='color:#bf0303;'>&lt;</span> <span style='color:#bf0303;'>0</span><b><span style='color:#644a9b;'>:</span></b> <span style='color:#bf0303;'>every</span> <span style='color:#bf0303;'>iteration</span> <span style='color:#bf0303;'>sleep</span> <span style='color:#bf0303;'>playingSpeed</span> <span style='color:#bf0303;'>milliseconds</span>
                                                    <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>If</span> <span style='color:#bf0303;'>playingSpeed</span> <span style='color:#bf0303;'>&gt;=</span> <span style='color:#b08000;'>0</span><span style='color:#bf0303;'>:</span> <span style='color:#bf0303;'>visualize</span> <span style='color:#bf0303;'>every</span> <span style='color:#bf0303;'>playingSpeed</span> <span style='color:#bf0303;'>milliseconds</span>
      <span style='color:#bf0303;'>&quot;initialObjects&quot;</span><span style='color:#bf0303;'>:</span><b><span style='color:#006e28;'>[</span></b>
         <b><span style='color:#644a9b;'>{</span></b>
            <span style='color:#0057ae;'>&quot;color&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#bf0303;'>&quot;0000FF&quot;</span><b><span style='color:#644a9b;'>,</span></b>                       <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Blue</span>
            <span style='color:#0057ae;'>&quot;speedY&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>-4</span><b><span style='color:#644a9b;'>,</span></b>                            <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Metre</span> <span style='color:#bf0303;'>per</span> <span style='color:#bf0303;'>second</span> <span style='color:#bf0303;'>(m/s)</span>
            <span style='color:#0057ae;'>&quot;speedX&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>-2.5</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;speedZ&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0.0</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;mass&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>1E17</span><b><span style='color:#644a9b;'>,</span></b>                            <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Kilogram</span> <span style='color:#bf0303;'>(kg)</span>
            <span style='color:#0057ae;'>&quot;x&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>-200</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;y&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;z&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;id&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#bf0303;'>&quot;0&quot;</span><b><span style='color:#644a9b;'>,</span></b>                               <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Unique</span> <span style='color:#bf0303;'>id</span> <span style='color:#bf0303;'>of</span> <span style='color:#bf0303;'>the</span> <span style='color:#bf0303;'>object</span>
            <span style='color:#0057ae;'>&quot;radius&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>80</span>                             <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Metre</span> <span style='color:#bf0303;'>(m)</span>
         <b><span style='color:#644a9b;'>}</span></b><b><span style='color:#006e28;'>,</span></b>
         <b><span style='color:#644a9b;'>{</span></b>
            <span style='color:#0057ae;'>&quot;color&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#bf0303;'>&quot;FF0000&quot;</span><b><span style='color:#644a9b;'>,</span></b>                       <span style='color:#bf0303;'>#</span> <span style='color:#bf0303;'>Red</span>
            <span style='color:#0057ae;'>&quot;speedY&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>-1.5</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;speedX&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>3</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;speedZ&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0.0</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;mass&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>2E17</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;x&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>200</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;y&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;z&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>0</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;id&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#bf0303;'>&quot;1&quot;</span><b><span style='color:#644a9b;'>,</span></b>
            <span style='color:#0057ae;'>&quot;radius&quot;</span><b><span style='color:#644a9b;'>:</span></b><span style='color:#b08000;'>70</span>
         <b><span style='color:#644a9b;'>}</span></b>
      <b><span style='color:#006e28;'>]</span></b>
   <b><span style='color:#644a9b;'>}</span></b>
<b><span style='color:#644a9b;'>}</span></b>
</pre>

## Source code description (for developers)
There is a lot of things which are not finished or implemented (such as viewport navigation),
but the main functionality is there.

The design of the application is not the best one. Especially in **master** branch
where GPU is used for computations. One of the reason is that the main
calculations have to be translated from byte code to OpenCL. This is done by
Aparapi library.

Main calculations translated to OpenCL are in methods:
 - SimulationLogicImpl.**calculateNewValues**(int i);
 - CollisionCheck.**run**();

!!! DO NOT CHANGE THESE METHODS and methods called from them if you don't have experience with Aparapi library!!!

Even adding a simple **break;** statement will cause translation failure and the code will execute on the CPU. Read more here: 
[Aparapi Java Kernel Guidelines](https://aparapi.com/documentation/kernel-guidelines.html).

The branch which uses CPU is called **arbitrary_precision** and as its name
suggests, it offers arbitrary precision for arithmetic calculations.
Design of this branch is better. There is **Number** interface which
has four implementations:
 - FloatNumberImpl
 - DoubleNumberImpl
 - BigDecimalNumberImpl
 - ApfloatNumberImpl

I'll not explain the parts of the system in details here. Instead, let me
give you some simple steps for compiling and running the project.

JOS is **maven** project. I've used **Java Swing** and IntelliJ GUI builder for the GUI.
You should be able to import it as a maven project in your favorite IDE.
 - To run it from source code you can use: `mvn clean compile exec:java`
 - To package as a single JAR file with dependencies use:
   - `mvn clean install`
   - `mvn assembly:single`
 - You can run the JAR with: `java -jar jos.jar`

## Contributing to JOS
### TODO List
- Implement tests.
- Fix viewport navigation during visualization.
- Implement object bouncing from each other using coefficient of restitution.
- Implement video recording of the simulation.
- Implement simulation generator using formula for placing objects in more
complex structures or create GUI for that.
- Implement 3D visualization.

 If you can implement anything from the TODO list or you want to fix a bug
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