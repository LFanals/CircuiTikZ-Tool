package circuitikztool;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

import java.awt.Graphics2D; // added
import java.awt.geom.Point2D; // 2D added

/**
 * Component is meant to be a data object for storing all possible component
 * objects in a single arrayList inside of the class CircuitMaker. For that
 * purpose this class handles path, node, and three terminal components and will
 * be adapted to handle all supported components.
 *
 * @author James
 */
public class Component {

    //path specific placement variables
    Point2D wireStart, wireEnd;

    //non path placement variables
    Point2D position;

    String latexParameters = "";          //stores the string which ultimately ends up in the LaTeX output, this is the variable a user modifies when they change the "Component String" field in the UI
    String Label = "";                    //User defined label that is displayed as "Component Label" in UI, meant for the user to help organize their schematic as it suits them

    int componentType;                    //this variable defines what "Type" of component we're using, please reference the constant vairables below for possible values. 
    private boolean pathComponent = true;

    //LaTeX doesn't like 3 terminal devices having the same name, we use this variable so that their labels are iterated everytime a new 3 terminal component is placed. 
    private static int TransistorCounter = 1;
    private static int OpAmpCounter = 1;
    private static int TransformerCounter = 1;
    private static int BufferCounter = 1;
    private static int BlockCounter = 1;
    private static int MixerCounter = 1;

    //circuitikz requires us to give unique labels to components in order to connect nodes to them
    //for transistors and other multi-terminal devices we need to have a unique ID
    //the deviceID is only used in the LaTeX output
    private int deviceID;

    /*
        Since we have to handle as many components as possible with a single class we allow the class to define multiple different types of components
        in this way we are able to store everything in a single array list. 
    
        Important note: "Path Components" are components that have 2 terminals and go from point A to Point B (wires, resistors, capacitors, etc)
                        "non-path components" are components that have any number of terminals but only a single position variable (transistors, nodes, etc)
                        this distinction is important because there are two constructors one which is for path components and one that is not.
     */
    //path components
    final static int PATH = 0;
    final static int RESISTOR = 1;
    final static int CAPACITOR = 2;
    final static int INDUCTOR = 3;
    final static int DIODE = 4;
    final static int VOLTAGE_SOURCE = 5;
    final static int CURRENT_SOURCE = 6;

    final static int SWITCH_NOS = 20; // new components start at index 20
    final static int ARROW = 26;
    final static int N_ARROW = 27;
    final static int NODE = 28;

    final static int IMPEDANCE = 29;

    //non-path components
    final static int GROUND_NODE = 7;
    final static int VCC_NODE = 8;
    final static int VSS_NODE = 9;
    final static int TRANSISTOR_NPN = 10;
    final static int TRANSISTOR_PNP = 11;
    final static int NMOS = 12;
    final static int PMOS = 13;
    final static int NIGBT = 14;
    final static int PIGBT = 15;
    final static int OPAMP_3TERMINAL = 16;
    final static int OPAMP_5TERMINAL = 17;
    final static int TRANSFORMER = 18;
    final static int TRANSFORMER_WITH_CORE = 19;

    final static int BUFFER = 21; // new components start at index 20
    final static int FD_OPAMP = 22;
    final static int GM_AMP = 23;
    final static int BLOCK = 24;
    final static int MIXER = 25;

    final static int SACDC = 30;
    final static int SDCAC = 31;

    //non-component commands used for Latex Component Builder
    final static int DELETE = 1000;
    final static int CANCEL = 1001;

    /**
     * Constructor for component as an option. In some instances (latex string
     * builder window) we need to return a component or a command. this
     * constructor allows us to pass a command as a component to logic higher
     * up.
     *
     * @param componentSelected
     */
    public Component(int componentSelected) {
        switch (componentSelected) {
            case DELETE:
                break;
            case CANCEL:
                break;
            default:
                throw new IllegalArgumentException("No NON-PATH component type exists for constant " + componentSelected);
        }
        pathComponent = false; //simple boolean for the class to know whether or not it's a pathing variable (there are other ways to test this but this is the easiest) 
        componentType = componentSelected; //set this object's componentType to the passed in value
    }

    /**
     * Constructor for NON-PATH components, requires only a position and a
     * selected component value. consult the constants at the top of the class
     * for valid input values to this function
     *
     * @param position position (in terms of the circuitikz placement) where the
     * component would be placed
     * @param componentSelected component selected, consult constant values
     * above for valid non-path components.
     */
    public Component(Point2D position, int componentSelected) {
        this.position = position;                                //pass the input parameter to the object

        /* Depending on the component selected we need to initalize the string parameter's such that the latex output is correct
        The values passed into latexParameters and Label are meant to be "template" values  
        when adding a non-path component it needs to be added ONLY to this constructor, the fact that this constructor throws an exception when
        a path component is input into it is very important to the functioning of the program.
         */
        switch (componentSelected) {
            case TRANSISTOR_NPN:
                deviceID = TransistorCounter++;
                latexParameters = "node[npn](Q" + deviceID + "){Q" + deviceID + "}";
                Label = "NPN Transistor";
                break;
            case TRANSISTOR_PNP:
                deviceID = TransistorCounter++;
                latexParameters = "node[pnp](Q" + deviceID + "){Q" + deviceID + "}";
                Label = "PNP Transistor";
                break;
            case NMOS:
                deviceID = TransistorCounter++;
                latexParameters = "node[nmos](Q" + deviceID + "){Q" + deviceID + "}";
                Label = "N-MOS";
                break;
            case NIGBT:
                deviceID = TransistorCounter++;
                latexParameters = "node[nigbt](Q" + deviceID + "){Q" + deviceID + "}";
                Label = "N-IGBT";
                break;
            case PIGBT:
                deviceID = TransistorCounter++;
                latexParameters = "node[pigbt](Q" + deviceID + "){Q" + deviceID + "}";
                Label = "P-IGBT";
                break;
            case PMOS:
                deviceID = TransistorCounter++;
                latexParameters = "node[pmos](Q" + deviceID + "){Q" + deviceID + "}";
                Label = "P-MOS";
                break;
            case GROUND_NODE:
                latexParameters = "node[ground]{}";
                Label = "GND";
                break;
            case VCC_NODE:
                latexParameters = "node[vcc]{VCC}";
                Label = "VCC";
                break;
            case VSS_NODE:
                latexParameters = "node[vss]{VSS}";
                Label = "VSS";
                break;
            case OPAMP_3TERMINAL:
                //3 terminal and 5 terminal opamps are actually identical in terms of their 
                //original template string, however we have to treat them differently in the latex output
                //and in the drawing
                deviceID = OpAmpCounter++;
                latexParameters = "node[op amp,scale=1.02] (opamp" + deviceID + ") {}"; // mod
                Label = "3T OpAmp";
                break;
            case OPAMP_5TERMINAL:
                deviceID = OpAmpCounter++;
                latexParameters = "node[op amp,scale=2.04] (opamp" + deviceID + ") {}";
                Label = "5-Term Opamp";
                break;
            case TRANSFORMER:
                deviceID = TransformerCounter++;
                latexParameters = "node[transformer,scale=.952] (T" + deviceID + ") {}";
                Label = "Transformer";
                break;
            case TRANSFORMER_WITH_CORE:
                deviceID = TransformerCounter++;
                latexParameters = "node[transformer core,scale=.952] (T" + deviceID + ") {}";
                Label = "Transformer w/ Core";
                break;

            case BUFFER:
                deviceID = BufferCounter++;
                latexParameters = "node[buffer, scale=1] (buffer" + deviceID + ") {}";
                Label = "Buffer";
                break;
            case FD_OPAMP:
                deviceID = OpAmpCounter++;
                latexParameters = "node[fd op amp, scale=1.02] (opamp" + deviceID + ") {}";
                Label = "FD OpAmp";
                break;
            case GM_AMP:
                deviceID = OpAmpCounter++;
                latexParameters = "node[gm amp, scale=1.02] (opamp" + deviceID + ") {}";
                Label = "Gm cell";
                break;
            case BLOCK:
                deviceID = BlockCounter++;
                latexParameters = "node[block, scale=1] (block" + deviceID + ") {}";
                Label = "Block";
                break;

            case SACDC:
                deviceID = BlockCounter++;
                latexParameters = "node[sacdc, scale=1] (block" + deviceID + ") {}";
                Label = "SACDC";
                break;
            case SDCAC:
                deviceID = BlockCounter++;
                latexParameters = "node[sdcac, scale=1] (block" + deviceID + ") {}";
                Label = "SDCAC";
                break;

            case MIXER:
                deviceID = MixerCounter++;
                latexParameters = "node[mixer, scale=1] (mixer" + deviceID + ") {}";
                Label = "X";
                break;
            case NODE:
                latexParameters = "node[] {$x$}";
                Label = "x";
                break;
            

            default:
                //this exception is important in isPathComponent();
                //in the unlikely event that a path component some how used this constructor throw an error to alert the nearest code monkey
                throw new IllegalArgumentException("No NON-PATH component type exists for constant " + componentSelected);
        }
        pathComponent = false; //simple boolean for the class to know whether or not it's a pathing variable (there are other ways to test this but this is the easiest) 
        componentType = componentSelected; //set this object's componentType to the passed in value
    }

