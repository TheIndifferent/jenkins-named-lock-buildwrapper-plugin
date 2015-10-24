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

import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamedLockBuildWrapperDescriptor extends BuildWrapperDescriptor {

    private static final Logger LOG = LoggerFactory.getLogger(NamedLockBuildWrapperDescriptor.class);

    NamedLockBuildWrapperDescriptor() {
        super(NamedLockBuildWrapper.class);
    }

    private String locksConfig;
    private transient Map<String, SemaphoreLock> locksMap;

    public Lock takeLock(String conf) {
        if (locksMap == null) {
            Map<String, SemaphoreLock> map = createLocksMap(locksConfig);
            synchronized (this) {
                if (locksMap == null) {
                    locksMap = map;
                }
            }
        }
        List<SemaphoreLock> locks = new ArrayList<>();
        StringTokenizer tokenizer = createTokenizer(conf);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            SemaphoreLock lock = locksMap.get(token);
            if (lock == null) {
                synchronized (locksMap) {
                    lock = locksMap.get(token);
                    if (lock == null) {
                        lock = createLock(1, token);
                        locksMap.put(token, lock);
                    }
                }
            }
            locks.add(lock);
        }
        if (locks.size() == 1) {
            return locks.get(0);
        }
        return new CompositeLock(locks);
    }

    public FormValidation doCheckLocksConfig(@QueryParameter String value) {
        try {
            createLocksMap(value);
            return FormValidation.ok();
        } catch (Exception ex) {
            return FormValidation.error(ex.getMessage());
        }
    }

    private Map<String, SemaphoreLock> createLocksMap(String conf) {
        Map<String, SemaphoreLock> map = new ConcurrentHashMap<>();
        if (conf == null || conf.trim().isEmpty()) {
            return map;
        }
        StringTokenizer tokenizer = createTokenizer(conf);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int index = token.indexOf('=');
            if (index == 0) {
                throw new IllegalArgumentException("Invalid lock: " + token);
            }
            if (index > 0) {
                String key = token.substring(0, index);
                String value = token.substring(index + 1);
                try {
                    int val = Integer.parseInt(value);
                    if (val <= 0) {
                        throw new Exception();
                    }
                    map.put(key, createLock(val, key));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid number of locks, must be positive integer: " + token);
                }
            }
        }
        return map;
    }

    private SemaphoreLock createLock(int value, String name) {
        return new SemaphoreLock(value, name);
    }

    private StringTokenizer createTokenizer(String value) {
        return new StringTokenizer(value, " ,;\t\n");
    }

    @Override
    public boolean isApplicable(AbstractProject<?, ?> ap) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "NamedLock";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        this.locksConfig = json.getString("locksConfig");
        save();
        return true;
    }

    public String getLocksConfig() {
        return locksConfig;
    }

    public void setLocksConfig(String locksConfig) {
        this.locksConfig = locksConfig;
    }

}
