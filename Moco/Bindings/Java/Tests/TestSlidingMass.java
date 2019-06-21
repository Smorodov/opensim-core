/* -------------------------------------------------------------------------- *
 * OpenSim Moco: TestSlidingMass.java                                         *
 * -------------------------------------------------------------------------- *
 * Copyright (c) 2017 Stanford University and the Authors                     *
 *                                                                            *
 * Author(s): Christopher Dembia                                              *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may    *
 * not use this file except in compliance with the License. You may obtain a  *
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0          *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 * -------------------------------------------------------------------------- */
import org.opensim.modeling.*;

class TestSlidingMass {

  public static Model createSlidingMassModel() {
    Model model = new Model();
    model.setName("sliding_mass");
    model.set_gravity(new Vec3(0, 0, 0));
    Body body = new Body("body", 2.0, new Vec3(0), new Inertia(0));
    model.addComponent(body);

    // Allows translation along x.
    SliderJoint joint = new SliderJoint("slider", model.getGround(), body);
    Coordinate coord = joint.updCoordinate();
    coord.setName("position");
    model.addComponent(joint);

    CoordinateActuator actu = new CoordinateActuator();
    actu.setCoordinate(coord);
    actu.setName("actuator");
    actu.setOptimalForce(1);
    model.addComponent(actu);

    model.finalizeConnections();

    return model;
  }

  public static void testSlidingMass() throws Exception {

    MocoStudy moco = new MocoStudy();
    moco.setName("sliding_mass");

    // Define the optimal control problem.
    // ===================================
    MocoProblem mp = moco.updProblem();

    // Model (dynamics).
    // -----------------
    mp.setModel(createSlidingMassModel());

    // Bounds.
    // -------
    // Initial time must be 0, final time can be within [0, 5].
    mp.setTimeBounds(new MocoInitialBounds(0.), new MocoFinalBounds(0., 5.));

    // Initial position must be 0, final position must be 1.
    mp.setStateInfo("/slider/position/value", new MocoBounds(-5, 5),
        new MocoInitialBounds(0), new MocoFinalBounds(1));
    // Initial and final speed must be 0. Use compact syntax.
    mp.setStateInfo("/slider/position/speed", new double[]{-50, 50},
        new double[]{0}, new double[]{0});

    // Applied force must be between -50 and 50.
    mp.setControlInfo("/actuator", new MocoBounds(-50, 50));

    // Cost.
    // -----
    MocoFinalTimeCost ftCost = new MocoFinalTimeCost();
    mp.addCost(ftCost);

    // Configure the solver.
    // =====================
    MocoTropterSolver ms = moco.initTropterSolver();
    ms.set_num_mesh_points(100);

    // Now that we've finished setting up the tool, print it to a file.
    moco.print("sliding_mass.omoco");

    // Solve the problem.
    // ==================
    MocoSolution solution = moco.solve();

    solution.write("sliding_mass_solution.sto");
  }
  public static void main(String[] args) {
    try {
      testSlidingMass();
      System.out.println("Test finished!");
    } catch (Exception e) {
      System.out.println("Exception: " + e);
      System.exit(1);
    }
  }
}