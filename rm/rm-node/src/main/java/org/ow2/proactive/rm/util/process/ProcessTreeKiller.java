/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.rm.util.process;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.ow2.proactive.utils.FileToBytesConverter;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import org.jvnet.winp.WinProcess;
import org.jvnet.winp.WinpException;

import static com.sun.jna.Pointer.NULL;
import static org.ow2.proactive.rm.util.process.GNUCLibrary.LIBC;


/**
 * Kills a process tree to clean up the mess left by a build.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.201
 */
public abstract class ProcessTreeKiller {

    protected static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(ProcessTreeKiller.class);

    /**
     * Kills the given process (like {@link Process#destroy()}
     * but also attempts to kill descendant processes created from the given
     * process.
     *
     * <p>
     * The implementation is obviously OS-dependent.
     *
     * <p>
     * The execution doesn't have to be blocking; the method may return
     * before processes are actually killed.
     * @param proc the process to kill.
     */
    public abstract void kill(Process proc);

    /**
     * In addition to what {@link #kill(Process)} does, also tries to
     * kill all the daemon processes launched.
     *
     * <p>
     * Daemon processes are hard to find because they normally detach themselves
     * from the parent process. In this method, the method is given a
     * "model environment variables", which is a list of environment variables
     * and their values that are characteristic to the launched process.
     * The implementation is expected to find processes
     * in the system that inherit these environment variables, and kill
     * them even if the {@code proc} and such processes do not have direct
     * ancestor/descendant relationship. 
     * @param proc the process to kill.
     * @param modelEnvVars the model environment variables characterizing the process.
     */
    public abstract void kill(Process proc, Map<String, String> modelEnvVars);

    /**
     * Similar to kill(proc, modelEnvVars but doesn't require a main process, simply kill all processes which map a specific
     * Env model
     * @param modelEnvVars the model environment variables characterizing the process.
     */
    public abstract void kill(Map<String, String> modelEnvVars);

    /**
     * Creates a magic cookie that can be used as the model environment variable
     * when we later kill the processes.
     * 
     * @return the magic cookie.
     */
    public static String createCookie() {
        return UUID.randomUUID().toString();
    }

    /**
     * Gets the {@link ProcessTreeKiller} suitable for the current system
     * that JVM runs in, or in the worst case return the default one
     * that's not capable of killing descendants at all.
     * @return the Process Tree Killer.
     */
    public static ProcessTreeKiller get() {
        if (File.pathSeparatorChar == ';')
            return new Windows();
        String os = fixNull(System.getProperty("os.name"));
        if (os.equals("Linux"))
            return new Linux();
        if (os.equals("SunOS"))
            return new Solaris();
        if (os.equals("Mac OS X"))
            return new Darwin();
        return DEFAULT;
    }

    /**
     * Given the environment variable of a process and the "model environment variable" that Hudson
     * used for launching the build, returns true if there's a match (which means the process should
     * be considered a descendant of a build.) 
     */
    protected boolean hasMatchingEnvVars(Map<String, String> envVar, Map<String, String> modelEnvVar) {
        if (modelEnvVar.isEmpty())
            // sanity check so that we don't start rampage.
            return false;

        for (Entry<String, String> e : modelEnvVar.entrySet()) {
            String v = envVar.get(e.getKey());
            logger.debug("Matching Env Var " + e.getKey() + " , expected: " + e.getValue() + " found: " + v);
            if (v == null || !v.equals(e.getValue()))
                return false; // no match
        }
        return true;
    }

    /**
     * Fallback implementation that doesn't do anything clever.
     */
    private static final ProcessTreeKiller DEFAULT = new ProcessTreeKiller() {
        @Override
        public void kill(Process proc) {
            proc.destroy();
        }

        @Override
        public void kill(Process proc, Map<String, String> modelEnvVars) {
            proc.destroy();
        }

        @Override
        public void kill(Map<String, String> modelEnvVars) {
            // do nothing
        }
    };

    /**
     * Implementation for Windows.
     *
     * <p>
     * Not a singleton pattern because loading this class requires Windows specific library.
     */
    private static final class Windows extends ProcessTreeKiller {
        /**
         * @see ProcessTreeKiller#kill(java.lang.Process)
         */
        @Override
        public void kill(Process proc) {
            new WinProcess(proc).killRecursively();
        }

        /**
         * @see ProcessTreeKiller#kill(java.lang.Process, java.util.Map)
         */
        @Override
        public void kill(Process proc, Map<String, String> modelEnvVars) {
            kill(proc);

            kill(modelEnvVars);
        }

