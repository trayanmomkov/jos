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