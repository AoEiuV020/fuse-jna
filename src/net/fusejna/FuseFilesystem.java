package net.fusejna;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.StructStatvfs.StatvfsWrapper;
import net.fusejna.types.TypeDev;
import net.fusejna.types.TypeGid;
import net.fusejna.types.TypeMode;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.types.TypeOff;
import net.fusejna.types.TypeSize;
import net.fusejna.types.TypeUid;

import com.sun.jna.Function;
import com.sun.jna.Pointer;

public abstract class FuseFilesystem
{
	private static @interface FuseMethod
	{
	}

	private static @interface UserMethod
	{
	}

	private static final String defaultFilesystemName = "userfs-";
	private static final Pattern regexNormalizeFilesystemName = Pattern.compile("[a-zA-Z]");
	private final ReentrantLock mountLock = new ReentrantLock();
	private File mountPoint = null;
	private Logger logger = null;

	@FuseMethod
	final int _chmod(final String path, final TypeMode mode)
	{
		return chmod(path, new ModeWrapper(mode.longValue()));
	}

	@FuseMethod
	final int _chown(final String path, final TypeUid uid, final TypeGid gid)
	{
		return chown(path, uid.longValue(), gid.longValue());
	}

	@FuseMethod
	final void _destroy()
	{
		destroy();
	}

	@FuseMethod
	final int _fgetattr(final String path, final StructStat stat, final StructFuseFileInfo info)
	{
		final StatWrapper swrapper = new StatWrapper(path, stat);
		defaultStat(swrapper, FuseJna.getUid(), FuseJna.getGid());
		final FileInfoWrapper fwrapper = new FileInfoWrapper(path, info);
		final int result = fgetattr(path, swrapper, fwrapper);
		swrapper.write();
		fwrapper.write();
		return result;
	}

	@FuseMethod
	final int _flush(final String path, final StructFuseFileInfo info)
	{
		return flush(path, new FileInfoWrapper(path, info));
	}

	@FuseMethod
	final int _fsync(final String path, final StructFuseFileInfo info)
	{
		return fsync(path, new FileInfoWrapper(info));
	}

	@FuseMethod
	final int _getattr(final String path, final StructStat stat)
	{
		final StatWrapper wrapper = new StatWrapper(path, stat);
		defaultStat(wrapper, FuseJna.getUid(), FuseJna.getGid());
		final int result = getattr(path, wrapper);
		wrapper.write();
		return result;
	}

	@FuseMethod
	final void _init()
	{
		init();
	}

	@FuseMethod
	final int _link(final String path, final String target)
	{
		return link(path, target);
	}

	@FuseMethod
	final int _mkdir(final String path, final TypeMode mode)
	{
		return mkdir(path, new ModeWrapper(mode.longValue()));
	}

	@FuseMethod
	final int _mknod(final String path, final TypeMode mode, final TypeDev dev)
	{
		return mknod(path, new ModeWrapper(mode.longValue()), dev.longValue());
	}

	@FuseMethod
	final int _open(final String path, final StructFuseFileInfo info)
	{
		final FileInfoWrapper wrapper = new FileInfoWrapper(path, info);
		final int result = open(path, wrapper);
		wrapper.write();
		return result;
	}

	@FuseMethod
	final int _read(final String path, final Pointer buffer, final TypeSize size, final TypeOff offset,
			final StructFuseFileInfo info)
	{
		final long bufSize = size.longValue();
		final long readOffset = offset.longValue();
		final ByteBuffer buf = buffer.getByteBuffer(0, bufSize);
		final FileInfoWrapper wrapper = new FileInfoWrapper(path, info);
		final int result = read(path, buf, bufSize, readOffset, wrapper);
		wrapper.write();
		return result;
	}

	@FuseMethod
	final int _readdir(final String path, final Pointer buf, final Pointer fillFunction, final TypeOff offset,
			final StructFuseFileInfo info)
	{
		return readdir(path, new DirectoryFiller(buf, Function.getFunction(fillFunction)));
	}

	@FuseMethod
	final int _readlink(final String path, final Pointer buffer, final TypeSize size)
	{
		final long bufSize = size.longValue();
		final ByteBuffer buf = buffer.getByteBuffer(0, bufSize);
		return readlink(path, buf, bufSize);
	}

	@FuseMethod
	final int _release(final String path, final StructFuseFileInfo info)
	{
		return release(path, new FileInfoWrapper(path, info));
	}

	@FuseMethod
	final int _rename(final String path, final String newName)
	{
		return rename(path, newName);
	}

	@FuseMethod
	final int _rmdir(final String path)
	{
		return rmdir(path);
	}

	@FuseMethod
	final int _statfs(final String path, final StructStatvfs statsvfs)
	{
		final StatvfsWrapper wrapper = new StatvfsWrapper(path, statsvfs);
		final int result = statfs(path, wrapper);
		wrapper.write();
		return result;
	}

	@FuseMethod
	final int _symlink(final String path, final String target)
	{
		return symlink(path, target);
	}

	@FuseMethod
	final int _truncate(final String path, final TypeOff offset)
	{
		return truncate(path, offset.longValue());
	}

	@FuseMethod
	final int _unlink(final String path)
	{
		return unlink(path);
	}

