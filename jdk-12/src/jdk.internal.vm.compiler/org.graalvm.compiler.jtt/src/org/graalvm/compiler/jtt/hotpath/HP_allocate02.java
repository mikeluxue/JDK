/*
 * Copyright (c) 2007, 2018, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */


package org.graalvm.compiler.jtt.hotpath;

import org.junit.Test;

import org.graalvm.compiler.jtt.JTTTest;

/*
 */
public class HP_allocate02 extends JTTTest {

    @SuppressWarnings({"deprecation", "unused"})
    public static int test(int count) {
        int sum = 0;
        for (int i = 0; i < count; i++) {
            final Integer j = new Integer(i);
            sum += j;
        }
        return sum;
    }

    @Test
    public void run0() throws Throwable {
        runTest("test", 100);
    }

}
