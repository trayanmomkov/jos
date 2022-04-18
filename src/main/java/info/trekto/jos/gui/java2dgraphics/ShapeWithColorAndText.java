package info.trekto.jos.gui.java2dgraphics;

import java.awt.*;

public class ShapeWithColorAndText {
    private Shape shape;
    private Color color;
    private String text;

    public ShapeWithColorAndText(Shape shape, Color color) {
        this.shape = shape;
        this.color = color;
    }

    public ShapeWithColorAndText(Shape shape, Color color, String text) {
        this.shape = shape;
        this.color = color;
        this.text = text;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
