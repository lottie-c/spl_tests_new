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
package cz.cuni.mff.spl.utils.lock;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Wrapper around {@link FileLock}.
 * 
 * @author Frantisek Haas
 * 
 */
public class Lock implements AutoCloseable {

    private static final SplLog    logger   = SplLogger.getLogger(Lock.class);

    private static final int       WAIT_FOR = 100;

    private final RandomAccessFile access;
    private final FileChannel      channel;
    private final FileLock         lock;
    private final File             file;

    private Lock() {
        this.access = null;
        this.channel = null;
        this.lock = null;
        this.file = null;
    }

    private Lock(RandomAccessFile access, FileChannel channel, FileLock lock, File file) {
        this.access = access;
        this.channel = channel;
        this.lock = lock;
        this.file = file;
    }

    /**
     * Dummy method to disable unused warning in try-with-resource block.
     */
    public void dummy() {
        // nothing
    }

    /**
     * <p>
     * Tries to lock the file specified. {@link Lock#isLocked()} must be checked
     * if lock was successfully acquired. Always returns valid object.
     * 
     * @param lockFile
     *            The file to lock.
     * @return
     *         The lock on the file.
     */
    public static Lock tryLock(File lockFile) {
        final String lockType = "rw";

        RandomAccessFile access = null;
        FileChannel channel = null;
        try {
            access = new RandomAccessFile(lockFile, lockType);
            channel = access.getChannel();
            FileLock lock = channel.tryLock();

            if (lock != null) {
                logger.trace("Successfully acquired lock on [%s].", lockFile);
            } else {
                logger.trace("Failed to acquire lock on [%s].", lockFile);
            }

            return new Lock(access, channel, lock, lockFile);

        } catch (OverlappingFileLockException | IOException e) {
            logger.trace(e, "Failed to acquire lock on [%s].", lockFile);

            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e1) {
                    logger.error(e1, "Troubles releasing resources of unsuccessful lock try.");
                }
            }

            if (access != null) {
                try {
                    access.close();
                } catch (IOException e1) {
                    logger.error(e1, "Troubles releasing resources of unsuccessful lock try.");
                }
            }

            return new Lock();
        }
    }

    /**
     * <p>
     * Tries to acquire the lock forever until it's finally acquired.
     * 
     * @param lockFile
     *            The file to lock.
     * @return
     *         The definitely locked lock on the file.
     */
    public static Lock waitForLock(File lockFile) {
        Lock lock = tryLock(lockFile);

        while (!lock.isLocked()) {
            lock.close();

            try {
                Thread.sleep(WAIT_FOR);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                InvokedExecutionConfiguration.checkIfExecutionAborted();
                Thread.interrupted();
            }

            lock = tryLock(lockFile);
        }

        return lock;
    }

    /**
     * Try if file is locked by someone.
     * 
     * @param lockFile
     *            The file to check.
     * @return
     */
    public static boolean isLocked(File lockFile) {
        try (Lock lock = tryLock(lockFile)) {
            return !(lock.isLocked());
        }
    }

    /**
     * Checks if the lock is locked.
     * 
     * @return
     */
    public boolean isLocked() {
        if (lock == null) {
            return false;
        }

        return lock.isValid();
    }

    /**
     * Releases the lock.
     */
    @Override
    public void close() {
        if (file != null) {
            logger.trace("Unlocking [%s].", file.toString());
        }

        if (lock != null) {
            try {
                lock.release();
                lock.close();
            } catch (IOException e) {
                if (file != null) {
                    logger.error(e, "Troubles releasing lock [file=%s].", file.toString());
                } else {
                    logger.error(e, "Troubles releasing lock.");
                }
            }
        }

        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                if (file != null) {
                    logger.error(e, "Troubles releasing lock [file=%s].", file.toString());
                } else {
                    logger.error(e, "Troubles releasing lock.");
                }
            }
        }

        if (access != null) {
            try {
                access.close();
            } catch (IOException e) {
                if (file != null) {
                    logger.error(e, "Troubles releasing lock [file=%s].", file.toString());
                } else {
                    logger.error(e, "Troubles releasing lock.");
                }
            }
        }
    }
}