    /**
     * Constructor for PATH components including Wires, resistors, capacitors,
     * etc. Please consult the defined constants in CircuitMaker to determine
     * proper input values. Non-path components should not use this constructor,
     * the fact that this constructor throws an error when a non path component
     * is passed into it is important to the functioning of the program.
     *
     * this constructor also serves as the "ultimate list" of which components
     * are path components and which components are not path components, please
     * see isPathComponent()
     *
     * @param wireStart starting position of the path component (position is in
     * terms of circuitikz coordinates)
     * @param wireEnd ending position of the path component (position is in
     * terms of circuitikz coordinates)
     * @param componentSelected desired PATH component to be created, see
     * constants at the top of this class for acceptable values
     */
    public Component(Point2D wireStart, Point2D wireEnd, int componentSelected) {
        //pass start and end positions of the wire to the object
        this.wireStart = wireStart;
        this.wireEnd = wireEnd;

        /* Depending on the component selected we need to initalize the string parameter's such that the latex output is correct
        The values passed into latexParameters and Label are meant to be "template" values  
        when adding a path component it needs to be added ONLY using this constructor, the fact that this constructor throws an exception when
        a non-path component is input into it is very important to the functioning of the program.
         */
        switch (componentSelected) {
            case PATH:
                latexParameters = "to[short]";
                Label = "Wire";
                break;
            case RESISTOR:
                latexParameters = "to[R,l=$R$]";
                Label = "R";
                break;
            case CAPACITOR:
                latexParameters = "to[C,l=$C$]";
                Label = "C";
                break;
            case INDUCTOR:
                latexParameters = "to[L,l=$L$]";
                Label = "L";
                break;
            case DIODE:
                latexParameters = "to[D,l=$D$]";
                Label = "D";
                break;
            case VOLTAGE_SOURCE:
                latexParameters = "to[V,l=$V$]";
                Label = "V";
                break;
            case CURRENT_SOURCE:
                latexParameters = "to[isource,l=$I$]";
                Label = "I";
                break;

            case SWITCH_NOS:
                latexParameters = "to[nos]";
                Label = "NOS";
                break;
            case ARROW:
                latexParameters = "--";
                Label = "->";
                break;
            case N_ARROW:
                latexParameters = "-- node[at end, xshift=0.25cm, yshift=0.25cm] {$-$}";
                Label = "-> -";
                break;
            case IMPEDANCE:
                latexParameters = "to[european resistor,l=$Z$]";
                Label = "Z";
                break;


            default:
                //this exception is important in isPathComponent();
                //in the unlikely event that a non-path component some how used this constructor throw an error to alert the nearest code monkey
                throw new IllegalArgumentException("No PATH component type exists for constant " + componentSelected);
        }
        componentType = componentSelected; //pass the selected component value to the object
    }

    public int getDeviceID(){
      return deviceID;
    }
    
    public static Component getComponentFromXML(String xml) {
        if (getDataFromXMLTag(xml, "pathComponent").equals("true")) {
            Component ret = new Component(
                    new Point2D.Double(Double.parseDouble(getDataFromXMLTag(xml, "start-x")), Double.parseDouble(getDataFromXMLTag(xml, "start-y"))),
                    new Point2D.Double(Double.parseDouble(getDataFromXMLTag(xml, "end-x")), Double.parseDouble(getDataFromXMLTag(xml, "end-y"))),
                    Integer.parseInt(getDataFromXMLTag(xml, "type"))
            );
            ret.setLatexString(getDataFromXMLTag(xml, "latexParameters"));
            ret.setComponentLabel(getDataFromXMLTag(xml, "label"));
            return ret;
        } else {
            Component ret = new Component(
                    new Point2D.Double(Double.parseDouble(getDataFromXMLTag(xml, "position-x")), Double.parseDouble(getDataFromXMLTag(xml, "position-y"))),
                    Integer.parseInt(getDataFromXMLTag(xml, "type"))
            );
            ret.setLatexString(getDataFromXMLTag(xml, "latexParameters"));
            ret.setComponentLabel(getDataFromXMLTag(xml, "label"));
            return ret;
        }
    }

    public static String getDataFromXMLTag(String xml, String tag) {
        try {
            int startPos = xml.indexOf("<" + tag + ">") + tag.length() + 2;
            int endPos = xml.indexOf("</" + tag + ">");
            return xml.substring(startPos, endPos);
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("tag \"" + tag + "\" not found");
            return "";
        }
    }

