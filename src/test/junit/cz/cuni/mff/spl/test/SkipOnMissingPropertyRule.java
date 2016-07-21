/*
 * Copyright (c) 2014, Vojtech Horky
 * Copyright (c) 2014, Charles University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package cz.cuni.mff.spl.test;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Vojtech Horky
 * 
 */
@Ignore
public class SkipOnMissingPropertyRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
        return new SkipOnMissingPropertyStatement(statement);
    }

    private static class SkipOnMissingPropertyStatement extends Statement {
        private final Statement base;

        public SkipOnMissingPropertyStatement(Statement parent) {
            this.base = parent;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                base.evaluate();
            } catch (cz.cuni.mff.spl.deploy.build.vcs.Utils.NotFoundPropertiesException propertyException) {
                Assume.assumeNoException(propertyException);
            } catch (cz.cuni.mff.spl.utils.ssh.Utils.NotFoundPropertiesException propertyException) {
                Assume.assumeNoException(propertyException);
            } finally {
            }
        }
    }
}
