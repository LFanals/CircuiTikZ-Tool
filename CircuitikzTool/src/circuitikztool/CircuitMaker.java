/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuitikztool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D; // added
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.geom.Point2D; // 2D added
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;



/**
 * Circuitmaker is an extension of JPanel which allows for the user to draw
 * schematics using the components supplied by the Components class in this
 * project. uses mouse input to draw components with the selected component.
 *
 * @author James
 */
public class CircuitMaker extends JPanel {

    //LaTeX formatting parameters used for generating the final latex output
    boolean wrapInFigure = true;
    boolean americanStyleComponents = true;
    boolean useHMarker = true;

    //GRID_SIZE determines the current zoom level of the schematic window, a lower values indicates zooming out and a larger values indicates zooming in
    static double GRID_SIZE = 50;

    //current mouse position mapped to the schematic grid
    double xGridPosition;
    double yGridPosition;

    //current offset of the origin in the CircuitMaker Panel
    double x_offset = GRID_SIZE * 5;
    double y_offset = GRID_SIZE * 5;

    //booleans indicating whether or not the user is currently holding down the mouse wheel or the left click respectively
    //these are updated by the mouseListeners implemented in the CircuitMaker Constructor
    boolean dragging = false;
    boolean clicking = false;

    //previously polled mouse position, these are used to track user dragging (panning) in the schematic window
    int lastMouseX, lastMouseY;

    //current index selected in the components ArrayList (if a user selects a component in the Components listbox this variable is updated) 
    private int componentIndexSelected = 0;

    //offset of the origin from 0,0 of the draw window, changed when the user pans around the schematic
    Point2D originOffset;

    /*
        ArrayList storing all components placed by the user
     */
    private ArrayList<Component> components;

    //current tool the user is selecting
    static int currentTool = Component.PATH;

    //when a user starts holding left click the starting coordinates of the cursor are stored here, used at the end to place a component. 
    Point2D wireStart;
    // private Point2D wireStart;

    /**
     * sets the current tool of the CircuitMaker, this is the tool that will be
     * used when the user is placing a component, consult the Components class
     * to view which values relate to which tools
     *
     * @param tool current tool to be used by the CircuitMaker window
     */
    public static void setCurrentTool(int tool) {
        currentTool = tool;
    }

    /**
     * sets the selected component index, a component is selected by the user in
     * the UI by clicking on it in the "Components" list box. when a component
     * is selected it needs to be highlighted in the circuitmaker and the UI
     * fields need to be updated with its latexString and label
     *
     * @param index index components ArrayList to be selected
     */
    public void setSelectedComponentIndex(int index) {
        componentIndexSelected = index;
    }

