/*
 * Launcher.java
 *
 * Copyright 1997 - 2001 INRIA Project Oasis. All Rights Reserved.
 * 
 * This software is the proprietary information of INRIA Sophia Antipolis.  
 * 2004 route des lucioles, BP 93 , FR-06902 Sophia Antipolis 
 * Use is subject to license terms.
 * 
 * @author  ProActive Team
 * @version ProActive 0.7 (October 2001)
 * 
 * ===================================================================
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL INRIA, THE OASIS PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ===================================================================
 * 
 */
package org.objectweb.proactive.examples.matrix;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


public class Launcher implements java.io.Serializable {

    Node[] nodesList;

    public Launcher () {}
  
    public Launcher (String[] nodesNameList) throws NodeException {
	nodesList = new Node[nodesNameList.length];
	for (int i=0; i<nodesNameList.length; i++) {
	    nodesList[i] = NodeFactory.getNode(nodesNameList[i]);
	}
    }


    // MAIN !!!
    public void start(Matrix m1, Matrix m2, int i) {	
	// DISTRIBUTED MULTIPLICATION      
	int matrixSize=m1.getWidth();


	long startTime;
 	long endTime;
	
	startTime = System.currentTimeMillis();

	//System.out.println("Multiplication!!!!! ");
	Matrix groupResult =multiply(m1,m2/*group*/);

	//endTime = System.currentTimeMillis() - startTime;
	//System.out.println("     Distributed Multiplication : " + endTime + " millisecondes\n");
	

	//startTime = System.currentTimeMillis();
	 
	// RECONSTRUCTION
	try {
	    Matrix result = reconstruction(groupResult,matrixSize); }
	catch (Exception e) {}
	

	endTime = System.currentTimeMillis() - startTime;
	System.out.println("\n       Result (" +i+ ") : Total time spent = " + endTime + " millisecondes");

	//System.out.println(result);
    }


    public Matrix createMatrix(int size) {
	Matrix m = new Matrix(size,size);
	m.initializeWithRandomValues();
	return m;
    }


    public Matrix distribute (Matrix m) {
	Matrix verticalSubMatrixGroup = null;
	verticalSubMatrixGroup = m.transformIntoActiveVerticalSubMatrixGroup(nodesList);

	return verticalSubMatrixGroup;
    }


    public Matrix multiply (Matrix m, Matrix group) {
	Matrix ma = group.localMultiplyForGroup(m);
	return ma;
    }


    public Matrix reconstruction (Matrix group, int size) {
	Matrix result = null;

	result = new Matrix(group,size);

	return result;
    }
    

    public String getString(Matrix m) {
	return m.toString();
    }

}
