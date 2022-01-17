package info.trekto.jos.gui;

import info.trekto.jos.C;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import info.trekto.jos.model.impl.TripleInt;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.New;
import info.trekto.jos.numbers.Number;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class InitialObjectsTableModelAndListener extends DefaultTableModel implements TableModelListener {
    Map<String, Integer> columnNameIndexMap;
    MainForm mainForm;

    public InitialObjectsTableModelAndListener(MainForm mainForm) {
        super();
        this.mainForm = mainForm;

        columnNameIndexMap = new HashMap<>();

        int i = 0;
        addCol("label", i++);
        addCol("mass", i++);
        addCol("X", i++);
        addCol("Y", i++);
        addCol("Z", i++);
        addCol("radius", i++);
        addCol("speedX", i++);
        addCol("speedY", i++);
        addCol("speedX", i++);
        addCol("motionless", i++);
        addCol("colorR", i++);
        addCol("colorG", i++);
        addCol("colorB", i++);

        addTableModelListener(this);
    }

    void addCol(String name, int index) {
        addColumn(name);
        columnNameIndexMap.put(name, index);
    }

    public void addRow(SimulationObject o) {
        super.addRow(new Object[]{
                o.getLabel(),
                o.getMass(),
                o.getX(),
                o.getY(),
                o.getZ(),
                o.getRadius(),
                o.getSpeed().getX(),
                o.getSpeed().getY(),
                o.getSpeed().getZ(),
                o.isMotionless(),
                o.getColor().getR(),
                o.getColor().getG(),
                o.getColor().getB()});
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() != TableModelEvent.DELETE) {
            refreshInitialObjects();
        }
    }

    public void refreshInitialObjects() {
        C.prop.setInitialObjects(new ArrayList<>());
        for (Vector vector : dataVector) {
            SimulationObject o = new SimulationObjectImpl();
            int i = 0;
            o.setLabel(String.valueOf(vector.get(i++)));
            o.setMass(New.num(String.valueOf(vector.get(i++))));
            o.setX(New.num(String.valueOf(vector.get(i++))));
            o.setY(New.num(String.valueOf(vector.get(i++))));
            o.setZ(New.num(String.valueOf(vector.get(i++))));
            o.setRadius(New.num(String.valueOf(vector.get(i++))));
            o.setSpeed(new TripleNumber(
                    New.num(String.valueOf(vector.get(i++))),
                    New.num(String.valueOf(vector.get(i++))),
                    New.num(String.valueOf(vector.get(i++)))));
            o.setMotionless((Boolean) vector.get(i++));
            o.setColor(new TripleInt(Integer.parseInt(String.valueOf(vector.get(i++))),
                                     Integer.parseInt(String.valueOf(vector.get(i++))),
                                     Integer.parseInt(String.valueOf(vector.get(i++)))));

            C.prop.getInitialObjects().add(o);
        }
    }
}
