package com.lip.woqu.utils.net;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by LJG on 13-11-28.
 */
public class NetCustomMultiPartEntity extends MultipartEntity {

    private final ProgressListener listener;
    private long totalSize=0;

    public NetCustomMultiPartEntity(final ProgressListener listener){
        super();
        this.listener = listener;
    }

    public NetCustomMultiPartEntity(final HttpMultipartMode mode, final ProgressListener listener){
        super(mode);
        this.listener = listener;
    }

    public NetCustomMultiPartEntity(HttpMultipartMode mode, final String boundary, final Charset charset, final ProgressListener listener){
        super(mode, boundary, charset);
        this.listener = listener;
    }


    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        if(totalSize==0){
            totalSize= NetCustomMultiPartEntity.this.getContentLength();
        }
        super.writeTo(new CountingOutputStream(outstream,totalSize,this.listener));
    }

    public static interface ProgressListener{
        void transferred(long num, long total);
    }

    public static class CountingOutputStream extends FilterOutputStream {
        private final ProgressListener listener;
        private long transferred;
        private long totalSize;
        public CountingOutputStream(final OutputStream out,long totalSize, final ProgressListener listener){
            super(out);
            this.listener = listener;
            this.totalSize=totalSize;
            this.transferred = 0;
        }
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            this.transferred += len;
            if(this.listener!=null){
                this.listener.transferred(this.transferred,this.totalSize);
            }
        }
        public void write(int b) throws IOException {
            out.write(b);
            this.transferred++;
            if(this.listener!=null){
                this.listener.transferred(this.transferred,this.totalSize);
            }
        }
    }

}
