###v.3.2.1 - 2026-Nov-10
- Fix fast objects passing through each other when they cross within one iteration.
- Warn when an object moves more than its radius in one iteration.
- Stop orbiting objects from gaining energy: the velocity is now changed before the objects are moved.
- Fix crash on start when the number of objects differs from the number of the loaded objects.
- Fix invalid JSON at the end of a stopped or endless simulation run.
- Highlight the "Generate objects" button only when the objects have to be generated again.
- Keep the fields used only for running disabled while playing a saved simulation.

###v.3.2.0 - 2026-Jun-11
- Fix objects sticking together and losing speed on inelastic collisions.
- Conserve momentum exactly when one object collides with several others simultaneously.
- Push overlapping objects apart (de-penetration) instead of leaving them interpenetrated.
- Place the merged object at the common center of mass.
- Fix mass duplication and loss when three or more objects merge in the same iteration.
- Fix crash on switching from GPU to CPU in AUTO mode when merging is off.
- Implement many missing tests (collisions, gravity, movement) and re-enable disabled ones.

###v.3.1.0 - 2023-Jun-20
- Fix scaling.
- Improve visualization.
- Add option to change the background color.
- Speed up initialization.
- Do not overwrite existing file when saving properties.
- Add checkbox to control console auto-scrolling.
- Add checkbox for rounding number of objects.
- Print simulation time in the console.
- Add functionallity to skip iterations on playing.

###v.3.0.0 - 2023-May-21
- Dramatically improve performance by moving collision processing on GPU.
- Reorder calculation of accelerations, velocities and moving objects.
- Add minimum distance when calculate acc.
- Unify CPU and GPU implementations.
- Workaround Aparapi/OpenCL bug for bigger than 256 local size.
- Start using arrays for data structure instead of Set and List.
- Improve complex object generator. Use mass to calculate color on merging.
- GUI improvements.
- Refactoring

###v.2.0.0 - 2022-May-20
 - Elastic and partially inelastic collisions added.
 - Auto execution mode added. It dynamically switches between GPU and CPU. 
 - BigDecimal implementation removed.
 - Float number type implemented. It is the faster one on bath CPU and GPU.
 - Acceleration can be saved now in the output file.
 - Calculating average file size.
 - Collision detection optimization.
 - UI improvements.
 - Output file changed. It can be much smaller now.
 - Fixed timing/order of calculating velocity and acceleration.
 - Fixed Pi and gravitational constant precision.
 - Object generator improvements.
 - Major redesign and refactoring.
 - GPU and CPU branches merged.
 - Other minor bug fixes and improvements.

###v.1.2.0 - 2022-Mar-28
 - Pause button.
 - Ability to switch trajectories with 'T' key.
 - GPU compatibility check.
 - Bugfixes.