package net.brutus5000.deltaforge.io;

public interface ByteCountListener {

    void updateBytesWritten(long written, long total);
}