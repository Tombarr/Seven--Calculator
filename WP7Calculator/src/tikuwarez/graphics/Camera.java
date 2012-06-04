package tikuwarez.graphics;

// Android Utils
import android.graphics.Matrix;
import android.util.Log;

public class Camera {

	static {
		try {
			// Don't bother for Android API 14 ICS or greater.
			if (android.os.Build.VERSION.SDK_INT < 12)
			{
				System.loadLibrary("camera3d");
				Log.v("libcamera3d", "Loaded Camera3D Library Successfully.");
				Log.v("libcamera3d", "Camera3D Library Registered.");
				Log.v("libcamera3d", "It's sexy animation time!");
			}
		} catch (UnsatisfiedLinkError e) {
			Log.e("libcamera3d", "Failed to load Camera3D Library.", e);
		}
	}
	 	
    public Camera() {
        nativeConstructor();
    }
    
    public native void save();
    public native void restore();

    public native void translate(float x, float y, float z);
    public native void rotateX(float deg);
    public native void rotateY(float deg);
    public native void rotateZ(float deg);
    
    public native void rotate(float x,float y,float z);
    public native void setLocation(float x, float y, float z);
    
    public void getMatrix(Matrix matrix) {
    	nativeGetMatrix(matrix);
    }

    public native float dotWithNormal(float dx, float dy, float dz);
    
    protected void finalize() throws Throwable {
        nativeDestructor();
    }

    private native void nativeConstructor();
    private native void nativeDestructor();
    private native void nativeGetMatrix(Object matrix);
    
    int native_instance;
}