    public String toXML() {
        String ret = "<component>";
        if (pathComponent) {
            ret += "<pathComponent>true</pathComponent>";
            ret += "<start-x>" + wireStart.getX() + "</start-x>";
            ret += "<start-y>" + wireStart.getY() + "</start-y>";
            ret += "<end-x>" + wireEnd.getX() + "</end-x>";
            ret += "<end-y>" + wireEnd.getY() + "</end-y>";
        } else {
            ret += "<pathComponent>false</pathComponent>";
            ret += "<position-x>" + position.getX() + "</position-x>";
            ret += "<position-y>" + position.getY() + "</position-y>";
        }
        ret += "<type>" + componentType + "</type>";
        ret += "<label>" + Label + "</label>";
        ret += "<latexParameters>" + latexParameters + "</latexParameters>";
        ret += "</component>";
        return ret;
    }

    public static void resetStatics() {
        TransistorCounter = 1;
        OpAmpCounter = 1;
        TransformerCounter = 1;
        BufferCounter = 1;
        BlockCounter = 1;
        MixerCounter = 1;
    }

    /**
     * For components that are already initalized this function identifies a
     * component as path or not-path
     *
     * @return true if path component, false if not.
     */
    public boolean isPathComponent() {
        return pathComponent;
    }

    /**
     * Since the circuit maker class will need to know which indexes are pathing
     * components and which ones aren't we use this function to test a given
     * index to determine whether or not a component is a path component. This
     * relys on the constructor's being properly split between pathing and non
     * pathing components.
     *
     * @param componentIndex component index relating to one of the constants
     * defined at the top of CircuitMaker class
     * @return true if path component, false if not path component
     */
    public static boolean isPathComponent(int componentIndex) {
        try {
            Component c = new Component(new Point2D.Double(0, 0), new Point2D.Double(0, 0), componentIndex);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Paints the component to the schematic window, Since the schematic window
     * allows for variable grid size and for the user to move the schematic
     * around it requires the current grid size, the current offset (position of
     * the origin relative to 0,0) and the position whether or not the component
     * is selected (selected components are highlighted in a different color in
     * the schematicWindow)
     *
     * @param g2d Graphics object for the components to be draw onto
     * @param gridSize current gridSize of the graphics object, this variable
     * allows for the components to scale relatively
     * @param offset current offset of the grid which is changed when the user
     * pans around the schematic
     * @param selected whether or not this component is currently selected
     */
    public void paint(Graphics g2d, double gridSize, Point2D offset, boolean selected) {
        //if a component is selected we should set its color differently. 
        if (selected) {
            g2d.setColor(Preferences.selectedColor);
        } else {
            g2d.setColor(Preferences.componentColor);
        }

        /*
            Since this class handles a wide variety of components we have to paint 
        each component differently. In the case of path components we can more or 
        less draw the component the same and just display the label. however non-path 
        components vary a lot so we need to handle them individually 
         */
        if (pathComponent) {
            g2d.drawLine(
                  (int)  (gridSize * (wireStart.getX() + offset.getX())),
                  (int)  (gridSize * (wireStart.getY() + offset.getY())),
                  (int)  (gridSize * (wireEnd.getX() + offset.getX())),
                  (int)  (gridSize * (wireEnd.getY() + offset.getY()))
            );
        } else if (componentType == VCC_NODE) {
            drawVCCNode(g2d, gridSize, position.getX() + offset.getX(), position.getY() + offset.getY());
        } else if (componentType == GROUND_NODE) {
            drawGNDNode(g2d, gridSize, position.getX() + offset.getX(), position.getY() + offset.getY());
        } else if (componentType == VSS_NODE) {
            drawVSSNode(g2d, gridSize, position.getX() + offset.getX(), position.getY() + offset.getY());
        } else if (componentType == OPAMP_3TERMINAL || componentType == OPAMP_5TERMINAL) {
            drawOpamp(g2d, gridSize, position.getX() + offset.getX(), position.getY() + offset.getY(), selected, componentType);
        } else if (componentType == BUFFER) {
            drawBuffer(g2d, gridSize, position.getX() + offset.getX(), position.getY() + offset.getY(), selected);
        } else if (componentType == FD_OPAMP) {
            drawFDOpAmp(g2d, gridSize, position.getX() + offset.getX(), position.getY() + offset.getY(), selected);
        } else if (componentType == GM_AMP) {
            drawGMAmp(g2d, gridSize, position.getX() + offset.getX(), position.getY() + offset.getY(), selected);
        } else if (componentType == BLOCK || componentType == SACDC || componentType == SDCAC) {
            drawBlock(g2d, gridSize, position.getX() + offset.getX(), position.getY() + offset.getY(), selected);
        } else if (componentType == MIXER) {
            drawMixer(g2d, gridSize, position.getX() + offset.getX(), position.getY() + offset.getY(), selected);
        } else if (componentType == NODE) {
            drawNode(g2d, gridSize, position.getX() + offset.getX(), position.getY() + offset.getY(), selected);
        } else if (componentType == TRANSFORMER || componentType == TRANSFORMER_WITH_CORE) {
            drawTransformer(g2d, gridSize, position.getX() + offset.getX(), position.getY() + offset.getY(), selected);
        } else {
            //if it's not any of those then it's a three terminal transistor so we just draw the transistor
            drawTransistor(g2d, gridSize, position.getX() + offset.getX(), position.getY() + offset.getY(), selected);
        }

        /*
                    this section of code implements the "Draw label to wire" functionality of the circuitmaker, 
                this way the user can always see what the label of any component is while they're working on the 
                schematic. 
                we first calculate the midpoint which is where we place the label. we then fill the background of the string black for visibility 
                then we draw the string
         */
        int fontSize = 10;
        g2d.setFont(new Font("Dialog", Font.PLAIN, fontSize));
        Point2D labelPosition;

        /*            if the component we're drawing is a path component then we want to place the label right on the midpoint
        otherwise we can just place it at the position of the component.        
        since the user is allowed to zoom the position of the label needs to be calculated as a fraction of the
        gridSize
         */
        if (isPathComponent()) {
            labelPosition = new Point2D.Double(
                    ( wireStart.getX() * gridSize + wireEnd.getX() * gridSize) / 2,
                    ( wireStart.getY() * gridSize + wireEnd.getY() * gridSize) / 2
            );
        } else if (componentType == VCC_NODE) {
            //vcc nodes have the label above the drawn component
            labelPosition = new Point2D.Double(position.getX() * gridSize, position.getY() * gridSize - 2 * gridSize / 3);
        } else if (componentType == GROUND_NODE || componentType == VSS_NODE) {
            //VSS and GND nodes have the label displayed below the component
            labelPosition = new Point2D.Double(position.getX() * gridSize, position.getY() * gridSize + 2 * gridSize / 3);
        } else {
            //if the component doesn't need any special label placement and isn't a pathing component
            //then we can just place the label directly on the position of the component
            labelPosition = new Point2D.Double(position.getX() * gridSize, position.getY() * gridSize);
        }

        //calculate width of the string itself
        int stringWidth = g2d.getFontMetrics().stringWidth(Label);

        //padding of the label (how many pixels of black space around the text before the border) 
        int boxPadding = 3;

        //create bounding box for the string
        g2d.setColor(Preferences.backgroundColor);
        g2d.fillRect((int) (labelPosition.getX() + offset.getX() * gridSize - stringWidth / 2 - boxPadding), (int) (labelPosition.getY() + offset.getY() * gridSize + 2 - fontSize - boxPadding), stringWidth + boxPadding * 2, fontSize + boxPadding * 2);

        if (selected) {
            g2d.setColor(Preferences.selectedColor);
        } else {
            g2d.setColor(Preferences.componentColor);
        }

        //create white border around label so it pops a little better
        g2d.setColor(Preferences.componentColor);
        g2d.drawRect((int) (labelPosition.getX() + offset.getX() * gridSize - stringWidth / 2 - boxPadding), (int) (labelPosition.getY() + offset.getY() * gridSize + 2 - fontSize - boxPadding), stringWidth + boxPadding * 2, fontSize + boxPadding * 2);
        //draw label string
        g2d.drawString(Label, (int) (labelPosition.getX() + offset.getX() * gridSize - stringWidth / 2), (int) (labelPosition.getY() + offset.getY() * gridSize + 2));
    }

    /**
     *
     * @return component label string
     */
    public String getComponentLabel() {
        return Label;
    }

    /**
     * sets the component label
     *
     * @param text String the component label should be set to
     */
    public void setComponentLabel(String text) {
        Label = text;
    }

    /**
     * returns the latex parameters of the current component
     *
     * @return latex parameter string of the current component
     */
    public String getLatexString() {
        return latexParameters;
    }

    /**
     * FETs need some special treatment in the latex output, this function
     * returns true if the device is a FET device and false otherwise
     *
     * @return true if FET device
     */
    public boolean isFet() {
        return componentType == NMOS || componentType == PMOS;
    }

    /**
     * sets the latex parameters of the current component
     *
     * @param text latex parameter string to pass into the current component
     */
    public void setLatexString(String text) {
        latexParameters = text;
    }

    /**
     * returns the beginning coordinate of a path component, throws
     * IllegalStateException if component is not a path component
     *
     * @return starting coordinate (in circuitikz coordinates) of the current
     * path component
     */
    public Point2D getStart() {
        if (pathComponent) {
            return wireStart;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * returns the end coordinate of a path component, throws
     * IllegalStateException if component is not a path component
     *
     * @return starting coordinate (in circuitikz coordinates) of the current
     * path component
     */
    public Point2D getEnd() {
        if (pathComponent) {
            return wireEnd;
        } else {
            throw new IllegalStateException();
        }
    }

    public Point2D getPosition() {
        if (pathComponent) {
            throw new IllegalStateException();
        } else {
            return position;
        }
    }

    /**
     * returns the component label string, including information about the
     * placement of the component for display in the UI.
     *
     * @return component label string with position information
     */
    public String getComponentLabelString() {
        if (isPathComponent()) {
            String retString = "";
            retString += Label + " ";
            retString += "[" + wireStart.getX() + "," + wireStart.getY() + "] to [" + wireEnd.getX() + "," + wireEnd.getY() + "]";
            return retString;
        } else {
            String retString = "";
            retString += Label + " ";
            retString += "[" + position.getX() + "," + position.getY() + "] ";
            return retString;
        }
    }

    /**
     * outputs the formatted LaTeX line representing this component, in special
     * cases this function may return multiple lines of LaTeX code
     *
     * @return Circuitikz code representing current component
     */
    public String getLatexLine() {
        String output = "";

        //path components are simple, just insert the label between the start and end position. 
        if (isPathComponent()) {
            if (componentType == ARROW || componentType == N_ARROW){
                output += "\\draw [->] (";
            } else {
                output += "\\draw (";
            }
            output +=  wireStart.getX() + "," +  (-1) * (wireStart.getY()) + ") ";
            output += getLatexString() + " ";
            output += "(" +  getEnd().getX() + "," +  (-1) * getEnd().getY() + ");";
        } else {

            /*to deal with multi-terminal and other non-path components we have to consider special cases.             
              for the most part we can just print the position values and the latexString and be good, however in the cases of some components
            such as the BJT devices we need to make sure that their terminals are "broken out" to our standardized grid system so that everything plays nicely
            together in the final output, there are much better and more human-readable ways to do this in CircuiTikz however those are much more difficult to implement
            and for the time being this serves most of the functionality at the cost of outputing more code. 
             */
            output += "\\draw (";
            if (componentType == NODE){
                output +=  position.getX() + "," +  (-1) * (position.getY() + 0.3) + ") ";
            }
            else {
                output +=  position.getX() + "," +  (-1) * (position.getY()) + ") ";
            }
            output += getLatexString() + ";";

            switch (componentType) {
                case TRANSISTOR_NPN:
                    //breakout the BJT's terminals to fit with the current grid system
                    output += "\\draw (Q" + deviceID + ".C) to[short] (" +  position.getX() + "," +  (-1) * (position.getY() - 1) + ");\n";
                    output += "\\draw (Q" + deviceID + ".E) to[short] (" +  position.getX() + "," +  (-1) * (position.getY() + 1) + ");\n";
                    output += "\\draw (Q" + deviceID + ".B) to[short] (" +  (position.getX() - 1) + "," +  (-1) * (position.getY()) + ");";
                    break;
                case TRANSISTOR_PNP:
                    //breakout the BJT's terminals to fit with the current grid system
                    output += "\\draw (Q" + deviceID + ".E) to[short] (" + position.getX() + "," + (-1) * (position.getY() - 1) + ");\n";
                    output += "\\draw (Q" + deviceID + ".C) to[short] (" + position.getX() + "," +  (-1) * (position.getY() + 1) + ");\n";
                    output += "\\draw (Q" + deviceID + ".B) to[short] (" + (position.getX() - 1) + "," +  (-1) * (position.getY()) + ");";
                    break;
                case NMOS:
                    //breakout the fet's terminals to fit with the current grid system:
                    output += "\\draw (Q" + deviceID + ".D) to[short] (" +  position.getX() + "," +  (-1) * (position.getY() - 1) + ");\n";
                    output += "\\draw (Q" + deviceID + ".S) to[short] (" +  position.getX() + "," +  (-1) * (position.getY() + 1) + ");\n";
                    output += "\\draw (Q" + deviceID + ".G) to[short] (" +  (position.getX() - 1) + "," +  (-1) * (position.getY()) + ");";
                    break;
                case PMOS:
                    //breakout the fets's terminals to fit with the current grid system:
                    output += "\\draw (Q" + deviceID + ".S) to[short] (" +  position.getX() + "," +  (-1) * (position.getY() - 1) + ");\n";
                    output += "\\draw (Q" + deviceID + ".D) to[short] (" +  position.getX() + "," +  (-1) * (position.getY() + 1) + ");\n";
                    output += "\\draw (Q" + deviceID + ".G) to[short] (" +  (position.getX() - 1) + "," +  (-1) * (position.getY()) + ");";
                    break;
                case NIGBT:
                    //breakout the IGBT's terminals to fit with the current grid system:
                    output += "\\draw (Q" + deviceID + ".D) to[short] (" +  position.getX() + "," +  (-1) * (position.getY() - 1) + ");\n";
                    output += "\\draw (Q" + deviceID + ".S) to[short] (" +  position.getX() + "," +  (-1) * (position.getY() + 1) + ");\n";
                    output += "\\draw (Q" + deviceID + ".G) to[short] (" +  (position.getX() - 1) + "," +  (-1) * (position.getY()) + ");";
                    break;
                case PIGBT:
                    //breakout the IGBT's terminals to fit with the current grid system:
                    output += "\\draw (Q" + deviceID + ".S) to[short] (" +  position.getX() + "," +  (-1) * (position.getY() - 1) + ");\n";
                    output += "\\draw (Q" + deviceID + ".D) to[short] (" +  position.getX() + "," +  (-1) * (position.getY() + 1) + ");\n";
                    output += "\\draw (Q" + deviceID + ".G) to[short] (" +  (position.getX() - 1) + "," +  (-1) * (position.getY()) + ");";
                    break;

                case TRANSFORMER:
                    output += "\\draw (T" + deviceID + ".A1) to[short] (" +  (position.getX() - 1) + "," +  (-1) * (position.getY() - 1) + ");\n";
                    output += "\\draw (T" + deviceID + ".A2) to[short] (" +  (position.getX() - 1) + "," +  (-1) * (position.getY() + 1) + ");\n";

                    output += "\\draw (T" + deviceID + ".B1) to[short] (" +  (position.getX() + 1) + "," +  (-1) * (position.getY() - 1) + ");\n";
                    output += "\\draw (T" + deviceID + ".B2) to[short] (" +  (position.getX() + 1) + "," +  (-1) * (position.getY() + 1) + ");";

                    break;
                case TRANSFORMER_WITH_CORE:
                    output += "\\draw (T" + deviceID + ".A1) to[short] (" +  (position.getX() - 1) + "," +  (-1) * (position.getY() - 1) + ");\n";
                    output += "\\draw (T" + deviceID + ".A2) to[short] (" +  (position.getX() - 1) + "," +  (-1) * (position.getY() + 1) + ");\n";

                    output += "\\draw (T" + deviceID + ".B1) to[short] (" +  (position.getX() + 1) + "," +  (-1) * (position.getY() - 1) + ");\n";
                    output += "\\draw (T" + deviceID + ".B2) to[short] (" +  (position.getX() + 1) + "," +  (-1) * (position.getY() + 1) + ");";

                    break;
                case OPAMP_3TERMINAL:
                    //breakout the opamp's terminals to fit with the current grid system:
                    output += "\n\\draw (opamp" + deviceID + ".-) to[short] (" +  (position.getX() - 1.5) + "," +  (-1) * (position.getY() - 0.5) + ");\n"; // mod
                    output += "\\draw (opamp" + deviceID + ".+) to[short] (" +  (position.getX() - 1.5) + "," +  (-1) * (position.getY() + 0.5) + ");"; // mod 
//                    output += "\\draw (opamp" + deviceID + ".out) to[short] (" + (int) (position.getX() +1 ) + "," + (int) (-1) * (position.getY()) + ");";
                    break;
                case OPAMP_5TERMINAL:
                    //breakout the opamp's terminals to fit with the current grid system:
                    output += "\n\\draw (opamp" + deviceID + ".-) to[short] (" +  (position.getX() - 3) + "," +  (-1) * (position.getY() - 1) + ");\n";
                    output += "\\draw (opamp" + deviceID + ".+) to[short] (" +  (position.getX() - 3) + "," +  (-1) * (position.getY() + 1) + ");";
//                    output += "\\draw (opamp" + deviceID + ".out) to[short] (" + (int) (position.getX()) + "," + (int) (-1) * (position.getY()) + ");";
                    break;

                case BUFFER:
                    // connect a wire to the input terminal:
                    output += "\n\\draw (buffer" + deviceID + ".in) to[short] (" +  (position.getX() - 1) + "," +  (-1) * (position.getY() - 0) + ");";
                    break;
                case FD_OPAMP:
                    // connect a wire to the input terminals:
                    output += "\n\\draw (opamp" + deviceID + ".-) to[short] (" +  (position.getX() - 1.5) + "," +  (-1) * (position.getY() - 0.5) + ");\n";
                    output += "\\draw (opamp" + deviceID + ".+) to[short] (" +  (position.getX() - 1.5) + "," +  (-1) * (position.getY() + 0.5) + ");";
                    break;
                case GM_AMP:
                    // connect a wire to the input terminals:
                    output += "\n\\draw (opamp" + deviceID + ".-) to[short] (" +  (position.getX() - 1.5) + "," +  (-1) * (position.getY() - 0.5) + ");\n";
                    output += "\\draw (opamp" + deviceID + ".+) to[short] (" +  (position.getX() - 1.5) + "," +  (-1) * (position.getY() + 0.5) + ");";
                    break;

                case BLOCK:
                    // declare block
                    break;
                case SACDC:
                    // declare block
                    break;
                case SDCAC:
                    // declare block
                    break;
                case MIXER:
                    // declare mixer
                    break;
                case NODE:
                    // declare mixer
                    break;
                

            }
        }
        output += "\n"; //an extra line break to be nice :)
        return output;
    }

    /**
     * draws the gndNode at an x and y position (in CircuiTikz coordinates) to
     * the schematic window
     *
     * @param g2d graphics object to be drawn onto
     * @param gridSize current size of the grid
     * @param xPos x position in circuitikz coordinates
     * @param yPos y position in circuitikz coordinates
     */
    public static void drawGNDNode(Graphics g2d, double gridSize, double xPos, double yPos) {
        g2d.drawLine(
              (int)  (gridSize * xPos - gridSize / 4),
              (int)  (gridSize * yPos),
              (int)  (gridSize * xPos + gridSize / 4),
              (int)  (gridSize * yPos)
        );
        g2d.drawLine(
              (int)  (gridSize * xPos - gridSize / 8),
              (int)  (gridSize * yPos + gridSize / 8),
              (int)  (gridSize * xPos + gridSize / 8),
              (int)  (gridSize * yPos + gridSize / 8)
        );
        g2d.drawLine(
              (int)  (gridSize * xPos - gridSize / 16),
              (int)  (gridSize * yPos + 2 * gridSize / 8),
              (int)  (gridSize * xPos + gridSize / 16),
              (int)  (gridSize * yPos + 2 * gridSize / 8)
        );
    }

    /**
     * draws the vss node at an x and y position (in CircuiTikz coordinates) to
     * the schematic window
     *
     * @param g2d graphics object to be drawn onto
     * @param gridSize current size of the grid
     * @param xPos x position in circuitikz coordinates
     * @param yPos y position in circuitikz coordinates
     */
    public static void drawVSSNode(Graphics g2d, double gridSize, double xPos, double yPos) {
        g2d.drawLine(
              (int)  (gridSize * xPos),
              (int)  (gridSize * yPos),
              (int)  (gridSize * xPos),
              (int)  (gridSize * yPos + gridSize / 3)
        );
        g2d.drawLine(
              (int)  (gridSize * xPos),
              (int)  (gridSize * yPos + gridSize / 3),
              (int)  (gridSize * xPos - gridSize / 8),
              (int)  (gridSize * yPos + gridSize / 5 - gridSize / 8)
        );
        g2d.drawLine(
              (int)  (gridSize * xPos),
              (int)  (gridSize * yPos + gridSize / 3),
              (int)  (gridSize * xPos + gridSize / 8),
              (int)  (gridSize * yPos + gridSize / 5 - gridSize / 8)
        );

    }

    /**
     * draws the vcc Node at an x and y position (in CircuiTikz coordinates) to
     * the schematic window
     *
     * @param g2d graphics object to be drawn onto
     * @param gridSize current size of the grid
     * @param xPos x position in circuitikz coordinates
     * @param yPos y position in circuitikz coordinates
     */
    public static void drawVCCNode(Graphics g2d, double gridSize, double xPos, double yPos) {
        g2d.drawLine(
              (int)  (gridSize * xPos),
              (int)  (gridSize * yPos),
              (int)  (gridSize * xPos),
              (int)  (gridSize * yPos - gridSize / 3)
        );
        g2d.drawLine(
              (int)  (gridSize * xPos),
              (int)  (gridSize * yPos - gridSize / 3),
              (int)  (gridSize * xPos - gridSize / 8),
              (int)  (gridSize * yPos - gridSize / 5 + gridSize / 8)
        );
        g2d.drawLine(
              (int)  (gridSize * xPos),
              (int)  (gridSize * yPos - gridSize / 3),
              (int)  (gridSize * xPos + gridSize / 8),
              (int)  (gridSize * yPos - gridSize / 5 + gridSize / 8)
        );
    }

    /**
     * draws the transistor at an x and y position (in CircuiTikz coordinates)
     * to the schematic window, must
     *
     * @param g2d graphics object to be drawn onto
     * @param gridSize current size of the grid
     * @param xPos x position in circuitikz coordinates
     * @param yPos y position in circuitikz coordinates
     * @param selected boolean indicating whether or not the transistor should
     * be drawn as a selected component
     */
    public static void drawTransistor(Graphics g2d, double gridSize, double xPos, double yPos, boolean selected) {
        g2d.drawLine((int) (gridSize * xPos), (int) (gridSize * yPos), (int) (gridSize * xPos), (int) (gridSize * yPos - gridSize));
        g2d.drawLine((int) (gridSize * xPos), (int) (gridSize * yPos), (int) (gridSize * xPos), (int) (gridSize * yPos + gridSize));
        g2d.drawLine((int) (gridSize * xPos), (int) (gridSize * yPos), (int) (gridSize * xPos - gridSize), (int) (gridSize * yPos));
        g2d.setColor(Preferences.backgroundColor);
        g2d.fillOval((int) (gridSize * xPos - gridSize / 3), (int) (gridSize * yPos - gridSize / 3), (int) (gridSize * 2 / 3), (int) (gridSize * 2 / 3));
        if (selected) {
            g2d.setColor(Preferences.selectedColor);
        } else {
            g2d.setColor(Preferences.componentColor);
        }
        g2d.drawOval( (int) (gridSize * xPos - gridSize / 3), (int) (gridSize * yPos - gridSize / 3), (int) (gridSize * 2 / 3), (int) (gridSize * 2 / 3));
    }

    public static void drawTransformer(Graphics g2d, double gridSize, double xPos, double yPos, boolean selected) {
        if (selected) {
            g2d.setColor(Preferences.selectedColor);
        } else {
            g2d.setColor(Preferences.componentColor);
        }

        //top and bottom horizontal lines on left side
        g2d.drawLine( (int) ((xPos - 1) * gridSize), (int) ((yPos + 1) * gridSize), (int) ((xPos - .25) * gridSize), (int) ((yPos + 1) * gridSize));
        g2d.drawLine( (int) ((xPos - 1) * gridSize), (int) ((yPos - 1) * gridSize), (int) ((xPos - .25) * gridSize), (int) ((yPos - 1) * gridSize));

        //vertical horizontal lines on left and right side
        g2d.drawLine((int) ((xPos + .25) * gridSize), (int) ((yPos + 1) * gridSize), (int) ((xPos + .25) * gridSize), (int) ((yPos - 1) * gridSize));
        g2d.drawLine((int) ((xPos - .25) * gridSize), (int) ((yPos + 1) * gridSize), (int) ((xPos - .25) * gridSize), (int) ((yPos - 1) * gridSize));

        //top and bottom horizontal lines on right side
        g2d.drawLine( (int) ((xPos + 1) * gridSize), (int) ((yPos + 1) * gridSize), (int) ((xPos + .25) * gridSize), (int) ((yPos + 1) * gridSize));
        g2d.drawLine( (int) ((xPos + 1) * gridSize), (int) ((yPos - 1) * gridSize), (int) ((xPos + .25) * gridSize), (int) ((yPos - 1) * gridSize));

        //draw some impedence-like symbols to differentiate the transformer a bit
        g2d.fillRect((int) ((xPos - .35) * gridSize), (int) ((yPos - .5) * gridSize), (int) ((.2 * gridSize)), (int) (gridSize));
        g2d.fillRect((int) ((xPos + .15) * gridSize), (int) ((yPos - .5) * gridSize), (int) ((.2 * gridSize)), (int) (gridSize));
    }

    /**
     * draws the transistor at an x and y position (in CircuiTikz coordinates)
     * to the schematic window, must
     *
     * @param g2d graphics object to be drawn onto
     * @param gridSize current size of the grid
     * @param xPos x position in circuitikz coordinates
     * @param yPos y position in circuitikz coordinates
     * @param selected boolean indicating whether or not the transistor should
     * be drawn as a selected component
     * @param component integer representing the component itself, since 5
     * terminal and 3 terminal opamps need to be drawn differently. (uses
     * constants defined at the top of Component class)
     */
    public static void drawOpamp(Graphics g2d, double gridSize, double xPos, double yPos, boolean selected, int component) {
        Polygon opampBody = new Polygon();
        opampBody.addPoint( (int) (gridSize * (xPos + 0.8)), (int) (gridSize * (yPos - 0))); // mod, adds 0.5 points
        opampBody.addPoint((int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos - 1))); // mod
        opampBody.addPoint((int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos + 1))); // mod

        g2d.drawLine( (int) (gridSize * (xPos + 0.8)), (int) (gridSize * (yPos - 0)),  (int) (gridSize * (xPos + 1.2)), (int) (gridSize * (yPos - 0))); // output line


        g2d.setColor(Preferences.backgroundColor);
        g2d.fillPolygon(opampBody);
        if (selected) {
            g2d.setColor(Preferences.selectedColor);
        } else {
            g2d.setColor(Preferences.componentColor);
        }
        g2d.drawPolygon(opampBody);

        //add terminals
        g2d.drawLine((int) (gridSize * (xPos - 1.5)), (int) (gridSize * (yPos - 0.5)), (int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos - 0.5))); // mod
        g2d.drawLine((int) (gridSize * (xPos - 1.5)), (int) (gridSize * (yPos + 0.5)), (int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos + 0.5))); // mod

        //finally add the inverting and non-inverting input indicators  
        //inverting indicator
        g2d.drawLine((int) (gridSize * (xPos - .4)), (int) (gridSize * (yPos - 0.5)), (int) (gridSize * (xPos - .8)), (int) (gridSize * (yPos - 0.5))); // mod
        //non-inverting indicator
        g2d.drawLine((int) (gridSize * (xPos - .6)), (int) (gridSize * (yPos + 0.7)), (int) (gridSize * (xPos - .6)), (int) (gridSize * (yPos + 0.3))); // mod
        g2d.drawLine((int) (gridSize * (xPos - .4)), (int) (gridSize * (yPos + 0.5)), (int) (gridSize * (xPos - .8)), (int) (gridSize * (yPos + 0.5))); // mod

        //have to draw the power supply inputs for 5 terminal opamps 
        if (component == OPAMP_5TERMINAL) {
            g2d.fillOval( (int) (gridSize * (xPos) - gridSize / 8), (int) (gridSize * (yPos - 1) - gridSize / 8), (int) (gridSize / 4), (int) (gridSize / 4));
            g2d.fillOval( (int) (gridSize * (xPos) - gridSize / 8), (int) (gridSize * (yPos + 1) - gridSize / 8), (int) (gridSize / 4), (int) (gridSize / 4));
        }

    }


