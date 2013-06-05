package com.openshift.internal.client;

import com.openshift.client.fakes.HttpServerFake;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Nicolas Spano
 */
public class WaitingHttpServerFake extends HttpServerFake {

    private long delay;

    @Override
    protected void write(byte[] text, OutputStream outputStream) throws IOException {

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            //Intentional ignore
            return ;
        }
    }

    public WaitingHttpServerFake(long delay){
        this.delay = delay;
    }
}
