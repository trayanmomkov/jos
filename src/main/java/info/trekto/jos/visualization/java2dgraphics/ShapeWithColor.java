package info.trekto.jos.visualization.java2dgraphics;

import java.awt.*;

public class ShapeWithColor {
    private Shape shape;
    private Color color;

    public ShapeWithColor(Shape shape, Color color) {
        this.shape = shape;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }
}
