package org.objectweb.proactive.ic2d.dgc.figures;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.eclipse.draw2d.Label;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;


class DgcLabel extends Label {
    private String text;

    public DgcLabel(String text) {
        super(text);
        this.text = text;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public String getSubStringText() {
        return getText();
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }
}


public class DgcAOFigure extends AOFigure {
    public DgcAOFigure(String text) {
        super(text);
        this.label = new DgcLabel(text + "\nDGC_STATE");
        this.initFigure();
    }

    public void updateDgcState(ActiveObject model) {
        NodeObject node = model.getParent();
        String state = "";
        try {
            state = (String) model.getAttribute("DgcState");
            ((DgcLabel) this.label).setText(model.getName() + "\n" + state);
        } catch (AttributeNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
