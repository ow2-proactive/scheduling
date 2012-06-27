package org.ow2.proactive.scheduler.core.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;


@Entity
@Table(name = "JOB_CLASSPATH_CONTENT")
public class JobClasspathContent {

    private long crc;

    private byte[] classpathContent;

    private boolean containsJarFiles;

    @Id
    @Column(name = "CRC")
    public long getCrc() {
        return crc;
    }

    public void setCrc(long crc) {
        this.crc = crc;
    }

    @Column(name = "CONTENT", length = Integer.MAX_VALUE)
    @Lob
    public byte[] getClasspathContent() {
        return classpathContent;
    }

    public void setClasspathContent(byte[] classpathContent) {
        this.classpathContent = classpathContent;
    }

    @Column(name = "CONTAINS_JAR")
    public boolean isContainsJarFiles() {
        return containsJarFiles;
    }

    public void setContainsJarFiles(boolean containsJarFiles) {
        this.containsJarFiles = containsJarFiles;
    }

}
