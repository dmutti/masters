package debugger;

import java.io.*;

public class StreamRedirecter extends Thread {

    private static final int BUFFER_SIZE = 2048;
    private final Reader in;
    private final Writer out;

    public StreamRedirecter(String name, InputStream in, OutputStream out) {
        super(name);
        this.in = new InputStreamReader(in); // stream to copy from
        this.out = new OutputStreamWriter(out); // stream to copy to
        setPriority(Thread.MAX_PRIORITY - 1);
    }

    @Override
    public void run() {
        try {
            char[] cbuf = new char[BUFFER_SIZE];
            int count;
            while ((count = in.read(cbuf, 0, BUFFER_SIZE)) >= 0)
                out.write(cbuf, 0, count);
            out.flush();
        } catch (IOException e) {
            System.err.println("StreamRedirecter: " + e);
        }
    }
}