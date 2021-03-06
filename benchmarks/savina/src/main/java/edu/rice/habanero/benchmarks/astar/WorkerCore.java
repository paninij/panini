/*******************************************************************************
 * This file is part of the Panini project at Iowa State University.
 *
 * @PaniniJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * @PaniniJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with @PaniniJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more details and the latest version of this code please see
 * http://paninij.org
 *
 * Contributors:
 * 	Dr. Hridesh Rajan,
 * 	Dalton Mills,
 * 	David Johnston,
 * 	Trey Erenberger
 *******************************************************************************/
package edu.rice.habanero.benchmarks.astar;

import java.util.LinkedList;
import java.util.Queue;

import org.paninij.lang.Capsule;
import org.paninij.lang.Imported;

import edu.rice.habanero.benchmarks.astar.GuidedSearchConfig.GridNode;

@Capsule class WorkerCore {
    @Imported
    Master master;

    int threshold = GuidedSearchConfig.THRESHOLD;

    void search(Work work) {

        GridNode target = work.target();
        Queue<GridNode> workQueue = new LinkedList<GridNode>();
        workQueue.add(work.node());

        int nodesProcessed = 0;

        while (!workQueue.isEmpty() && nodesProcessed < threshold) {
            nodesProcessed++;
            GuidedSearchConfig.busyWait();

            GridNode loopNode = workQueue.poll();
            int numNeighbors = loopNode.numNeighbors();

            for (int i = 0; i < numNeighbors; i++) {
                GridNode loopNeighbor = loopNode.neighbor(i);
                boolean success = loopNeighbor.setParent(loopNode);
                if (success) {
                    if (loopNeighbor.equals(target)) {
                        master.goalReached();
                        return;
                    } else {
                        workQueue.add(loopNeighbor);
                    }
                }
            }

            while (!workQueue.isEmpty()) {
                Work w = new Work(workQueue.poll(), target);
                master.sendWork(w);
            }

            master.workComplete();
        }
    }

    void done() {
        master.workerDone();
    }
}
