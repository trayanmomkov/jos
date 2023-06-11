# JOS &nbsp;<img src="src/main/resources/jos-icon.png" alt="JOS - N-body Simulation" width="36"/> &nbsp;N-body Simulation in Java

![GitHub repo size](https://img.shields.io/github/repo-size/trayanmomkov/jos)
![License](https://img.shields.io/github/license/trayanmomkov/jos)

**J**ava **O**bjects **S**imulation (**JOS**) is an N-body simulation system written in Java.

It offers **arbitrary precision** (alongside float and double) and three execution modes: CPU, GPU and Auto.

[![JOS - N-body simulation video](resources/JOS-N-Body-Simulation.png)](https://youtu.be/EfCrmT-ZGPs)

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
```json
{
  "properties":{
    "numberOfIterations":0,                       # Zero means no limit
    "secondsPerIteration":0.001,
    "numberOfObjects":2,
    "outputFile":"2022-Feb-07_00-26-53.json.gz",  # Where to save the simulation run
    "saveToFile":true,                            # Whether to save the simulation run
    "saveEveryNthIteration": 100,                 # Will save only every 100th iteration. This way the output file will be much smaller.
    "saveMass": false,                            # Whether to save the mass of the obejcts in the output file.
    "saveVelocity": false,                        # Whether to save the velocity.
    "saveAcceleration": false,                    # Whether to save the acceleration.
    "numberType":"DOUBLE",                        # What type to use for numbers. GPU version works with DOUBLE only
    "interactingLaw":"NEWTON_LAW_OF_GRAVITATION", # Current version (1.0.0) supports only NEWTON_LAW_OF_GRAVITATION
    "precision":32,                               # Numbers precision. Used by BigDecimal and ApFloat
    "realTimeVisualization":true,                 # Whether to visualize during simulation run. If true, runs slower
    "playingSpeed":0,                             # If playingSpeed < 0: every iteration sleep playingSpeed milliseconds.
                                                  # If playingSpeed >= 0: visualize every playingSpeed milliseconds.
    "bounceFromScreenBorders": false,             # If true, objects will bounce from the screen borders.
    "mergeOnCollision": false,
    "coefficientOfRestitution": "0.8",            # Coefficient of restitution. 1: elastic, 0: perfectly inelastic, above 1: objects will gain energy on collision.
    "minimumDistance": "10.0",                    # If distance between objects centers < minimumDistance, minimumDistance will be used on acceleration calculating.,
    "scale": 1.0,                                 # Visual scale/zoom of the scene.
    "backgroundColor": "FFFFFF",                  # Background color of the simulaiton.
    "initialObjects":[
      {
        "id":"0",                                 # Unique id of the object
        "x":-200,                                 # Position of the centre
        "y":0,
        "z":0,
        "velocityX":-4,                           # Metre per second (m/s)
        "velocityY":-2.5,
        "velocityZ":0.0,
        "accelerationX": "-8.003695",             # Current acceleration
        "accelerationY": "34.10966",
        "accelerationZ": "0",
        "mass":1E17,                              # Kilogram (kg)
        "radius":80                               # Metre (m)
        "color":"0000FF",                         # Blue
      },
      {
        "id":"1",
        "x":200,
        "y":0,
        "z":0,
        "velocityX":-1.5,
        "velocityY":3,
        "velocityZ":0.0,
        "accelerationX": "-9.364132",
        "accelerationY": "14.401725",
        "accelerationZ": "0",
        "mass":2E17,
        "radius":70,
        "color":"FF0000",                         # Red
      }
    ]
  }
}
```

## Source code description (for developers)
There is a lot of things which are not finished or implemented (such as viewport navigation),
but the main functionality is there.

For GPU mode, the main
calculations are translated from byte code to OpenCL. This is done by
Aparapi library. This code is located in methods:
 - MoveObjectsLogic(Float|Double).run();
 - ProcessCollisions(Float|Double).run();

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