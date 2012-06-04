package tikuwarez.graphics;

// Android Packages
import android.graphics.Matrix;

/**
 * Proxy that uses either an Android Camera or one
 * supplied through a given native library.
 */
public class L7Camera
{
  android.graphics.Camera c = null;
  public boolean changed;
  tikuwarez.graphics.Camera oc = null;

  public L7Camera()
  {
  	if (android.os.Build.VERSION.SDK_INT >= 12)
  	{
  		this.c = new android.graphics.Camera();
  		return;
  	}
  
    try
    {
      this.oc = new tikuwarez.graphics.Camera();
      return;
    }
    catch (UnsatisfiedLinkError localUnsatisfiedLinkError)
    {
      this.c = new android.graphics.Camera();
    }
  }

  public void getMatrix(Matrix paramMatrix)
  {
    if (this.oc != null)
    {
      this.oc.getMatrix(paramMatrix);
      return;  
    }
    
    if (this.c != null)
    {
	  this.c.getMatrix(paramMatrix);
    }
  }

  public void restore()
  {
  	if (this.oc != null)
    {
      this.oc.restore();
      return;  
    }
    
    if (this.c != null)
    {
	  this.c.restore();
    }
  }

  public void rotateX(float paramFloat)
  {
    if (this.oc != null)
    {
      this.oc.rotateX(paramFloat);
      return;  
    }
    
    if (this.c != null)
    {
	  this.c.rotateX(paramFloat);
    }
  }

  public void rotateY(float paramFloat)
  {
    if (this.oc != null)
    {
      this.oc.rotateY(paramFloat);
      return;  
    }
    
    if (this.c != null)
    {
	  this.c.rotateY(paramFloat);
    }
  }

  public void rotateZ(float paramFloat)
  {
    if (this.oc != null)
    {
      this.oc.rotateZ(paramFloat);
      return;  
    }
    
    if (this.c != null)
    {
	  this.c.rotateZ(paramFloat);
    }
  }

  public void save()
  {
    if (this.oc != null)
    {
      this.oc.save();
      return;  
    }
    
    if (this.c != null)
    {
	  this.c.save();
    }
  }

  public void setLocation(float paramFloat1, float paramFloat2, float paramFloat3)
  {
    if (this.oc != null)
      this.oc.setLocation(paramFloat1, paramFloat2, paramFloat3);
  }

  public void translate(float paramFloat1, float paramFloat2, float paramFloat3)
  {
    if (this.oc != null)
    {
      this.oc.setLocation(paramFloat1, paramFloat2, paramFloat3);
      return;
    }
    
	if (this.c != null)
    {
      this.c.setLocation(paramFloat1, paramFloat2, paramFloat3);
    }
  }
}