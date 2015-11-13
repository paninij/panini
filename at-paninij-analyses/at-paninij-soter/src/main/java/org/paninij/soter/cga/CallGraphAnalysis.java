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
package org.paninij.soter.cga;

import org.paninij.soter.model.CapsuleTemplate;
import org.paninij.soter.util.Analysis;

import com.ibm.wala.analysis.pointers.BasicHeapGraph;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXCFABuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.IClassHierarchy;


/**
 * Builds Zero-One CFA call graph using flow insensitive Andersen style points-to analysis with
 * entrypoints stemming from the procedures of a single template class.
 */
public class CallGraphAnalysis extends Analysis
{
    public static final int INSTANCE_POLICY = ZeroXInstanceKeys.ALLOCATIONS
                                            //| ZeroXInstanceKeys.SMUSH_MANY
                                            //| ZeroXInstanceKeys.SMUSH_STRINGS
                                            | ZeroXInstanceKeys.SMUSH_THROWABLES;
    
    // Analysis dependencies:
    protected final CapsuleTemplate template;
    protected final IClassHierarchy cha;
    protected final AnalysisOptions options;
    protected final AnalysisCache cache;
    
    // Artifacts generated by performing the analysis:
    protected CallGraph callGraph;
    protected PointerAnalysis<InstanceKey> pointerAnalysis;
    protected HeapModel heapModel;
    protected HeapGraph<InstanceKey> heapGraph;
    
    public CallGraphAnalysis(CapsuleTemplate template, IClassHierarchy cha, AnalysisOptions options)
    {
        this(template, cha, options, new AnalysisCache());
    }
    
    public CallGraphAnalysis(CapsuleTemplate template, IClassHierarchy cha, AnalysisOptions options,
                             AnalysisCache cache)
    {
        this.template = template;
        this.cha = cha;
        this.options = options;
        this.cache = cache;
    }

    /**
     * This performs a zero-one CFA algorithm which simultaneously builds the call graph and
     * performs the pointer analysis. Note that by calling this function, any entrypoints stored in
     * `options` will be overridden with new entrypoints.
     * 
     * @see org.paninij.soter.util.Analysis#perform()
     */
    @Override
    @SuppressWarnings("unchecked")
    public void performAnalysis()
    {
        options.setEntrypoints(CapsuleTemplateEntrypoint.makeAll(template.getTemplateClass()));

        ContextSelector contextSelector = new ReceiverInstanceContextSelector();
        PropagationCallGraphBuilder builder = ZeroXCFABuilder.make(cha, options, cache,
                                                                   contextSelector, null,
                                                                   INSTANCE_POLICY);
        try
        {
            callGraph = builder.makeCallGraph(options, null);
            pointerAnalysis = builder.getPointerAnalysis();
            heapModel = pointerAnalysis.getHeapModel();
            heapGraph = new BasicHeapGraph(pointerAnalysis, callGraph);
        }
        catch (CallGraphBuilderCancelException ex)
        {
            String msg = "Call graph construction was unexpectedly cancelled: ";
            throw new IllegalArgumentException(msg + template.toString());
        }
    }
    
    public CallGraph getCallGraph()
    {
        assert hasBeenPerformed;
        return callGraph;
    }

    public HeapModel getHeapModel()
    {
        assert hasBeenPerformed;
        return heapModel;
    }

    public PointerAnalysis<InstanceKey> getPointerAnalysis()
    {
        assert hasBeenPerformed;
        return pointerAnalysis;
    }
    
    public HeapGraph<InstanceKey> getHeapGraph()
    {
        assert hasBeenPerformed;
        return heapGraph;
    }
}
