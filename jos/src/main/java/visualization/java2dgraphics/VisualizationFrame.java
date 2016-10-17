/**
 *
 */
package visualization.java2dgraphics;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * @author Trayan Momkov
 * @date 2016-окт-17 23:16
 *
 */
public class VisualizationFrame extends Frame {

    private List<Shape> shapes;

    /**
     * Instantiates an Example01 object.
     **/
    public static void main(String args[]) {
        new VisualizationFrame();
    }

    /**
     * Our Example01 constructor sets the frame's size, adds the
     * visual components, and then makes them visible to the user.
     * It uses an adapter class to deal with the user closing
     * the frame.
     **/
    public VisualizationFrame() {
        //Title our frame.
        super("Java 2D Example01");

        //Set the size for the frame.
        setSize(400, 300);

        //We need to turn on the visibility of our frame
        //by setting the Visible parameter to true.
        setVisible(true);

        //Now, we want to be sure we properly dispose of resources
        //this frame is using when the window is closed.  We use
        //an anonymous inner class adapter for this.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });
    }

    /**
     * The paint method provides the real magic.  Here we
     * cast the Graphics object to Graphics2D to illustrate
     * that we may use the same old graphics capabilities with
     * Graphics2D that we are used to using with Graphics.
     **/
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //Here is how we used to draw a square with width
        //of 200, height of 200, and starting at x=50, y=50.
        g.setColor(Color.red);
        g.drawRect(50, 50, 200, 200);

        //Let's set the Color to blue and then use the Graphics2D
        //object to draw a rectangle, offset from the square.
        //So far, we've not done anything using Graphics2D that
        //we could not also do using Graphics.  (We are actually
        //using Graphics2D methods inherited from Graphics.)
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.blue);
        g2d.drawRect(75, 75, 300, 200);

        if (shapes != null) {
            for (Object element : shapes) {
                Shape shape = (Shape) element;
                g2d.draw(shape);
            }
        }
    }

    public void draw(List<Shape> shapes) {
        this.shapes = shapes;
    }
}
