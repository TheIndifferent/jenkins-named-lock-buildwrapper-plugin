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

import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SemaphoreLock implements Lock {

    private static final Logger LOG = LoggerFactory.getLogger(SemaphoreLock.class);
    private final int max;
    private final String name;
    private final Semaphore semaphore;

    public SemaphoreLock(int max, String name) {
        this.max = max;
        this.name = name;
        this.semaphore = new Semaphore(max);
    }

    @Override
    public void acquire() throws InterruptedException {
        LOG.info("acquiring lock: {}", name);
        semaphore.acquire();
        LOG.info("lock acquired: {}", name);
    }

    @Override
    public void release() {
        LOG.info("releasing lock: {}", name);
        semaphore.release();
        LOG.info("lock released: {}", name);
    }

    int getMax() {
        return max;
    }

}