    /**
     * CircuitMaker constructor, initializes the components ArrayList and
     * attaches input listeners to the current window.
     *
     */
    public CircuitMaker() {
        components = new ArrayList(0);

        addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                //user moved mouse so we need to map the cursor to the grid. 
                xGridPosition = 0.5 * (Math.round (e.getX() / (GRID_SIZE/2)));
                yGridPosition = 0.5 * (Math.round (e.getY() / (GRID_SIZE/2)));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                //user moved mouse so we need to map the cursor to the grid. 
                xGridPosition = 0.5 * (Math.round (e.getX() / (GRID_SIZE/2)));
                yGridPosition = 0.5 * (Math.round (e.getY() / (GRID_SIZE/2)));
            }
        }
        );

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                switch (e.getButton()) {
                    case MouseEvent.BUTTON1:
                        //left click
                        clicking = true;
                        //user holding left click on the CircuitMaker means they're going to be placing a path component, we need to store the starting position of the click (mapped to the grid)
                        wireStart = new Point2D.Double(xGridPosition - originOffset.getX(), yGridPosition - originOffset.getY());
                        // wireStart = new Point2D.Double(xGridPosition, yGridPosition);
                        break;
                    case MouseEvent.BUTTON2:
                        //center click
                        dragging = true;

                        //poll the mouse, we use these values to calculate the panning of the CircuitMaker window. 


                        lastMouseX = MouseInfo.getPointerInfo().getLocation().x;
                        lastMouseY = MouseInfo.getPointerInfo().getLocation().y;
                        break;
                    case MouseEvent.BUTTON3:
                        configComponent(getComponentClosestToPointer());
                    default:
                        break;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    //left click
                    clicking = false;

                    //mouse button has been released, place the component the user has just tried to create. 
                    placeComponent();

                    //set the selected component to component just placed. 
                    componentIndexSelected = components.size();
                    CircuitikzTool.ui.updateComponentList(); //this is very bad and we shouldn't do it this way but eh whatever, update the UI component list

                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    //center click
                    //no longer panning the schematic
                    dragging = false;
                }

            }

        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                //we don't cap the user from zooming in, they can make the grid as big as they want
                //however if they make the grid too small it becomes difficult to render anything properly so we cap 
                //the zoom in to a gridsize of 10. if we're at a grid size of 10 we make sure the user can also zoom out
                if (GRID_SIZE > 10 || e.getWheelRotation() < 0) {
                    GRID_SIZE -= e.getWheelRotation(); //very simple zoom method here, could be improved
                }
                //     System.out.println("Grid size is now " + GRID_SIZE);
            }
        });
    }

    private int getComponentClosestToPointer() {
        Point2D position = new Point2D.Double(xGridPosition - originOffset.getX(), yGridPosition - originOffset.getY());

        double shortestDistance = Double.MAX_VALUE;
        int index = -1;

        //for path components we look for the center of the line, non-path have a single position so that makes things easier. 
        for (int a = 0; a < components.size(); a++) {
            double distance = Double.MAX_VALUE;
            if (components.get(a).isPathComponent()) {
                //find center of line
                Point2D center = new Point2D.Double((components.get(a).wireStart.getX() + components.get(a).wireEnd.getX()) / 2, (components.get(a).wireStart.getX() + components.get(a).wireEnd.getY()) / 2);
                distance = Point2D.distance(center.getX(), center.getY(), position.getX(), position.getY());
            } else {
                distance = Point2D.distance(components.get(a).getPosition().getX(), components.get(a).getPosition().getY(), position.getX(), position.getY());
            }
            if (distance < shortestDistance) {
                shortestDistance = distance;
                index = a;
            }
        }
        return index;
    }

    private void configComponent(int componentIndex) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        JOptionPane.showMessageDialog(this, "Selected: " + componentIndex);
        LatexStringBuilder w = new LatexStringBuilder(CircuitikzTool.ui, true, components.get(componentIndex));
        w.setLocationRelativeTo(null);
        w.setVisible(true);
        switch (w.getReturnStatus().componentType) {
            case Component.DELETE:
                components.remove(componentIndex);
                CircuitikzTool.ui.updateComponentList();
                break;
            case Component.CANCEL:
                break;
            default:
                components.set(componentIndex, w.getReturnStatus());
                System.out.println(w.getReturnStatus().getComponentLabel());
                break;
        }
    }

    /**
     * gets the current selected component index
     *
     * @return currently selected component index
     */
    public int getSelectedComponentIndex() {
        return componentIndexSelected;
    }

    /**
     * returns a string array representing each component placed in the
     * CircuitMaker window, different components may have different formatting,
     * thus getting the individual component strings is handled by the Component
     * object.
     *
     * @return String array representing all components placed in the
     * circuitmaker window
     */
    public String[] getComponentList() {
        String[] listItems = new String[components.size()];
        for (int a = 0; a < listItems.length; a++) {
            listItems[a] = components.get(a).getComponentLabelString();
        }
        return listItems;
    }

    /**
     * paint method of the CircuitMaker window, draws the grid, origin, and
     * components to the window. also handles tracking mouse panning (yeah
     * that'll be fixed in a later commit if I don't forget about writing this
     * into the official javadoc)
     *
     * @param g2d graphics object to be drawn onto
     */
     @Override
    public void paint(Graphics g2d) {

        //first figure out what placed component has already been selected (we need to highlight it so the user can interact with it)
        setSelectedComponentIndex(CircuitikzTool.ui.componentList.getSelectedIndex());

        //keep track of the users mouse dragging, adjust the offset accordingly
        if (dragging) {
            x_offset += (MouseInfo.getPointerInfo().getLocation().x - lastMouseX);
            y_offset += (MouseInfo.getPointerInfo().getLocation().y - lastMouseY);
            lastMouseX = MouseInfo.getPointerInfo().getLocation().x;
            lastMouseY = MouseInfo.getPointerInfo().getLocation().y;
        }

        //snap the x and y offset to values of the grid size. this could be made somewhat better with some nicer rounding functions
        double originOffsetX = ((int) (x_offset / GRID_SIZE)) * GRID_SIZE;
        double originOffsetY = ((int) (y_offset / GRID_SIZE)) * GRID_SIZE;

        //offset of the origin (in terms of Circuitikz coordinates) 
        originOffset = new Point2D.Double( (int) (x_offset / GRID_SIZE), (int) (y_offset / GRID_SIZE));

        //fill in the background with the background color
        g2d.setColor(Preferences.backgroundColor);
        g2d.fillRect(0, 0, 10000, 10000);

        //if someone hovers over the origin lets make sure they know that its the origin
        if (originOffsetX == xGridPosition * GRID_SIZE && originOffsetY == yGridPosition * GRID_SIZE) {
            g2d.setColor(Preferences.gridColor);
            g2d.drawString("Origin", (int) (originOffsetX - 10), (int) (originOffsetY - 5));
        }

        //draw the grid
        g2d.setColor(Preferences.gridColor);
        for (int x = 0; x < this.getWidth(); x += GRID_SIZE/2) {
            for (int y = 0; y < this.getHeight(); y += GRID_SIZE/2) {
                g2d.drawLine(x, y, x, y);
            }
        }

        //draw origin
        g2d.setColor(Preferences.selectedColor);
        g2d.fillOval((int) (originOffsetX - 3.0), (int) (originOffsetY - 3.0), 5, 5);

        //draw the current mouse position snapped to the grid
        g2d.setColor(Preferences.componentColor);
        g2d.fillOval( (int) (GRID_SIZE * xGridPosition - 3), (int) (GRID_SIZE * yGridPosition - 3), 5, 5);

        /*
            if the user is holding down left click we assume that they're attempting to place a component. We need to give them 
        some visual feedback, in the case of path components a simple line from where they started to their current position suffices
        for other components draw the component to the users's mouse position mapped to the grid. 
         */
        if (clicking) {
            g2d.setColor(Preferences.componentColor);

            //path components just draw a line from start to the current position
            if (Component.isPathComponent(currentTool)) {
                g2d.drawLine(
                        (int) (GRID_SIZE * (wireStart.getX() + originOffset.getX())),
                        (int) (GRID_SIZE * (wireStart.getY() + originOffset.getY())),
                        (int) (GRID_SIZE * xGridPosition),
                        (int) (GRID_SIZE * yGridPosition));
            } else if (currentTool == Component.VCC_NODE) {
                //for VCC we draw the node at the user's mouse position
                Component.drawVCCNode(g2d,  (GRID_SIZE), xGridPosition, yGridPosition);
            } else if (currentTool == Component.GROUND_NODE) {
                //for GND we draw the node at the user's mouse position
                Component.drawGNDNode(g2d, (GRID_SIZE), xGridPosition, yGridPosition);
            } else if (currentTool == Component.VSS_NODE) {
                //for VSS we draw the node at the user's mouse position
                Component.drawVSSNode(g2d, (GRID_SIZE), xGridPosition, yGridPosition);
            } else if (currentTool == Component.OPAMP_3TERMINAL || currentTool == Component.OPAMP_5TERMINAL) {
                Component.drawOpamp(g2d, (GRID_SIZE), xGridPosition, yGridPosition, false, currentTool);

            } else if (currentTool == Component.BUFFER) {
                Component.drawBuffer(g2d, (GRID_SIZE), xGridPosition, yGridPosition, false);
            } else if (currentTool == Component.FD_OPAMP) {
                Component.drawFDOpAmp(g2d, (GRID_SIZE), xGridPosition, yGridPosition, false);
            } else if (currentTool == Component.GM_AMP) {
                Component.drawGMAmp(g2d, (GRID_SIZE), xGridPosition, yGridPosition, false);
            } else if (currentTool == Component.BLOCK || currentTool == Component.SACDC || currentTool == Component.SACDC) {
                Component.drawBlock(g2d, (GRID_SIZE), xGridPosition, yGridPosition, false);
            } else if (currentTool == Component.MIXER) {
                Component.drawMixer(g2d, (GRID_SIZE), xGridPosition, yGridPosition, false);
            } else if (currentTool == Component.NODE) {
                Component.drawNode(g2d, (GRID_SIZE), xGridPosition, yGridPosition, false);



            } else if (currentTool == Component.TRANSFORMER || currentTool == Component.TRANSFORMER_WITH_CORE) {
                Component.drawTransformer(g2d, (GRID_SIZE), xGridPosition, yGridPosition, false);
            } else {
                //draw the transistor as a preview for the user
                Component.drawTransistor(g2d, (GRID_SIZE), xGridPosition, yGridPosition, false);
            }
        }

        //draw all the components currently placed to the CircuitMaker window
        for (int a = 0; a < components.size(); a++) {
            components.get(a).paint(g2d, (int) (GRID_SIZE), originOffset, a == componentIndexSelected);
        }
    }

    /**
     * sets the component label of the currently selected component in
     * components ArrayList
     *
     * @param text new label of the currently selected component
     */
    public void setSelectedComponentLabel(String text) {
        if (componentIndexSelected >= 0) {
            components.get(componentIndexSelected).setComponentLabel(text);
        } else {
        }
    }

    /**
     * gets the component label of the currently selected component.
     *
     * @return the component label of the currently selected component.
     */
    public String getSelectedComponentLabel() {
        if (componentIndexSelected >= 0) {
            return components.get(componentIndexSelected).getComponentLabel();
        } else {
            return "";
        }
    }

    /**
     * Sets the latex parameter string of the currently selected component in
     * the components ArrayList
     *
     * @param text new latex parameter string for the currently selected
     * component
     */
    public void setSelectedComponentLatexString(String text) {
        if (componentIndexSelected >= 0) {
            components.get(componentIndexSelected).setLatexString(text);
        } else {
        }
    }

    /**
     * returns the latex parameter string of the currently selected component in
     * components ArrayList
     *
     * @return latex parameter of the currently selected component in components
     */
    public String getSelectedComponentLatexString() {
        if (componentIndexSelected >= 0) {
            return components.get(componentIndexSelected).getLatexString();
        } else {
            return "";
        }
    }

    /**
     * places component using the users start position when path component or
     * using the user's current mouse position on the grid when a non-path
     * component is placed
     *
     */
    public void placeComponent() {
        Component c;
        try {
            c = new Component(wireStart, new Point2D.Double(xGridPosition - originOffset.getX(), yGridPosition - originOffset.getY()), currentTool);
        } catch (IllegalArgumentException e) {
            c = new Component(new Point2D.Double(xGridPosition - originOffset.getX(), yGridPosition - originOffset.getY()), currentTool);
        }
        components.add(c);
        setSelectedComponentIndex(components.size() - 1);
        //System.out.println("added component to index " + (components.size() - 1));
    }

    /**
     * deletes the currently selected component (only called when a user presses
     * delete while focused on the components listbox in the UI)
     *
     */
    public void deleteSelectedComponent() {
        try {
            components.remove(componentIndexSelected);
            componentIndexSelected = (componentIndexSelected > 0) ? componentIndexSelected-- : 0;
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    public String getCircuitXML() {
        String ret = "";
        for (int a = 0; a < components.size(); a++) {
            ret += components.get(a).toXML() + "\n";
        }
        return ret;
    }

    public void loadCircuitFromXML(String xml) {
        String[] coms = xml.split("\n");
        components.clear();
        Component.resetStatics();
        for (int a = 0; a < coms.length; a++) {
            components.add(Component.getComponentFromXML(coms[a]));
        }
    }

    /**
     * Converts the entire schematic into a LaTeX figure using circuitikz,
     * individual components generate their own latex line in the Components
     * object.
     *
     * @return latex string representing schematic created in CircuitMaker
     * window
     */
    public String generateLatexString() {
        String output = "";

        //if we're going to wrap the circuitikz in a \figure then we need to add that at the beginning
        //most of the user customizations are kind of a mess since they have to be written in the figure in a 
        //specific order. 
        if (wrapInFigure) {
            output += "\\begin{figure}";
            if (useHMarker) {
                output += "[H]\n";
            } else {
                output += "\n";
            }
            output += "\\centering\n";
            output += "\\begin{circuitikz}[>=latex']";
            if (americanStyleComponents) {
                output += "[american]";
            }
            output += "\n\\tikzstyle{block} = [draw, rectangle, minimum height=1cm, minimum width=2cm]\n";
        } else {
            output += "\\begin{circuitikz}";
            if (useHMarker && americanStyleComponents) {
                output += "[H, american]\n";
            } else if (useHMarker && !americanStyleComponents) {
                output += "[H]\n";
            } else if (!useHMarker && americanStyleComponents) {
                output += "[american]\n";
            } else {
                output += "\n";
            }
        }

        //determine whether we have any mosfets in the placed components, if we do then we need to add some extra formatting 
        boolean containsFet = false;
        for (int a = 0; a < components.size(); a++) {
            if (components.get(a).isFet()) {
                containsFet = true;
                break;
            }
        }

        //eventually these should be changeable by the user through some kind of settings window. 
        if (containsFet) {
            output += "\\ctikzset{tripoles/mos style/arrows}\n";
            output += "\\ctikzset{tripoles/pmos style/nocircle}\n";
        }

        //generate latex string for each component placed in the circuitmaker window
        for (int a = 0; a < components.size(); a++) {
            output += components.get(a).getLatexLine();
        }

        output += "\\end{circuitikz}";
        if (wrapInFigure) {
            output += "\n\\caption{Caption}";
            output += "\n\\end{figure}";
        }
        return output;
    }


    /**
     * Erases all placed components, clears LaTeX String and Components list
     *
     */
    public void clearSchematic() {
        String output = "";
        components.clear();
        CircuitikzTool.ui.updateComponentList(); //this is very bad and we shouldn't do it this way but eh whatever, update the UI component list (?)
    }



} // End
