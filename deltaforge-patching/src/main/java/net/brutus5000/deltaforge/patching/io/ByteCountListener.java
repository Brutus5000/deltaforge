package net.brutus5000.deltaforge.patching.io;

public interface ByteCountListener {

    void updateBytesWritten(long written, long total);
}