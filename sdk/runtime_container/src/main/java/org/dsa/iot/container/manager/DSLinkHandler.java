package org.dsa.iot.container.manager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.dsa.iot.container.wrapper.DSLinkProvider;
import org.dsa.iot.container.wrapper.log.LogManager;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author Samuel Grenier
 */
public class DSLinkHandler {

    private final DSLinkInfo info;

    private ThreadGroup group;
    private Thread thread;

    private DSLinkProvider provider;
    private ClassLoader loader;

    public DSLinkHandler(DSLinkInfo info) {
        this.info = info;
    }

    public synchronized void start() throws IOException {
        final String name = info.getName();
        if (isRunning()) {
            String msg = "DSLink `" + name + "` is already running";
            System.err.println(msg);
            return;
        }
        System.err.println("Starting DSLink `" + name + "`");

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                try {
                    URL[] urls = info.collectJars();
                    loader = new URLClassLoader(urls);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });

        LogManager manager = new LogManager(loader);
        manager.setLevel(info.getLogLevel());

        provider = new DSLinkProvider(loader, info);
        group = new ThreadGroup(info.getName());
        group.setMaxPriority(Thread.NORM_PRIORITY);
        thread = new Thread(group, new Runnable() {
            @Override
            public void run() {
                provider.start();
                provider.sleep();
            }
        }, info.getName());
        thread.start();
        System.err.println("Started dslink `" + name + "`");
    }

    @SuppressFBWarnings("DM_GC")
    @SuppressWarnings("deprecation")
    public synchronized void stop() {
        final String name = info.getName();
        if (!isRunning()) {
            String msg = "DSLink `" + name + "` is already stopped";
            System.err.println(msg);
            return;
        }
        System.err.println("Stopping DSLink `" + name + "`");
        try {
            provider.stop();
        } catch (Throwable ignored) {
        }
        try {
            try {
                group.interrupt();
            } catch (Throwable ignored) {
            }

            try {
                group.stop();
            } catch (Throwable ignored) {
            }

            group.destroy();
        } catch (Throwable ignored) {
        } finally {
            thread = null;
            group = null;
        }
        provider = null;
        loader = null;
        System.gc();
        System.err.println("Stopped DSLink `" + name + "`");
    }

    private boolean isRunning() {
        return loader != null;
    }
}
