package org.paninij.soter;

import org.paninij.soter.cga.CallGraphAnalysis;
import org.paninij.soter.live.CallGraphLiveAnalysis;
import org.paninij.soter.model.CapsuleTemplate;
import org.paninij.soter.util.Analysis;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class SoterAnalysis implements Analysis
{
    // The analysis's dependencies:
    protected final CapsuleTemplate template;
    protected final CallGraphAnalysis cga;
    protected final CallGraphLiveAnalysis cgla;
    protected final IClassHierarchy cha;
    
    protected boolean hasBeenPerformed;

    public SoterAnalysis(CapsuleTemplate template, CallGraphAnalysis cga,
                         CallGraphLiveAnalysis cgla, IClassHierarchy cha)
    {
        this.template = template;
        this.cga = cga;
        this.cgla = cgla;
        this.cha = cha;
        
        hasBeenPerformed = false;
    }

    @Override
    public void perform()
    {
        if (hasBeenPerformed) {
            return;
        }

        cga.perform();
        cgla.perform();
        
        hasBeenPerformed = true;
    }

    public CallGraph getCallGraph()
    {
        return cga.getCallGraph();
    }
 }
