/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 *  A subclass of PrintWriter that redirects its output to a log4j Logger. <p>
 *
 *  This class is used to have something to give api methods that require a
 *  PrintWriter for logging. JBoss-owned classes of this nature generally ignore
 *  the PrintWriter and do their own log4j logging.
 *
 * @author     <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 *
 * @version    $$
 */
public class LoggerWriter extends PrintWriter {
    private Logger logger;

    private Level level;

    private boolean inWrite;

    private boolean issuedWarning;

    /**
     *  Redirect logging to the indicated logger using Level.INFO
     *
     * @param  logger  Description of Parameter
     */
    public LoggerWriter(final Logger logger) {
        this(logger, Level.INFO);
    }

    /**
     *  Redirect logging to the indicated logger using the given level. The
     *  ps is simply passed to super but is not used.
     *
     * @param  logger  Description of Parameter
     * @param  level  Description of Parameter
     */
    public LoggerWriter(final Logger logger, final Level level) {
        super(new InternalLoggerWriter(logger, level), true);
    }

    /**
     * @created    August 19, 2001
     */
    static class InternalLoggerWriter extends Writer {
        private Logger logger;

        private Level level;

        private boolean closed;

        public InternalLoggerWriter(final Logger logger, final Level level) {
            lock = logger;
            //synchronize on this logger
            this.logger = logger;
            this.level = level;
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            if (closed) {
                throw new IOException("Called write on closed Writer");
            }
            // Remove the end of line chars
            while (len > 0 && (cbuf[len - 1] == '\n' || cbuf[len - 1] == '\r')) {
                len--;
            }
            if (len > 0) {
                logger.log(level, String.copyValueOf(cbuf, off, len));
            }
        }

        public void flush() throws IOException {
            if (closed) {
                throw new IOException("Called flush on closed Writer");
            }
        }

        public void close() {
            closed = true;
        }
    }

}
