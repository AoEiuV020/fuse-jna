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
    @Test
    public void hello() throws Exception {
        File mountPoint=newMountPointFolder("hello");
        HelloFS helloFS=new HelloFS();
        helloFS.mount(mountPoint,false);
        assertTrue(mountPoint.isDirectory());
        File hello=new File(mountPoint,"hello.txt");
        assertTrue(hello.isFile());
        assertTrue(hello.canRead());
        InputStream input=new FileInputStream(hello);
        int count=3;
        byte[] buf=new byte[count];
        input.read(buf);
        final String contents = "Hello World!\n";
        assertArrayEquals(contents.substring(0,count).getBytes(),buf);
        input.close();
        helloFS.unmount();
    }
    private File newMountPointFolder(String name) throws IOException {
        return tempFolder.newFolder(name);
    }
}

