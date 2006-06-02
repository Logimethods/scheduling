package org.objectweb.proactive.loadbalancing.util;


import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.node.P2PNodeLookup;



/**
 * @author cdelbe
 *
 */
public class JacobiDispatcher {

	static final public double boudaryValue = 0;
	static final public double initialValue = 1000000000;
	protected Vector nodes;
	protected int nodesBooked;
	protected P2PNodeLookup p2pNodeLookup;
	
	static public void createVirtualGrid(JacobiWorker[] workers, int workerGridSize){
			System.out.println("[JACOBI] Creating workers virtual grid...");
			int nbWorker = workers.length;
			for (int i=0;i<nbWorker;i++){
					System.out.println("[JACOBI] Initializing worker " + i + "..."); 
					JacobiWorker currentWorker = workers[i];
					JacobiWorker up=null, down=null, left=null, right=null;
					if (nbWorker == 1) continue;
					else 
					if (i==0) { // upper left
						left=null;
						up=null;
						right=workers[1];
						down=workers[workerGridSize];
					} else if ( i==(workerGridSize-1) ){ // upper right
						up=null;
						right=null;
						left=workers[i-1];
						down=workers[i+workerGridSize];	
					} else if (i==(nbWorker-workerGridSize)){ //lower left
						left=null;
						down=null;
						up=workers[i-workerGridSize];
						right=workers[i+1];
					} else if (i==nbWorker-1) { //lower right
						down=null;
						right=null;
						up=workers[i-workerGridSize];
						left=workers[i-1];
					} else if ( (i>0) && (i<(workerGridSize-1)) ) { // up
						up=null;
						right=workers[i+1];
						left=workers[i-1];
						if (i+workerGridSize < nbWorker) down=workers[i+workerGridSize];
					} else if ( (i>(workerGridSize*(workerGridSize-1))) && (i<(workerGridSize*workerGridSize-1)) ) { // down
						up=workers[i-workerGridSize];
						right=workers[i+1];
						left=workers[i-1];
						down=null;
					} else if ( i%workerGridSize == 0 ){ // left
						left=null;
						right=workers[i+1];
						up=workers[i-workerGridSize];
						if (i+workerGridSize < nbWorker) down=workers[i+workerGridSize];
					} else if ( i%workerGridSize == (workerGridSize-1)) {//right
						right=null;
						left=workers[i-1];
						if (i+workerGridSize < nbWorker) down=workers[i+workerGridSize];
						up=workers[i-workerGridSize];
					} else { // inside
						up=workers[i-workerGridSize];
						if (i+workerGridSize < nbWorker)	down=workers[i+workerGridSize];
						else down=null;
						left=workers[i-1];
						right=workers[i+1];
					}
					currentWorker.setNeighbours(up, down, left, right);
				}
			System.out.println("[JACOBI] Virtual grid is created.");
		}


	static public void createAndSplitMatrix(JacobiWorker[] workers, int workerGridSize, int subMatrixSize, int globalMatrixSize){
		System.out.println("[JACOBI] Creating and spliting global matrix");
		for (int currentW=0 ; currentW<workers.length ; currentW++){
			//create submatrix
			double[][] currentSubMatrix = new double[subMatrixSize][subMatrixSize];
			for (int i=0;i<subMatrixSize;i++){
				for(int j=0;j<subMatrixSize;j++){
					// **** HERE MATRIX VALUES ****
					//currentSubMatrix[i][j] = (double)( (i*j*(currentW+1)) );
					currentSubMatrix[i][j] = JacobiDispatcher.initialValue;
				}
			}
			// compute upperLeft values
			int upperLeftX = (currentW % workerGridSize)*subMatrixSize;
			int upperLeftY = (currentW / workerGridSize)*subMatrixSize;
			
			//send submatrix to worker
			workers[currentW].setSubMatrix(globalMatrixSize,subMatrixSize,upperLeftX,upperLeftY,
											currentSubMatrix);			
		}
		System.out.println("[JACOBI]Global Matrix created and splitted");
	
	}



	

/*	public static void main(String[] args) throws IOException, ProActiveException, AlreadyBoundException {
		
		// cmd : java Start globalSize nbWorker maxIter
		
		if (args.length != 3){
			System.err.println("java Start globalSize nbWorker maxIter");
			System.exit(1);
		}
		Node n = NodeFactory.getNode("rmi://anaconda:2805/StartTest");
		P2PTest myTest= (P2PTest) ProActive.newActive(P2PTest.class.getName(),args,n);;
	}
*/
	public JacobiDispatcher() {}
	
public JacobiDispatcher(String s1, String s2, String s3, P2PService serviceP2P) throws ProActiveException {
		int globalSize = Integer.parseInt((String) s1);
		int nbWorker =  Integer.parseInt((String) s2);
		int maxIter =  Integer.parseInt((String) s3);
	
		
		int workerGridSize = (int) Math.sqrt((double)(nbWorker));
		int submatrixSize = globalSize/workerGridSize;
		JacobiWorker[] workers = new JacobiWorker[nbWorker];
		nodesBooked = 0;
		
		System.out.println("[JACOBI] Initialization with :");
		System.out.println("         * global matrix size = " + globalSize);
		System.out.println("         * sub matrix size    = " + submatrixSize);
		System.out.println("         * # of workers       = " + nbWorker);
		System.out.println("         * worker grid size   = " + workerGridSize);
		System.out.println("         * # of iterations    = " + maxIter);
		System.out.println("         * boundary value     = " + JacobiDispatcher.boudaryValue);
		
		try {
			
		 	ProActiveConfiguration.load();
		 			 	
		 	int n = 8;
		 	P2PNodeLookup p2pNodeLookup = serviceP2P.getNodes(n, "JacobiNode", "Jacobi");
		 
		 	Vector nodes = p2pNodeLookup.getNodes();
		 	
			System.out.println("[JACOBI] Deploying Workers in "+nodes.size()+" nodes");
			for (int i=0; i<nbWorker ;i++){
				int fix = (int) (Math.random()*n);
				workers[i]=(JacobiWorker)(ProActive.newActive(JacobiWorker.class.getName(), 
					 					new Object[]{new Integer(i), new Double(JacobiDispatcher.boudaryValue), new Integer(maxIter)}, 
										(Node) nodes.get(fix)));
			}
			System.out.println("[JACOBI] Workers are deployed");

			//initializing workers
			JacobiDispatcher.createVirtualGrid(workers, workerGridSize);
			JacobiDispatcher.createAndSplitMatrix(workers, workerGridSize, submatrixSize, globalSize);
			
			for (int i=0;i<nbWorker;i++){
				workers[i].computeNewSubMatrix();
			}
			
			
		} catch (NodeException e) {
			e.printStackTrace();
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	} 
}
