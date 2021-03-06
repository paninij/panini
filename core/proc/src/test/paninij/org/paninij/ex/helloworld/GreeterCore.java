package org.paninij.ex.helloworld;

import org.paninij.lang.Block;
import org.paninij.lang.Capsule;
import org.paninij.lang.Future;
import org.paninij.lang.Imported;
import org.paninij.lang.String;

@Capsule
class GreeterCore
{
    String message;
    @Imported Stream s;

    void init() {
        message = new String("Hello World!");
    }

    @Future
    long greet(boolean draw) {
        s.write(new String("Panini: " + message));
        long time = System.currentTimeMillis();
        s.write(new String("Time is now: " + time));
        return time;
    }

    @Block
    int greetBlock() {
        s.write(new String("Panini: " + message));
        long time = System.currentTimeMillis();
        s.write(new String("Time is now: " + time));
        return 42;
    }
}