	@FuseMethod
	final int _write(final String path, final Pointer buffer, final TypeSize size, final TypeOff offset,
			final StructFuseFileInfo info)
	{
		final long bufSize = size.longValue();
		final long readOffset = offset.longValue();
		final ByteBuffer buf = buffer.getByteBuffer(0, bufSize);
		final FileInfoWrapper wrapper = new FileInfoWrapper(path, info);
		final int result = write(path, buf, bufSize, readOffset, wrapper);
		wrapper.write();
		return result;
	}

	public abstract void afterUnmount(final File mountPoint);

	public abstract void beforeUnmount(final File mountPoint);

	@UserMethod
	public abstract int chmod(final String path, final ModeWrapper modeWrapper);

	@UserMethod
	public abstract int chown(final String path, final long uid, final long gid);

	/**
	 * Subclasses may override this to customize the default parameters applied to the stat structure, or to prevent such
	 * behavior (by overriding this method with an empty one)
	 * 
	 * @param stat
	 *            The
	 */
	protected void defaultStat(final StatWrapper wrapper, final long uid, final long gid)
	{
		// Set some sensible defaults
		wrapper.setMode(NodeType.DIRECTORY).setAllTimesMillis(System.currentTimeMillis()).nlink(1).uid(FuseJna.getUid())
				.gid(FuseJna.getGid());
	}

	@UserMethod
	public abstract void destroy();

	@UserMethod
	public abstract int fgetattr(final String path, final StatWrapper stat, final FileInfoWrapper info);

	@UserMethod
	public abstract int flush(final String path, final FileInfoWrapper info);

	@UserMethod
	public abstract int fsync(final String path, final FileInfoWrapper info);

	@UserMethod
	public abstract int getattr(final String path, final StatWrapper stat);

	final String getFuseName()
	{
		String name = getName();
		if (name == null) {
			return defaultFilesystemName;
		}
		name = regexNormalizeFilesystemName.matcher(name).replaceAll("");
		if (name.isEmpty()) {
			return defaultFilesystemName;
		}
		return name.toLowerCase();
	}

	final Logger getLogger()
	{
		return logger;
	}

	public final File getMountPoint()
	{
		mountLock.lock();
		final File mountPoint = this.mountPoint;
		mountLock.unlock();
		return mountPoint;
	}

	protected abstract String getName();

	protected abstract String[] getOptions();

	@FuseMethod
	public abstract void init();

	public final boolean isMounted()
	{
		return getMountPoint() != null;
	}

	@UserMethod
	public abstract int link(String path, String target);

	protected final FuseFilesystem log(final boolean logging)
	{
		return log(logging ? Logger.getLogger(getClass().getCanonicalName()) : null);
	}

	protected final FuseFilesystem log(final Logger logger)
	{
		mountLock.lock();
		if (mountPoint != null) {
			mountLock.unlock();
			throw new IllegalStateException("Cannot turn logging on/orr when filesystem is already mounted.");
		}
		this.logger = logger;
		mountLock.unlock();
		return this;
	}

	@UserMethod
	public abstract int mkdir(final String path, final ModeWrapper modeWrapper);

	@UserMethod
	public abstract int mknod(final String path, final ModeWrapper modeWrapper, final long dev);

	public final void mount(final File mountPoint) throws FuseException
	{
		mount(mountPoint, true);
	}

	public final void mount(final File mountPoint, final boolean blocking) throws UnsatisfiedLinkError, FuseException
	{
		mountLock.lock();
		if (isMounted()) {
			throw new IllegalStateException(getFuseName() + " is already mounted at " + this.mountPoint);
		}
		try {
			FuseJna.mount(this, mountPoint, blocking);
			this.mountPoint = mountPoint;
			onMount(mountPoint);
		}
		finally {
			mountLock.unlock();
		}
	}

	public final void mount(final String mountPoint) throws FuseException
	{
		mount(new File(mountPoint), true);
	}

	public abstract void onMount(final File mountPoint);

	@UserMethod
	public abstract int open(final String path, final FileInfoWrapper info);

	@UserMethod
	public abstract int read(final String path, final ByteBuffer buffer, final long size, final long offset,
			final FileInfoWrapper info);

	@UserMethod
	public abstract int readdir(final String path, final DirectoryFiller filler);

	@UserMethod
	public abstract int readlink(final String path, final ByteBuffer buffer, final long size);

	@UserMethod
	public abstract int release(final String path, final FileInfoWrapper info);

	@UserMethod
	public abstract int rename(final String path, final String newName);

	@UserMethod
	public abstract int rmdir(final String path);

	void setFinalMountPoint(final File mountPoint)
	{
		mountLock.lock();
		this.mountPoint = mountPoint;
		mountLock.unlock();
	}

	@UserMethod
	public abstract int statfs(final String path, final StatvfsWrapper wrapper);

	@UserMethod
	public abstract int symlink(final String path, final String target);

	@UserMethod
	public abstract int truncate(final String path, final long offset);

	@UserMethod
	public abstract int unlink(final String path);

	public final void unmount() throws IOException, FuseException
	{
		mountLock.lock();
		try {
			beforeUnmount(mountPoint);
			FuseJna.unmount(this);
			final File oldMountPoint = mountPoint;
			mountPoint = null;
			beforeUnmount(oldMountPoint);
		}
		finally {
			mountLock.unlock();
		}
	}

	@UserMethod
	public abstract int write(final String path, final ByteBuffer buf, final long bufSize, final long readOffset,
			final FileInfoWrapper info);
}
