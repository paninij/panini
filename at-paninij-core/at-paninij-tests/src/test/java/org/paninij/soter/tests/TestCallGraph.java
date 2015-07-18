package org.paninij.soter.tests;

import static org.paninij.apt.util.PaniniArtifactCompiler.buildEffectiveClassPath;

import org.junit.Test;
import org.paninij.soter.PaniniAnalysisPrototype;
import org.paninij.soter.util.WalaUtil;


public class TestCallGraph
{
    private static final String CLASSPATH_FILE = "target/generated-resources/maven/panini_processor_classpath.txt";
    private static final String CLASSPATH = buildEffectiveClassPath("target/test-classes", CLASSPATH_FILE);
    
    @Test
    public void testCallGraphWithLeakyServer() throws Throwable
    {
        makeCallGraph("Lorg/paninij/soter/tests/LeakyServerTemplate", CLASSPATH, "logs/LeakyServerCallGraph.pdf");
    }
    

    @Test
    public void testCallGraphWithActiveClient() throws Throwable
    {
        makeCallGraph("Lorg/paninij/soter/tests/ActiveClientTemplate", CLASSPATH, "logs/ActiveClientCallGraph.pdf");
    }
    

    private void makeCallGraph(String template, String classPath, String pdfName) throws Throwable
    {
        PaniniAnalysisPrototype analysis = new PaniniAnalysisPrototype(template, classPath);
        analysis.perform();
        WalaUtil.makeGraphFile(analysis.getCallGraph(), pdfName);
    }
}
