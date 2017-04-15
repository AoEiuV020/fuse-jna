package net.fusejna.examples;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import org.junit.*;
import org.junit.rules.*;
import static org.junit.Assert.*;
/**
 * Created by AoEiuV020 on 2017/04/15.
 */
public class MountTest{
    @Rule
    public TemporaryFolder tempFolder=new TemporaryFolder();
    private HelloFS helloFS;
    private InputStream input;
    @Test
    public void hello() throws Exception {
        File mountPoint=newMountPointFolder("hello");
        helloFS=new HelloFS();
        HelloFS helloFS=new HelloFS();
        helloFS.mount(mountPoint,false);
        assertTrue(mountPoint.isDirectory());
        File hello=new File(mountPoint,"hello.txt");
        assertTrue(hello.isFile());
        assertTrue(hello.canRead());
        int len=8;
        int off=3;
        byte[] buf=new byte[len];
        input=new FileInputStream(hello);
        input.skip(off);
        int actuallyLength=input.read(buf,0,len);
        final String contents = "Hello World!\n";
        assertEquals(contents.substring(off,off+actuallyLength),new String(buf,0,actuallyLength));
    }
    @After
    public void unmount() throws Exception {
        if(helloFS!=null&&helloFS.isMounted())
            helloFS.unmount();
        if(input!=null)
            input.close();
    }
    private File newMountPointFolder(String name) throws IOException {
        return tempFolder.newFolder(name);
    }
}

