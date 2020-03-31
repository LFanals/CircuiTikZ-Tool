/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuitikztool;

import javax.swing.JFrame;

public class CircuitikzTool extends JFrame{

    static public CircuitikzTool ct = new CircuitikzTool();
    static public GUI ui = new GUI();

    public CircuitikzTool() {

    }

    public static void main(String[] args) {
        ui.setTitle("CircuiTikz Tool Ver 1.0.0");
        ui.setLocationRelativeTo(null);
        ui.setVisible(true);
        ui.setFocusable(true);
        ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        while (true) {
//            ui.schematicWindow.repaint();
            ui.repaintCircuitMaker();
         //   CircuitMaker.setCurrentTool(ui.getCurrentToolSelected());
        }
    }

}
