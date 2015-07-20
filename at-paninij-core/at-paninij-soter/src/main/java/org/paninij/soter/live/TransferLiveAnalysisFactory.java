package org.paninij.soter.live;

import org.paninij.soter.cga.CallGraphAnalysis;
import org.paninij.soter.model.CapsuleTemplate;
import org.paninij.soter.transfer.TransferAnalysis;

import com.ibm.wala.ipa.cha.IClassHierarchy;

public class TransferLiveAnalysisFactory
{
    protected final IClassHierarchy cha;

    public TransferLiveAnalysisFactory(IClassHierarchy cha)
    {
        this.cha = cha;
    }

    public TransferLiveAnalysis make(CapsuleTemplate template, CallGraphAnalysis cga,
                                     TransferAnalysis ta)
    {
        LocalLiveAnalysisFactory llaFactory = new LocalLiveAnalysisFactory(cga);
        return new TransferLiveAnalysis(template, llaFactory, ta, cga, cha);
    }
}