        @Override
        public void kill(Map<String, String> modelEnvVars) {
            if (modelEnvVars != null) {
                for (WinProcess p : WinProcess.all()) {
                    if (p.getPid() < 10)
                        continue; // ignore system processes like "idle process"
                    if (logger.isDebugEnabled()) {
                        try {
                            String cmdLine = p.getCommandLine();
                            logger.debug("Analysing process " + p.getPid() + " : " + cmdLine);
                        } catch (Exception e) {
                            logger.debug("Analysing process " + p.getPid() + " : (cmd line unacessible)");
                        }

                    }
                    boolean matched;
                    try {
                        matched = hasMatchingEnvVars(p.getEnvironmentVariables(), modelEnvVars);
                    } catch (WinpException e) {
                        logger.debug(e.getMessage());
                        // likely a missing privilege
                        continue;
                    }

                    if (matched)
                        p.killRecursively();
                }
            }
        }

        static {
            WinProcess.enableDebugPrivilege();
        }
    }

    /**
     * Implementation for Unix that supports reasonably powerful <tt>/proc</tt> FS.
     */
    private static abstract class Unix<S extends Unix.UnixSystem<?>> extends ProcessTreeKiller {
        /**
         * @see ProcessTreeKiller#kill(java.lang.Process)
         */
        @Override
        public void kill(Process proc) {
            kill(proc, null);
        }

        protected abstract S createSystem();

        /**
         * @see ProcessTreeKiller#kill(java.lang.Process, java.util.Map)
         */
        @Override
        public void kill(Process proc, Map<String, String> modelEnvVars) {
            S system = createSystem();
            UnixProcess p;
            try {
                Field pid = proc.getClass().getDeclaredField("pid");
                pid.setAccessible(true);
                p = system.get((Integer) pid.get(proc));
            } catch (Exception e) { // impossible
                IllegalAccessError x = new IllegalAccessError();
                x.initCause(e);
                throw x;
            }

            if (modelEnvVars == null) {
                p.killRecursively();
            } else {
                kill(modelEnvVars);
            }
        }

        @Override
        public void kill(Map<String, String> modelEnvVars) {
            if (modelEnvVars != null) {
                S system = createSystem();
                for (UnixProcess lp : system) {
                    if (hasMatchingEnvVars(lp.getEnvVars(), modelEnvVars))
                        lp.kill();
                }
            }
        }

        /**
         * Method to destroy a process, given pid.
         */
        private static final Method DESTROY_PROCESS;

        static {
            try {
                Class<?> clazz = Class.forName("java.lang.UNIXProcess");
                Method destroy;
                try {
                    destroy = clazz.getDeclaredMethod("destroyProcess", int.class);
                } catch (NoSuchMethodException ex) {
                    destroy = clazz.getDeclaredMethod("destroyProcess", int.class, boolean.class);
                }
                DESTROY_PROCESS = destroy;
                DESTROY_PROCESS.setAccessible(true);
            } catch (ClassNotFoundException e) {
                LinkageError x = new LinkageError();
                x.initCause(e);
                throw x;
            } catch (NoSuchMethodException e) {
                LinkageError x = new LinkageError();
                x.initCause(e);
                throw x;
            }
        }

        /**
         * Represents a single Unix system, which hosts multiple processes.
         *
         * <p>
         * The object represents a snapshot of the system state.
         */
        static abstract class UnixSystem<P extends UnixProcess<P>> implements Iterable<P> {
            /**
             * To be filled in the constructor of the derived type.
             */
            protected final Map<Integer/*pid*/, P> processes = new HashMap<Integer, P>();

            public P get(int pid) {
                return processes.get(pid);
            }

            public Iterator<P> iterator() {
                return processes.values().iterator();
            }
        }

        /**
         * {@link UnixSystem} that has /proc.
         */
        static abstract class ProcfsUnixSystem<P extends UnixProcess<P>> extends UnixSystem<P> {
            ProcfsUnixSystem() {
                File[] processes = new File("/proc").listFiles(new FileFilter() {
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }
                });
                if (processes == null) {
                    logger.info("No /proc");
                    return;
                }

                for (File p : processes) {
                    int pid;
                    try {
                        pid = Integer.parseInt(p.getName());
                    } catch (NumberFormatException e) {
                        // other sub-directories
                        continue;
                    }
                    try {
                        this.processes.put(pid, createProcess(pid));
                    } catch (IOException e) {
                        // perhaps the process status has changed since we obtained a directory listing
                    }
                }
            }

