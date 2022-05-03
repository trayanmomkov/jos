package info.trekto.jos.gui;

import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.*;

import static info.trekto.jos.core.Controller.C;

public class InitialObjectsTableModelAndListener extends DefaultTableModel implements TableModelListener {
    private final Map<String, Integer> columnNameIndexMap;
    private List<SimulationObject> initialObjects;

    public InitialObjectsTableModelAndListener() {
        super();

        columnNameIndexMap = new HashMap<>();

        int i = 0;
        addCol("id", i++);
        addCol("mass", i++);
        addCol("X", i++);
        addCol("Y", i++);
        addCol("Z", i++);
        addCol("radius", i++);
        addCol("speedX", i++);
        addCol("speedY", i++);
        addCol("speedZ", i++);
        addCol("color", i++);

        addTableModelListener(this);
    }

    void addCol(String name, int index) {
        addColumn(name);
        columnNameIndexMap.put(name, index);
    }

    public void addRow(SimulationObject o) {
        super.addRow(new Object[]{
                o.getId(),
                o.getMass(),
                o.getX(),
                o.getY(),
                o.getZ(),
                o.getRadius(),
                o.getSpeed().getX(),
                o.getSpeed().getY(),
                o.getSpeed().getZ(),
                String.format("%08X", o.getColor()).substring(2)});
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() != TableModelEvent.DELETE && e.getColumn() != -1) {
            refreshInitialObjects();
        }
    }

    public void refreshInitialObjects() {
        initialObjects = new ArrayList<>();
        for (Object vector : dataVector) {
            Vector v = (Vector) vector;
            SimulationObject o = C.createNewSimulationObject();
            int i = 0;
            o.setId(String.valueOf(v.get(i++)));
            o.setMass(getNumber(v.get(i++)));
            o.setX(getNumber(v.get(i++)));
            o.setY(getNumber(v.get(i++)));
            o.setZ(getNumber(v.get(i++)));
            o.setRadius(getNumber(v.get(i++)));
            o.setSpeed(new TripleNumber(
                    getNumber(v.get(i++)),
                    getNumber(v.get(i++)),
                    getNumber(v.get(i++))));
            o.setColor(Integer.parseInt(String.valueOf(v.get(i++)), 16));

            initialObjects.add(o);
        }
    }

    private Number getNumber(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        } else {
            return New.num(String.valueOf(value));
        }
    }

    public List<SimulationObject> getInitialObjects() {
        return initialObjects;
    }

    public void setInitialObjects(List<SimulationObject> initialObjects) {
        this.initialObjects = initialObjects;
        setRowCount(0);
        for (SimulationObject initialObject : initialObjects) {
            addRow(initialObject);
        }
    }
}
