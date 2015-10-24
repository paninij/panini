package org.paninij.proc.check.signature;

import org.junit.Test;
import org.paninij.proc.check.AbstractTestBadTemplates;


public class TestBadTemplates extends AbstractTestBadTemplates
{
    @Override
    protected String getBadTemplatePackage() {
        return "org.paninij.proc.check.signature";
    }

    @Override
    protected Class<?> getExpectedCause() {
        return SignatureCheckException.class;
    }
    
    @Test
    public void testBadSuffix() {
        testBadTemplate("BadSuffix");
    }
    
    @Test
    public void testBadModifier() {
        testBadTemplate("BadModifierTemplate");
    }
}
