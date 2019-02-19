//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.sshweb.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.schmizz.concurrent.Event;
import net.schmizz.concurrent.ExceptionChainer;
import net.schmizz.sshj.common.LoggerFactory;
import org.slf4j.Logger;

public class StreamCopier {
    private static final StreamCopier.Listener NULL_LISTENER = new StreamCopier.Listener() {
        public void reportProgress(long transferred) {
        }
    };
    private final LoggerFactory loggerFactory;
    private final Logger log;
    private final InputStream in;
    private final OutputStream out;
    private StreamCopier.Listener listener;
    private int bufSize;
    private boolean keepFlushing;
    private long length;

    public StreamCopier(InputStream in, OutputStream out, LoggerFactory loggerFactory) {
        this.listener = NULL_LISTENER;
        this.bufSize = 1;
        this.keepFlushing = true;
        this.length = -1L;
        this.in = in;
        this.out = out;
        this.loggerFactory = loggerFactory;
        this.log = loggerFactory.getLogger(this.getClass());
    }

    public StreamCopier bufSize(int bufSize) {
        this.bufSize = bufSize;
        return this;
    }

    public StreamCopier keepFlushing(boolean keepFlushing) {
        this.keepFlushing = keepFlushing;
        return this;
    }

    public StreamCopier listener(StreamCopier.Listener listener) {
        if(listener == null) {
            this.listener = NULL_LISTENER;
        } else {
            this.listener = listener;
        }

        return this;
    }

    public StreamCopier length(long length) {
        this.length = length;
        return this;
    }

    public Event<IOException> spawn(String name) {
        return this.spawn(name, false);
    }

    public Event<IOException> spawnDaemon(String name) {
        return this.spawn(name, true);
    }

    private Event<IOException> spawn(final String name, final boolean daemon) {
        final Event doneEvent = new Event("copyDone", new ExceptionChainer() {
            public IOException chain(Throwable t) {
                return t instanceof IOException?(IOException)t:new IOException(t);
            }
        }, this.loggerFactory);
        (new Thread() {
            {
                this.setName(name);
                this.setDaemon(daemon);
            }

            public void run() {
                try {
                    StreamCopier.this.log.debug("Will copy from {} to {}", StreamCopier.this.in, StreamCopier.this.out);
                    StreamCopier.this.copy();
                    StreamCopier.this.log.debug("Done copying from {}", StreamCopier.this.in);
                    doneEvent.set();
                } catch (IOException var2) {
                    StreamCopier.this.log.error(String.format("In pipe from %1$s to %2$s", new Object[]{StreamCopier.this.in.toString(), StreamCopier.this.out.toString()}), var2);
                    doneEvent.deliverError(var2);
                }

            }
        }).start();
        return doneEvent;
    }

    public long copy() throws IOException {
        byte[] buf = new byte[this.bufSize];
        long count = 0L;
        int read = 0;
        long startTime = System.currentTimeMillis();
        if(this.length == -1L) {
            while((read = this.in.read(buf)) != -1) {
                count += this.write(buf, count, read);
            }
        } else {
            while(count < this.length && (read = this.in.read(buf, 0, (int)Math.min((long)this.bufSize, this.length - count))) != -1) {
                count += this.write(buf, count, read);
            }
        }

        if(!this.keepFlushing) {
            this.out.flush();
        }

        double timeSeconds = (double)(System.currentTimeMillis() - startTime) / 1000.0D;
        double sizeKiB = (double)count / 1024.0D;
        this.log.debug(String.format("%1$,.1f KiB transferred in %2$,.1f seconds (%3$,.2f KiB/s)", new Object[]{Double.valueOf(sizeKiB), Double.valueOf(timeSeconds), Double.valueOf(sizeKiB / timeSeconds)}));
        if(this.length != -1L && read == -1) {
            throw new IOException("Encountered EOF, could not transfer " + this.length + " bytes");
        } else {
            return count;
        }
    }

    public long write(byte[] buf, long curPos, int len) throws IOException {
        this.out.write(buf, 0, len);
        if(this.keepFlushing) {
            this.out.flush();
        }

        this.listener.reportProgress(curPos + (long)len);
        return (long)len;
    }

    public interface Listener {
        void reportProgress(long var1) throws IOException;
    }
}