    /**
     * draws the buffer at an x and y position (in CircuiTikz coordinates)
     * to the schematic window, must
     *
     * @param g2d graphics object to be drawn onto
     * @param gridSize current size of the grid
     * @param xPos x position in circuitikz coordinates
     * @param yPos y position in circuitikz coordinates
     * @param selected boolean indicating whether or not the buffer should
     * be drawn as a selected component
     */
    public static void drawBuffer(Graphics g2d, double gridSize, double xPos, double yPos, boolean selected) {
        Polygon bufferBody = new Polygon();
        bufferBody.addPoint( (int) (gridSize * (xPos + 0.3)), (int) (gridSize * (yPos - 0))); // mod, adds 0.5 points
        bufferBody.addPoint((int) (gridSize * (xPos - 0.5)), (int) (gridSize * (yPos - 0.5))); // mod
        bufferBody.addPoint((int) (gridSize * (xPos - 0.5)), (int) (gridSize * (yPos + 0.5))); // mod

        // output line
        g2d.drawLine( (int) (gridSize * (xPos + 0.4)), (int) (gridSize * (yPos - 0)),  (int) (gridSize * (xPos + 0.6)), (int) (gridSize * (yPos - 0)));

        g2d.setColor(Preferences.backgroundColor);
        g2d.fillPolygon(bufferBody);
        if (selected) {
            g2d.setColor(Preferences.selectedColor);
        } else {
            g2d.setColor(Preferences.componentColor);
        }
        g2d.drawPolygon(bufferBody);

        // input line
        g2d.drawLine((int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos - 0.0)), (int) (gridSize * (xPos - 0.5)), (int) (gridSize * (yPos - 0.0))); // mod

    }


    /**
     * draws the fully-differential opamp at an x and y position (in CircuiTikz coordinates)
     *
     * @param g2d graphics object to be drawn onto
     * @param gridSize current size of the grid
     * @param xPos x position in circuitikz coordinates
     * @param yPos y position in circuitikz coordinates
     * @param selected boolean indicating whether or not the fd opamp should
     * be drawn as a selected component
     */
    public static void drawFDOpAmp(Graphics g2d, double gridSize, double xPos, double yPos, boolean selected) {
        Polygon opampBody = new Polygon();
        opampBody.addPoint( (int) (gridSize * (xPos + 0.8)), (int) (gridSize * (yPos - 0))); // mod, adds 0.5 points
        opampBody.addPoint((int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos - 1))); // mod
        opampBody.addPoint((int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos + 1))); // mod

        // output terminals
        g2d.drawLine( (int) (gridSize * (xPos - 0.1)), (int) (gridSize * (yPos - 0.5)),  (int) (gridSize * (xPos + 0.75)), (int) (gridSize * (yPos - 0.5)));
        g2d.drawLine( (int) (gridSize * (xPos - 0.1)), (int) (gridSize * (yPos + 0.5)),  (int) (gridSize * (xPos + 0.75)), (int) (gridSize * (yPos + 0.5)));

        g2d.setColor(Preferences.backgroundColor);
        g2d.fillPolygon(opampBody);
        if (selected) {
            g2d.setColor(Preferences.selectedColor);
        } else {
            g2d.setColor(Preferences.componentColor);
        }
        g2d.drawPolygon(opampBody);

        //add input terminals
        g2d.drawLine((int) (gridSize * (xPos - 1.5)), (int) (gridSize * (yPos - 0.5)), (int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos - 0.5))); // mod
        g2d.drawLine((int) (gridSize * (xPos - 1.5)), (int) (gridSize * (yPos + 0.5)), (int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos + 0.5))); // mod

        //finally add the inverting and non-inverting input indicators  
        //inverting indicator (-)
        g2d.drawLine((int) (gridSize * (xPos - .7)), (int) (gridSize * (yPos - 0.5)), (int) (gridSize * (xPos - .9)), (int) (gridSize * (yPos - 0.5))); // mod
        //non-inverting indicator (+)
        g2d.drawLine((int) (gridSize * (xPos - .8)), (int) (gridSize * (yPos + 0.6)), (int) (gridSize * (xPos - .8)), (int) (gridSize * (yPos + 0.4))); // mod
        g2d.drawLine((int) (gridSize * (xPos - .7)), (int) (gridSize * (yPos + 0.5)), (int) (gridSize * (xPos - .9)), (int) (gridSize * (yPos + 0.5))); // mod
        
        // add the inverting and non-inverting output indicators  
        //inverting indicator (-)
        g2d.drawLine((int) (gridSize * (xPos - .4)), (int) (gridSize * (yPos + 0.4)), (int) (gridSize * (xPos - .2)), (int) (gridSize * (yPos + 0.4))); // mod
        //non-inverting indicator (+)
        g2d.drawLine((int) (gridSize * (xPos - .3)), (int) (gridSize * (yPos - 0.5)), (int) (gridSize * (xPos - .3)), (int) (gridSize * (yPos - 0.3))); // mod
        g2d.drawLine((int) (gridSize * (xPos - .4)), (int) (gridSize * (yPos - 0.4)), (int) (gridSize * (xPos - .2)), (int) (gridSize * (yPos - 0.4))); // mod
    }

    /**
     * draws the gm amp at an x and y position (in CircuiTikz coordinates)
     *
     * @param g2d graphics object to be drawn onto
     * @param gridSize current size of the grid
     * @param xPos x position in circuitikz coordinates
     * @param yPos y position in circuitikz coordinates
     * @param selected boolean indicating whether or not the fd opamp should
     * be drawn as a selected component
     */
    public static void drawGMAmp(Graphics g2d, double gridSize, double xPos, double yPos, boolean selected) {
        Polygon gmampBody = new Polygon();
        gmampBody.addPoint( (int) (gridSize * (xPos + 0.8)), (int) (gridSize * (yPos + 0.5))); // mod, adds 0.5 points
        gmampBody.addPoint( (int) (gridSize * (xPos + 0.8)), (int) (gridSize * (yPos - 0.5))); // mod, adds 0.5 points
        gmampBody.addPoint((int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos - 1))); // mod
        gmampBody.addPoint((int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos + 1))); // mod

        // output terminal
        g2d.drawLine( (int) (gridSize * (xPos - 0.1)), (int) (gridSize * (yPos - 0.0)),  (int) (gridSize * (xPos + 1.25)), (int) (gridSize * (yPos - 0.0)));

        g2d.setColor(Preferences.backgroundColor);
        g2d.fillPolygon(gmampBody);
        if (selected) {
            g2d.setColor(Preferences.selectedColor);
        } else {
            g2d.setColor(Preferences.componentColor);
        }
        g2d.drawPolygon(gmampBody);

        //add input terminals
        g2d.drawLine((int) (gridSize * (xPos - 1.5)), (int) (gridSize * (yPos - 0.5)), (int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos - 0.5))); // mod
        g2d.drawLine((int) (gridSize * (xPos - 1.5)), (int) (gridSize * (yPos + 0.5)), (int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos + 0.5))); // mod

        //finally add the inverting and non-inverting input indicators  
        //inverting indicator (-)
        g2d.drawLine((int) (gridSize * (xPos - .7)), (int) (gridSize * (yPos - 0.5)), (int) (gridSize * (xPos - .9)), (int) (gridSize * (yPos - 0.5))); // mod
        //non-inverting indicator (+)
        g2d.drawLine((int) (gridSize * (xPos - .8)), (int) (gridSize * (yPos + 0.6)), (int) (gridSize * (xPos - .8)), (int) (gridSize * (yPos + 0.4))); // mod
        g2d.drawLine((int) (gridSize * (xPos - .7)), (int) (gridSize * (yPos + 0.5)), (int) (gridSize * (xPos - .9)), (int) (gridSize * (yPos + 0.5))); // mod
        
    }


    /**
     * draws the block at an x and y position (in CircuiTikz coordinates)
     *
     * @param g2d graphics object to be drawn onto
     * @param gridSize current size of the grid
     * @param xPos x position in circuitikz coordinates
     * @param yPos y position in circuitikz coordinates
     * @param selected boolean indicating whether or not the fd opamp should
     * be drawn as a selected component
     */
    public static void drawBlock(Graphics g2d, double gridSize, double xPos, double yPos, boolean selected) {
        Polygon blockBody = new Polygon();
        blockBody.addPoint( (int) (gridSize * (xPos + 1)), (int) (gridSize * (yPos + 0.5))); // mod, adds 0.5 points
        blockBody.addPoint( (int) (gridSize * (xPos + 1)), (int) (gridSize * (yPos - 0.5))); // mod, adds 0.5 points
        blockBody.addPoint((int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos - 0.5))); // mod
        blockBody.addPoint((int) (gridSize * (xPos - 1)), (int) (gridSize * (yPos + 0.5))); // mod


        g2d.setColor(Preferences.backgroundColor);
        g2d.fillPolygon(blockBody);
        if (selected) {
            g2d.setColor(Preferences.selectedColor);
        } else {
            g2d.setColor(Preferences.componentColor);
        }
        g2d.drawPolygon(blockBody);

    }



    /**
     * draws the mixer at an x and y position (in CircuiTikz coordinates)
     *
     * @param g2d graphics object to be drawn onto
     * @param gridSize current size of the grid
     * @param xPos x position in circuitikz coordinates
     * @param yPos y position in circuitikz coordinates
     * @param selected boolean indicating whether or not the fd opamp should
     * be drawn as a selected component
     */
    public static void drawMixer(Graphics g2d, double gridSize, double xPos, double yPos, boolean selected) {
            g2d.drawOval((int) (gridSize*(xPos - 0.5)), (int) (gridSize*(yPos - 0.5)), (int) gridSize, (int) gridSize);
        g2d.setColor(Preferences.backgroundColor);
        if (selected) {
            g2d.setColor(Preferences.selectedColor);
        } else {
            g2d.setColor(Preferences.componentColor);
        }

    }

    /**
     * draws the mixer at an x and y position (in CircuiTikz coordinates)
     *
     * @param g2d graphics object to be drawn onto
     * @param gridSize current size of the grid
     * @param xPos x position in circuitikz coordinates
     * @param yPos y position in circuitikz coordinates
     * @param selected boolean indicating whether or not the fd opamp should
     * be drawn as a selected component
     */
    public static void drawNode(Graphics g2d, double gridSize, double xPos, double yPos, boolean selected) {
        // g2d.drawOval((int) (gridSize*(xPos - 0.5 + 0.4)), (int) (gridSize*(yPos - 0.5 + 0.0)), (int) gridSize/2, (int) gridSize/2);

        Polygon nodeBody = new Polygon();
        nodeBody.addPoint( (int) (gridSize * (xPos + 0.2)), (int) (gridSize * (yPos - 0.0))); // mod, adds 0.5 points
        nodeBody.addPoint( (int) (gridSize * (xPos + 0.2)), (int) (gridSize * (yPos + 0.4))); // mod, adds 0.5 points
        nodeBody.addPoint((int) (gridSize * (xPos - 0.2)), (int) (gridSize * (yPos + 0.4))); // mod
        nodeBody.addPoint((int) (gridSize * (xPos - 0.2)), (int) (gridSize * (yPos - 0.0))); // mod

        g2d.setColor(Preferences.backgroundColor);
        g2d.fillPolygon(nodeBody);
        if (selected) {
            g2d.setColor(Preferences.selectedColor);
        } else {
            g2d.setColor(Preferences.componentColor);
        }
        g2d.drawPolygon(nodeBody);

    }




} // End
