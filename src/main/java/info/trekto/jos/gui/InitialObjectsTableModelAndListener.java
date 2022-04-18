package info.trekto.jos.gui;

import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static info.trekto.jos.core.Controller.C;

public class InitialObjectsTableModelAndListener extends DefaultTableModel implements TableModelListener {
    Map<String, Integer> columnNameIndexMap;
    MainForm mainForm;

    public InitialObjectsTableModelAndListener(MainForm mainForm) {
        super();
        this.mainForm = mainForm;

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
        addCol("speedX", i++);
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
        C.prop.setInitialObjects(new ArrayList<>());
        for (Object vector : dataVector) {
            Vector v = (Vector) vector;
            SimulationObject o = new SimulationObjectImpl();
            int i = 0;
            o.setId(String.valueOf(v.get(i++)));
            o.setMass(New.num(String.valueOf(v.get(i++))));
            o.setX(New.num(String.valueOf(v.get(i++))));
            o.setY(New.num(String.valueOf(v.get(i++))));
            o.setZ(New.num(String.valueOf(v.get(i++))));
            o.setRadius(New.num(String.valueOf(v.get(i++))));
            o.setSpeed(new TripleNumber(
                    New.num(String.valueOf(v.get(i++))),
                    New.num(String.valueOf(v.get(i++))),
                    New.num(String.valueOf(v.get(i++)))));
            o.setColor(Integer.parseInt(String.valueOf(v.get(i++)), 16));

            C.prop.getInitialObjects().add(o);
        }
    }
}
