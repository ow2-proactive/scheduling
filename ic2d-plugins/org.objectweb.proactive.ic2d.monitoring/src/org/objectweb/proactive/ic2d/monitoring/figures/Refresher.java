package org.objectweb.proactive.ic2d.monitoring.figures;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.draw2d.Figure;

public class Refresher implements Runnable{

	private static Refresher instance;

	private Set<Figure> figures;

	private boolean isActive;

	/** Time To Sleep (in seconds) */
	private float tts = 0.5f;

	private Refresher(){
		figures = Collections.synchronizedSet(new HashSet<Figure>());
		isActive=true;
	}

	public static Refresher getInstance(){
		if(instance == null)
			instance = new Refresher();
		return instance;
	}

//	public void repaint(Figure figure){
//		if(figure!=null)
//			figures.add(figure);
//	}

	public void run() {
		int it = 0;
		while(it != 10){
			it++;
			synchronized(figures) {
				Iterator i = figures.iterator(); // Must be in the synchronized block
				while (i.hasNext()){
					Figure figure = (Figure) i.next();
					//figure.repaint();
				}
				figures.clear();
			}
			try {
				Thread.sleep((long) (tts * 1000));
				System.out.println("Refresher.run() REVEIL -- "+figures.size());
			} catch (InterruptedException e) {/* Do nothing */}
		}
	}	
}