            protected abstract P createProcess(int pid) throws IOException;

        }

        /**
         * A process.
         */
        public static abstract class UnixProcess<P extends UnixProcess<P>> {
            public final UnixSystem<P> system;

            protected UnixProcess(UnixSystem<P> system) {
                this.system = system;
            }

            public abstract int getPid();

            /**
             * Gets the parent process. This method may return null, because
             * there's no guarantee that we are getting a consistent snapshot
             * of the whole system state.
             */
            public abstract P getParent();

            protected final File getFile(String relativePath) {
                return new File(new File("/proc/" + getPid()), relativePath);
            }

            /**
             * Immediate child processes.
             */
            public List<P> getChildren() {
                List<P> r = new ArrayList<P>();
                for (P p : system)
                    if (p.getParent() == this)
                        r.add(p);
                return r;
            }

            /**
             * Tries to kill this process.
             */
            public void kill() {
                try {
                    if (DESTROY_PROCESS.getParameterTypes().length > 1) {
                        DESTROY_PROCESS.invoke(null, getPid(), false);
                    } else {
                        DESTROY_PROCESS.invoke(null, getPid());
                    }
                } catch (IllegalAccessException e) {
                    // this is impossible
                    IllegalAccessError x = new IllegalAccessError();
                    x.initCause(e);
                    throw x;
                } catch (InvocationTargetException e) {
                    // tunnel serious errors
                    if (e.getTargetException() instanceof Error)
                        throw (Error) e.getTargetException();
                    // otherwise log and let go. I need to see when this happens
                    logger.info("Failed to terminate pid=" + getPid(), e);
                }

            }

            public void killRecursively() {
                for (P p : getChildren())
                    p.killRecursively();
                kill();
            }

