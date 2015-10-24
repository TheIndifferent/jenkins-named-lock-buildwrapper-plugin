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

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import jenkins.tasks.SimpleBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamedLockBuildWrapper extends SimpleBuildWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(NamedLockBuildWrapper.class);

    @Extension
    public static final NamedLockBuildWrapperDescriptor DESCRIPTOR = new NamedLockBuildWrapperDescriptor();
    private String requiredLocks;

    @DataBoundConstructor
    public NamedLockBuildWrapper(String requiredLocks) {
        this.requiredLocks = requiredLocks;
    }

    @Override
    public void setUp(Context cntxt, Run<?, ?> run, FilePath fp, Launcher lnchr, TaskListener tl, EnvVars ev) throws IOException, InterruptedException {
        LOG.info("getting locks for: {}", requiredLocks);
        final Lock lock = DESCRIPTOR.takeLock(requiredLocks);
        cntxt.setDisposer(new Disposer() {
            @Override
            public void tearDown(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
                tearDownImpl(lock);
            }
        });
        LOG.info("acquiring locks for: {}", requiredLocks);
        lock.acquire();
        LOG.info("locks acquired: {}", requiredLocks);
    }

    private void tearDownImpl(Lock lock) {
            LOG.info("releasing locks: {}", requiredLocks);
            lock.release();
            LOG.info("locks released: {}", requiredLocks);
    }

    public String getRequiredLocks() {
        return requiredLocks;
    }

    @DataBoundSetter
    public void setRequiredLocks(String requiredLocks) {
        this.requiredLocks = requiredLocks;
    }

}
