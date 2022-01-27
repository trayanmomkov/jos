package info.trekto.jos.gui;

import info.trekto.jos.C;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static info.trekto.jos.core.impl.SimulationImpl.initArrays;

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
                o.getSpeedX(),
                o.getSpeedY(),
                o.getSpeedZ(),
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
            o.setMass(Double.parseDouble(String.valueOf(v.get(i++))));
            o.setX(Double.parseDouble(String.valueOf(v.get(i++))));
            o.setY(Double.parseDouble(String.valueOf(v.get(i++))));
            o.setZ(Double.parseDouble(String.valueOf(v.get(i++))));
            o.setRadius(Double.parseDouble(String.valueOf(v.get(i++))));
            o.setSpeedX(Double.parseDouble(String.valueOf(v.get(i++))));
            o.setSpeedY(Double.parseDouble(String.valueOf(v.get(i++))));
            o.setSpeedZ(Double.parseDouble(String.valueOf(v.get(i++))));
            o.setColor(Integer.parseInt(String.valueOf(v.get(i++)), 16));

            C.prop.getInitialObjects().add(o);
        }
        initArrays(C.prop.getInitialObjects());
    }
}
