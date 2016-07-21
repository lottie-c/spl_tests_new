/*
 * Copyright (c) 2012, František Haas, Martin Lacina, Jaroslav Kotrč, Jiří Daniel
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
package cz.cuni.mff.spl.utils.logging;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jdaniel
 * 
 */
public class LoggerTest {
    private static final SplLog LOG = SplLogger.getLogger(LoggerTest.class);

    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    @Test
    public void test() {
        LOG.fatal("FATAL test message without args");
        LOG.error("ERROR test message without args");
        LOG.warn("WARN test message without args");
        LOG.info("INFO test message without args");
        LOG.debug("DEBUG test message without args");
        LOG.trace("TRACE test message without args");

        LOG.fatal("FATAL test message with %s args", "this");
        LOG.error("ERROR test message with %s args", "this");
        LOG.warn("WARN test message with %s args", "this");
        LOG.info("INFO test message with %s args", "this");
        LOG.debug("DEBUG test message with %s args", "this");
        LOG.trace("TRACE test message with %s args", "this");

        try {
            throw new Exception("Test exception");
        } catch (Exception e) {
            LOG.fatal(e, "FATAL test message with %s args and e", "this");
            LOG.error(e, "ERROR test message with %s args and e", "this");
            LOG.warn(e, "WARN test message with %s args and e", "this");
            LOG.info(e, "INFO test message with %s args and e", "this");
            LOG.debug(e, "DEBUG test message with %s args and e", "this");
            LOG.trace(e, "TRACE test message with %s args and e", "this");
        }

        LOG.info("END");
    }
}
