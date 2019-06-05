
package ExDevp;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream; 
import java.io.FileOutputStream; 
import java.io.IOException;
import java.io.InputStream; 
import java.io.OutputStream; 
import java.util.Calendar;
import android.R.integer;
import android.util.Log;

public class ExDevPort {

	
	private static final String TAG = "SerialPort";
	/* * Do not remove or rename the field mFd: it is used by native method close(); */ 
	private FileDescriptor mFd; 
	private FileInputStream mFileInputStream; 
	private FileOutputStream mFileOutputStream; 
	public ExDevPort(File device, int baudrate) throws SecurityException, IOException {
		mFd = open(device.getAbsolutePath(), baudrate); 
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException(); 
			} 
		
		mFileInputStream = new FileInputStream(mFd); 
		mFileOutputStream = new FileOutputStream(mFd); 
		
		}
	
	// Getters and setters 
	public InputStream getInputStream() 
	{ 
		return mFileInputStream; 
	} 
	
	public OutputStream getOutputStream() 
	{ 
		return mFileOutputStream;
	} 
	
	
	// JNI 
	
	private native static FileDescriptor open(String path, int baudrate);
	static { 
		System.loadLibrary("ExSPort"); 
		}
	
}