            /**
             * Obtains the environment variables of this process.
             *
             * @return
             *      empty map if failed (for example because the process is already dead,
             *      or the permission was denied.)
             */
            public abstract EnvVars getEnvVars();
        }
    }

    /**
     * Implementation for Linux that uses <tt>/proc</tt>.
     */
    private static final class Linux extends Unix<Linux.LinuxSystem> {
        @Override
        protected LinuxSystem createSystem() {
            return new LinuxSystem();
        }

        static class LinuxSystem extends Unix.ProcfsUnixSystem<LinuxProcess> {
            @Override
            protected LinuxProcess createProcess(int pid) throws IOException {
                return new LinuxProcess(this, pid);
            }
        }

        static class LinuxProcess extends Unix.UnixProcess<LinuxProcess> {
            private final int pid;
            private int ppid = -1;
            private EnvVars envVars;

            LinuxProcess(LinuxSystem system, int pid) throws IOException {
                super(system);
                this.pid = pid;
                BufferedReader r = new BufferedReader(new FileReader(getFile("status")));
                try {
                    String line;
                    while ((line = r.readLine()) != null) {
                        line = line.toLowerCase(Locale.ENGLISH);
                        if (line.startsWith("ppid:")) {
                            ppid = Integer.parseInt(line.substring(5).trim());
                            break;
                        }
                    }
                } finally {
                    r.close();
                }
                if (ppid == -1)
                    throw new IOException("Failed to parse PPID from /proc/" + pid + "/status");
            }

            @Override
            public int getPid() {
                return pid;
            }

            @Override
            public LinuxProcess getParent() {
                return system.get(ppid);
            }

            @Override
            public synchronized EnvVars getEnvVars() {
                if (envVars != null)
                    return envVars;
                envVars = new EnvVars();
                try {
                    byte[] environ = FileToBytesConverter.convertFileToByteArray(getFile("environ"));
                    int pos = 0;
                    for (int i = 0; i < environ.length; i++) {
                        byte b = environ[i];
                        if (b == 0) {
                            envVars.addLine(new String(environ, pos, i - pos));
                            pos = i + 1;
                        }
                    }
                } catch (Exception e) {
                    // failed to read. this can happen under normal circumstances (most notably permission denied)
                    // so don't report this as an error.
                }
                return envVars;
            }
        }
    }

    /**
     * Implementation for Solaris that uses <tt>/proc</tt>.
     *
     * Amazingly, this single code works for both 32bit and 64bit Solaris, despite the fact
     * that does a lot of pointer manipulation and what not.
     */
    private static final class Solaris extends Unix<Solaris.SolarisSystem> {
        @Override
        protected SolarisSystem createSystem() {
            return new SolarisSystem();
        }

        static class SolarisSystem extends Unix.ProcfsUnixSystem<SolarisProcess> {
            @Override
            protected SolarisProcess createProcess(int pid) throws IOException {
                return new SolarisProcess(this, pid);
            }
        }

        static class SolarisProcess extends Unix.UnixProcess<SolarisProcess> {
            private final int pid;
            private final int ppid;
            /**
             * Address of the environment vector. Even on 64bit Solaris this is still 32bit pointer.
             */
            private final int envp;
            private EnvVars envVars;

            SolarisProcess(SolarisSystem system, int pid) throws IOException {
                super(system);
                this.pid = pid;

                RandomAccessFile psinfo = new RandomAccessFile(getFile("psinfo"), "r");
                try {
                    //typedef struct psinfo {
                    //	int	pr_flag;	/* process flags */
                    //	int	pr_nlwp;	/* number of lwps in the process */
                    //	pid_t	pr_pid;	/* process id */
                    //	pid_t	pr_ppid;	/* process id of parent */
                    //	pid_t	pr_pgid;	/* process id of process group leader */
                    //	pid_t	pr_sid;	/* session id */
                    //	uid_t	pr_uid;	/* real user id */
                    //	uid_t	pr_euid;	/* effective user id */
                    //	gid_t	pr_gid;	/* real group id */
                    //	gid_t	pr_egid;	/* effective group id */
                    //	uintptr_t	pr_addr;	/* address of process */
                    //	size_t	pr_size;	/* size of process image in Kbytes */
                    //	size_t	pr_rssize;	/* resident set size in Kbytes */
                    //	dev_t	pr_ttydev;	/* controlling tty device (or PRNODEV) */
                    //	ushort_t	pr_pctcpu;	/* % of recent cpu time used by all lwps */
                    //	ushort_t	pr_pctmem;	/* % of system memory used by process */
                    //	timestruc_t	pr_start;	/* process start time, from the epoch */
                    //	timestruc_t	pr_time;	/* cpu time for this process */
                    //	timestruc_t	pr_ctime;	/* cpu time for reaped children */
                    //	char	pr_fname[PRFNSZ];	/* name of exec'ed file */
                    //	char	pr_psargs[PRARGSZ];	/* initial characters of arg list */
                    //	int	pr_wstat;	/* if zombie, the wait() status */
                    //	int	pr_argc;	/* initial argument count */
                    //	uintptr_t	pr_argv;	/* address of initial argument vector */
                    //	uintptr_t	pr_envp;	/* address of initial environment vector */
                    //	char	pr_dmodel;	/* data model of the process */
                    //	lwpsinfo_t	pr_lwp;	/* information for representative lwp */
                    //} psinfo_t;

                    // see http://cvs.opensolaris.org/source/xref/onnv/onnv-gate/usr/src/uts/common/sys/types.h
                    // for the size of the various datatype.

                    psinfo.seek(8);
                    if (adjust(psinfo.readInt()) != pid)
                        throw new IOException("psinfo PID mismatch"); // sanity check
                    ppid = adjust(psinfo.readInt());

                    psinfo.seek(196); // now jump to pr_envp
                    envp = adjust(psinfo.readInt());
                } finally {
                    psinfo.close();
                }
                if (ppid == -1)
                    throw new IOException("Failed to parse PPID from /proc/" + pid + "/status");
            }

            /**
             * {@link DataInputStream} reads a value in big-endian, so
             * convert it to the correct value on little-endian systems.
             */
            private int adjust(int i) {
                if (IS_LITTLE_ENDIAN)
                    return (i << 24) | ((i << 8) & 0x00FF0000) | ((i >> 8) & 0x0000FF00) | (i >>> 24);
                else
                    return i;
            }

            @Override
            public int getPid() {
                return pid;
            }

            @Override
            public SolarisProcess getParent() {
                return system.get(ppid);
            }

            @Override
            public synchronized EnvVars getEnvVars() {
                if (envVars != null)
                    return envVars;
                envVars = new EnvVars();

                try {
                    RandomAccessFile as = new RandomAccessFile(getFile("as"), "r");
                    try {
                        for (int n = 0;; n++) {
                            // read a pointer to one entry
                            as.seek(to64(envp + n * 4));
                            int p = as.readInt();
                            if (p == 0)
                                break; // completed the walk
                            // now read the null-terminated string
                            as.seek(to64(p));
                            ByteArrayOutputStream buf = new ByteArrayOutputStream();
                            int ch;
                            while ((ch = as.read()) != 0)
                                buf.write(ch);
                            envVars.addLine(buf.toString());
                        }
                    } finally {
                        // failed to read. this can happen under normal circumstances,
                        // so don't report this as an error.
                        as.close();
                    }
                } catch (IOException e) {
                    // failed to read. this can happen under normal circumstances (most notably permission denied)
                    // so don't report this as an error.
                }

                return envVars;
            }

            /**
             * int to long conversion with zero-padding.
             */
            private static long to64(int i) {
                return i & 0xFFFFFFFFL;
            }
        }
    }

    /**
     * Implementation for Mac OS X based on sysctl(3).
     */
    private static final class Darwin extends Unix<Darwin.DarwinSystem> {
        protected DarwinSystem createSystem() {
            return new DarwinSystem();
        }

        static class DarwinSystem extends Unix.UnixSystem<DarwinProcess> {
            DarwinSystem() {
                String arch = System.getProperty("sun.arch.data.model");
                if ("64".equals(arch)) {
                    sizeOf_kinfo_proc = sizeOf_kinfo_proc_64;
                    kinfo_proc_pid_offset = kinfo_proc_pid_offset_64;
                    kinfo_proc_ppid_offset = kinfo_proc_ppid_offset_64;
                } else {
                    sizeOf_kinfo_proc = sizeOf_kinfo_proc_32;
                    kinfo_proc_pid_offset = kinfo_proc_pid_offset_32;
                    kinfo_proc_ppid_offset = kinfo_proc_ppid_offset_32;
                }
                try {
                    IntByReference _ = new IntByReference(sizeOfInt);
                    IntByReference size = new IntByReference(sizeOfInt);
                    Memory m;
                    int nRetry = 0;
                    while (true) {
                        // find out how much memory we need to do this
                        if (LIBC.sysctl(MIB_PROC_ALL, 3, NULL, size, NULL, _) != 0)
                            throw new IOException("Failed to obtain memory requirement: " +
                                LIBC.strerror(Native.getLastError()));

                        // now try the real call
                        m = new Memory(size.getValue());
                        if (LIBC.sysctl(MIB_PROC_ALL, 3, m, size, NULL, _) != 0) {
                            if (Native.getLastError() == ENOMEM && nRetry++ < 16)
                                continue; // retry
                            throw new IOException("Failed to call kern.proc.all: " +
                                LIBC.strerror(Native.getLastError()));
                        }
                        break;
                    }

                    int count = size.getValue() / sizeOf_kinfo_proc;
                    logger.debug("Found " + count + " processes");

                    for (int base = 0; base < size.getValue(); base += sizeOf_kinfo_proc) {
                        int pid = m.getInt(base + kinfo_proc_pid_offset);
                        int ppid = m.getInt(base + kinfo_proc_ppid_offset);

                        super.processes.put(pid, new DarwinProcess(this, pid, ppid));
                    }
                } catch (IOException e) {
                    logger.warn("Failed to obtain process list", e);
                }
            }

            private final int sizeOf_kinfo_proc;
            private final int kinfo_proc_pid_offset;
            private final int kinfo_proc_ppid_offset;

        }

        static class DarwinProcess extends Unix.UnixProcess<DarwinProcess> {
            private final int pid;
            private final int ppid;
            private EnvVars envVars;
            private List<String> arguments;

            DarwinProcess(DarwinSystem system, int pid, int ppid) {
                super(system);
                this.pid = pid;
                this.ppid = ppid;
            }

            public int getPid() {
                return pid;
            }

            public DarwinProcess getParent() {
                return system.get(ppid);
            }

            public synchronized EnvVars getEnvVars() {
                if (envVars != null)
                    return envVars;
                parse();
                return envVars;
            }

            public List<String> getArguments() {
                if (arguments != null)
                    return arguments;
                parse();
                return arguments;
            }

            private void parse() {
                try {
                    // allocate them first, so that the parse error wil result in empty data
                    // and avoid retry.
                    arguments = new ArrayList<String>();
                    envVars = new EnvVars();

                    IntByReference _ = new IntByReference();

                    IntByReference argmaxRef = new IntByReference(0);
                    IntByReference size = new IntByReference(sizeOfInt);

                    // for some reason, I was never able to get sysctlbyname work.
                    // if(LIBC.sysctlbyname("kern.argmax", argmaxRef.getPointer(), size, NULL, _)!=0)
                    if (LIBC.sysctl(new int[] { CTL_KERN, KERN_ARGMAX }, 2, argmaxRef.getPointer(), size,
                            NULL, _) != 0)
                        throw new IOException("Failed to get kernl.argmax: " +
                            LIBC.strerror(Native.getLastError()));

                    int argmax = argmaxRef.getValue();

                    class StringArrayMemory extends Memory {
                        private long offset = 0;

                        StringArrayMemory(long l) {
                            super(l);
                        }

                        int readInt() {
                            int r = getInt(offset);
                            offset += sizeOfInt;
                            return r;
                        }

                        byte peek() {
                            return getByte(offset);
                        }

                        String readString() {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte ch;
                            while ((ch = getByte(offset++)) != '\0')
                                baos.write(ch);
                            return baos.toString();
                        }

                        void skip0() {
                            // skip padding '\0's
                            while (getByte(offset) == '\0')
                                offset++;
                        }
                    }
                    StringArrayMemory m = new StringArrayMemory(argmax);
                    size.setValue(argmax);
                    if (LIBC.sysctl(new int[] { CTL_KERN, KERN_PROCARGS2, pid }, 3, m, size, NULL, _) != 0)
                        throw new IOException("Failed to obtain ken.procargs2: " +
                            LIBC.strerror(Native.getLastError()));

                    /*
                     * Make a sysctl() call to get the raw argument space of the
                     * process.  The layout is documented in start.s, which is part
                     * of the Csu project.  In summary, it looks like:
                     *
                     * /---------------\ 0x00000000
                     * :               :
                     * :               :
                     * |---------------|
                     * | argc          |
                     * |---------------|
                     * | arg[0]        |
                     * |---------------|
                     * :               :
                     * :               :
                     * |---------------|
                     * | arg[argc - 1] |
                     * |---------------|
                     * | 0             |
                     * |---------------|
                     * | env[0]        |
                     * |---------------|
                     * :               :
                     * :               :
                     * |---------------|
                     * | env[n]        |
                     * |---------------|
                     * | 0             |
                     * |---------------| <-- Beginning of data returned by sysctl()
                     * | exec_path     |     is here.
                     * |:::::::::::::::|
                     * |               |
                     * | String area.  |
                     * |               |
                     * |---------------| <-- Top of stack.
                     * :               :
                     * :               :
                     * \---------------/ 0xffffffff
                     */

                    // I find the Darwin source code of the 'ps' command helpful in understanding how it does this:
                    // see http://www.opensource.apple.com/source/adv_cmds/adv_cmds-147/ps/print.c
                    int argc = m.readInt();
                    String args0 = m.readString(); // exec path
                    m.skip0();
                    try {
                        for (int i = 0; i < argc; i++) {
                            arguments.add(m.readString());
                        }
                    } catch (IndexOutOfBoundsException e) {
                        throw new IllegalStateException("Failed to parse arguments: pid=" + pid + ", arg0=" +
                            args0 + ", arguments=" + arguments + ", nargs=" + argc + ". Please run 'ps e " +
                            pid + "' and report this to https://issues.jenkins-ci.org/browse/JENKINS-9634", e);
                    }

                    // read env vars that follow
                    while (m.peek() != 0)
                        envVars.addLine(m.readString());
                } catch (IOException e) {
                    // this happens with insufficient permissions, so just ignore the problem.
                }
            }
        }

        // local constants
        private static final int sizeOf_kinfo_proc_32 = 492; // on 32bit Mac OS X.
        private static final int sizeOf_kinfo_proc_64 = 648; // on 64bit Mac OS X.
        private static final int kinfo_proc_pid_offset_32 = 24;
        private static final int kinfo_proc_pid_offset_64 = 40;
        private static final int kinfo_proc_ppid_offset_32 = 416;
        private static final int kinfo_proc_ppid_offset_64 = 560;
        private static final int sizeOfInt = Native.getNativeSize(int.class);
        private static final int CTL_KERN = 1;
        private static final int KERN_PROC = 14;
        private static final int KERN_PROC_ALL = 0;
        private static final int ENOMEM = 12;
        private static int[] MIB_PROC_ALL = { CTL_KERN, KERN_PROC, KERN_PROC_ALL };
        private static final int KERN_ARGMAX = 8;
        private static final int KERN_PROCARGS2 = 49;

    }

    private static final boolean IS_LITTLE_ENDIAN = "little".equals(System.getProperty("sun.cpu.endian"));

    /**
     * Convert null to "".
     * @param s the string to fix.
     * @return The string if not null, empty string if null.
     */
    public static String fixNull(String s) {
        if (s == null)
            return "";
        else
            return s;
    }
}
