/*
 * The MIT License
 *
 * Copyright 2015 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.buildwrapper.namedlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class CompositeLock implements Lock, Comparator<SemaphoreLock> {

    private final List<SemaphoreLock> locks;

    public CompositeLock(List<SemaphoreLock> locks) {
        List<SemaphoreLock> copy = new ArrayList<>(locks);
        Collections.sort(copy, this);
        this.locks = copy;
    }

    @Override
    public void acquire() throws InterruptedException {
        for (int i = 0; i < locks.size(); i++) {
            SemaphoreLock lock = locks.get(i);
            lock.acquire();
        }
    }

    @Override
    public void release() {
        for (int i = locks.size() - 1; i >= 0; i--) {
            SemaphoreLock lock = locks.get(i);
            lock.release();
        }
    }

    @Override
    public int compare(SemaphoreLock o1, SemaphoreLock o2) {
        if (o1 == null) {
            if (o2 == null) {
                return 0;
            }
            // o2 != null:
            return 1;
        }
        // o1 != null
        if (o2 == null) {
            return -1;
        }
        return o1.getMax() - o2.getMax();
    }

}
