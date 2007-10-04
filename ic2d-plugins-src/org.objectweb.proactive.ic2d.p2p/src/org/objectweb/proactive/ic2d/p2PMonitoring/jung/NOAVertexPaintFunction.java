package org.objectweb.proactive.ic2d.p2PMonitoring.jung;

import java.awt.Color;
import java.awt.Paint;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;


public class NOAVertexPaintFunction implements VertexPaintFunction {
    public NOAVertexPaintFunction() {
        super();
    }

    public Paint getDrawPaint(Vertex v) {
        return Color.black;
    }

    public Paint getFillPaint(Vertex v) {
        int noa = v.degree();
        switch (noa) {
        case 0:
            return Color.BLACK;
        case 1:
            return Color.GRAY;
        case 3:
            return Color.BLUE;
        case 5:
            return Color.GREEN;
        case 10:
            return Color.RED;
        default:
            return Color.ORANGE;
        }
    }

    //
    //	public Color getForeColor(Vertex v) {
    //		return Color.BLACK;
    //	}
    //
    //	public Color getBackColor(Vertex v) {
    //	//	int noa =((P2PUndirectedSparseVertex)v).getNoa();
    //		int noa=v.degree();
    //		switch (noa) {
    //			case 0: return Color.BLACK;
    //			case 1: return Color.GRAY;
    //			case 3: return Color.BLUE;
    //			case 5: return Color.GREEN;
    //			case 10: return Color.RED;
    //			default : return Color.ORANGE;
    //		}		
    //	}
}
