package info.trekto.jos.gui.java2dgraphics;

import java.awt.*;

public class ShapeWithColorAndText {
    private Shape shape;
    private Color color;
    private String text;
    private boolean metaData;

    public ShapeWithColorAndText(Shape shape, Color color) {
        this.shape = shape;
        this.color = color;
    }

    public ShapeWithColorAndText(Shape shape, Color color, String text, boolean metaData) {
        this.shape = shape;
        this.color = color;
        this.text = text;
        this.metaData = metaData;
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

    public boolean isMetaData() {
        return metaData;
    }

    public void setMetaData(boolean metaData) {
        this.metaData = metaData;
    }
}
