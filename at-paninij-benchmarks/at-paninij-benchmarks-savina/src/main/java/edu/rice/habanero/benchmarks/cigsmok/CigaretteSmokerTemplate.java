package edu.rice.habanero.benchmarks.cigsmok;

import org.paninij.lang.Capsule;
import org.paninij.lang.Local;

@Capsule public class CigaretteSmokerTemplate {
    @Local Arbiter arbiter;

    public void run() {
        arbiter.start();
    }
}
