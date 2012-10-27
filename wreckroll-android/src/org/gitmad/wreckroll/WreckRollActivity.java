package org.gitmad.wreckroll;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.gitmad.wreckroll.canvas.Circle;
import org.gitmad.wreckroll.canvas.ControllerBoard;
import org.gitmad.wreckroll.canvas.Image;
import org.gitmad.wreckroll.canvas.OnDrawListener;
import org.gitmad.wreckroll.canvas.OnTouchPointListener;
import org.gitmad.wreckroll.canvas.TouchPoint;
import org.gitmad.wreckroll.canvas.TypewriterTextWriter;
import org.gitmad.wreckroll.client.BufferedClient;
import org.gitmad.wreckroll.client.DebugClient;
import org.gitmad.wreckroll.client.WreckClient;
import org.gitmad.wreckroll.util.CountdownTimer;
import org.gitmad.wreckroll.video.CameraCaptureAsyncTask;
import org.gitmad.wreckroll.video.CapturedImage;
import org.gitmad.wreckroll.video.SpyHardProcessor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.MotionEvent;


public class WreckRollActivity extends Activity {
    
    private CameraCaptureAsyncTask cameraCaptureTask;
    private WreckClient client;

    private static final int FREEZE_FRAME_TIME_MS = 1000;
    
    private final String REGISTRAR_ADDRESS = "192.168.1.250";

    protected static final int MAX_DETECTED_FACES = 1;

    private CountdownTimer freezeFrameTimer = new CountdownTimer();
    
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SteeringWheel steeringWheel;

    public WreckRollActivity() {
    }
    
    boolean blah = false;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        
        final ControllerBoard board = ((ControllerBoard)findViewById(R.id.panel));

//        Rect rectgle= new Rect();
//        Window window= getWindow();
//        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
//        int StatusBarHeight= rectgle.top;
//        int contentViewTop= 
//            window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
//        int TitleBarHeight= contentViewTop - StatusBarHeight;
//

        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int titleBarHeight = 50;
        int statusBarHeight = (int) Math.ceil(titleBarHeight * metrics.density);

        final int usableWidth  = metrics.widthPixels;
        final int usableHeight = metrics.heightPixels - statusBarHeight;

        board.setOnDrawListener(new OnDrawListener() {

            public void onPreDraw() {
                if (!WreckRollActivity.this.freezeFrameTimer.poll() && !board.isWritingMessage()) {
                    CapturedImage image = cameraCaptureTask.getCurrentImage();
                    if (image != null) {
                        //set the latest bitmap captured from the camera
                        board.setBackgroundImage(image.getBitmap());
                        if (image.getAttribute(SpyHardProcessor.ATTR_FACE_FOUND).equals("true")) {
                            String newMessage = generateRandomSpyMessage();
                            // TODO: this would make more sense if we could specify
                            // a max width...but for now, we're ok
                            // NOTE: 108 is kind of arbitrary...we may need to
                            // adjust with different text
                            board.setMessage(new TypewriterTextWriter(newMessage,
                                    usableWidth / 2 - 108, usableHeight - 100));
                        }
                    }
                }
//                if (!board.isWritingMessage()) {
//                    String newMessage = generateRandomSpyMessage();
//                    //TODO: this would make more sense if we could specify a max width...but for now, we're ok
//                    //NOTE: 108 is kind of arbitrary...we may need to adjust with different text
//                    board.setMessage(new TypewriterTextWriter(newMessage, usableWidth / 2 - 108, usableHeight - 100));
//                }
            }

            public void onPostDraw() {
                //nothing to do
            }
        });

        int spacing = usableHeight / 5;
        int radius  = usableHeight / 10;

        Circle circle = new Circle(3*radius, usableHeight / 2, 3*radius, Color.LTGRAY);
        board.addTouchPoint(circle);
        
        circle.setOnTouchListener(new OnTouchPointListener() {
            public boolean isSupportedAction(int action) {
                return action != MotionEvent.ACTION_UP;
            }

            public void touchPerformed(TouchPoint point, float x, float y) {
                processMovement((Circle) point, x, y);
            }
        });
        
//        circle = new Circle(radius, 3 * radius, radius, Color.RED);
//        board.addTouchPoint(circle);
//        
//        circle.setOnTouchListener(new OnTouchPointListener() {
//            public boolean isSupportedAction(int action) {
//                return action != MotionEvent.ACTION_UP;
//            }
//
//            public void touchPerformed(TouchPoint point, float x, float y) {
//                client.stop();
//            }
//        });
//        
//        circle = new Circle(radius, 5 * radius, radius, Color.CYAN);
//        board.addTouchPoint(circle);
//        
//        circle.setOnTouchListener(new OnTouchPointListener() {
//            public boolean isSupportedAction(int action) {
//                return action != MotionEvent.ACTION_UP;
//            }
//
//            public void touchPerformed(TouchPoint point, float x, float y) {
//                client.left();
//            }
//        });
//        
//        circle = new Circle(radius, 7 * radius, radius, Color.BLUE);
//        board.addTouchPoint(circle);
//        
//        circle.setOnTouchListener(new OnTouchPointListener() {
//            public boolean isSupportedAction(int action) {
//                return action != MotionEvent.ACTION_UP;
//            }
//
//            public void touchPerformed(TouchPoint point, float x, float y) {
//                client.right();
//            }
//        });
//        
//        circle = new Circle(radius, 9 * radius, radius, Color.YELLOW);
//        board.addTouchPoint(circle);
//        
//        circle.setOnTouchListener(new OnTouchPointListener() {
//            public boolean isSupportedAction(int action) {
//                return action != MotionEvent.ACTION_UP;
//            }
//
//            public void touchPerformed(TouchPoint point, float x, float y) {
//                client.reverse();
//            }
//        });
//
//
//        //draw the buttons as touchpoint controls
//
//        int dPadRadius = (int)(usableHeight * 0.6/2);
//        //NOTE: currently assumes landscape mode
//        Circle circle = new Circle((int)(dPadRadius * 1.3), usableHeight / 2, dPadRadius, Color.LTGRAY);
//        board.addTouchPoint(circle);
//        
//        circle.setOnTouchListener(new OnTouchPointListener() {
//            public boolean isSupportedAction(int action) {
//                return action != MotionEvent.ACTION_UP;
//            }
//
//            public void touchPerformed(TouchPoint point, float x, float y) {
//                processMovement((Circle) point, x, y);
//            }
//        });
//        
//        int radius = usableHeight / 8;

        radius = usableHeight / 8; //4 buttons
        Circle smokeButton = new Circle(metrics.widthPixels - radius, radius, radius, Color.YELLOW);
        board.addTouchPoint(smokeButton);
        smokeButton.setOnTouchListener(new OnTouchPointListener() {
            public boolean isSupportedAction(int action) {
                return action == MotionEvent.ACTION_DOWN;
            }

            public void touchPerformed(TouchPoint point, float x, float y) {
                client.toggleSmoke();
            }
        });
        
        Circle gunButton       = new Circle(metrics.widthPixels - radius, 3 * radius, radius, Color.YELLOW);
        board.addTouchPoint(gunButton);
        gunButton.setOnTouchListener(new OnTouchPointListener() {
            public boolean isSupportedAction(int action) {
                return action == MotionEvent.ACTION_DOWN;
            }
            
            public void touchPerformed(TouchPoint point, float x, float y) {
                client.toggleGun();
            }
        });

        Circle canopyButton    = new Circle(metrics.widthPixels - radius, 5 * radius, radius, Color.YELLOW);
        board.addTouchPoint(canopyButton);
        canopyButton.setOnTouchListener(new OnTouchPointListener() {
            
            public boolean isSupportedAction(int action) {
                return action == MotionEvent.ACTION_DOWN;
            }

            public void touchPerformed(TouchPoint point, float x, float y) {
                client.toggleCanopy();
            }
        });

        Circle emergencyStopButton    = new Circle(metrics.widthPixels - radius, 7 * radius, radius, Color.RED);
        board.addTouchPoint(emergencyStopButton);
        emergencyStopButton.setOnTouchListener(new OnTouchPointListener() {
            
            public boolean isSupportedAction(int action) {
                return action == MotionEvent.ACTION_DOWN;
            }

            public void touchPerformed(TouchPoint point, float x, float y) {
                client.emergencyStop();
            }
        });

        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.camera);
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2, false);
        Image snapShotButton = new Image(bitmap, metrics.widthPixels/2, 7 * radius);
        board.addTouchPoint(snapShotButton);
        snapShotButton.setOnTouchListener(new OnTouchPointListener() {

            public boolean isSupportedAction(int action) {
                return action == MotionEvent.ACTION_DOWN;
            }

            public void touchPerformed(TouchPoint point, float x, float y) {
                saveImage(board.getBackgroundImage());
                WreckRollActivity.this.freezeFrameTimer.start(FREEZE_FRAME_TIME_MS);
            }
        });
    
//        mSensor.
    }
    
    protected String generateRandomSpyMessage() {
        String [] openings    = {"**** CLASSIFIED ****", "FOUO: For official use only", "TOP SECRET", "FOR YOUR EYES ONLY", "SITREP: All Agents Bulletin", "MEMO RE: Intelligence"};
        String [] lastNames   = {"Burdell", "Peace", "Stallworth", "Hood", "Smith", "Cash", "Jenkins", "Jones", "Elmalem", "Rosalia", "Vorah", "Dekel", "Wang", "Johnson", "Borg", "Dijkstra", "Fischer", "Williams", "Powers", "Doe", "Zoidberg"};
        String [] countries   = {"USA", "UK", "Russia", "Ukraine", "Israel", "Canada", "France", "Germany", "China"};
        String [] specialties = {"Hacking", "Cracking", "Diplomancy", "Computers", "Espionage", "Electronics", "Surveillance", "Interrogation", "Weapons", "Disguises", "Forgery"};
        
        int one   = (int) (Math.random() * openings.length);
        int two   = (int) (Math.random() * lastNames.length);
        int three = (int) (Math.random() * 26);
        int four  = (int) (Math.random() * countries.length);
        int five  = (int) (Math.random() * specialties.length);
        int six;
        do {
            six   = (int) (Math.random() * specialties.length);
        } while(six == five);
        

        String lastName     = lastNames[two];
        String firstInitial = lastName.equals("Peace") ? "T" :
                              lastName.equals("Stallworth") ? "C" :
                              lastName.equals("Burdell") ? "G" :
                              lastName.equals("Doe") ? "J" :
                              lastName.equals("Dijkstra") ? "E" :
                                  String.valueOf((char)(0x41 + three));
        String str = String.format("%s\n%s. %s\nAllegience: %s\nSpecialties: %s, %s",
                                    openings[one].toUpperCase(), firstInitial, lastName, countries[four], specialties[five], specialties[six]);
        return str;
    }

    protected void saveImage(Bitmap backgroundImage) {
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "WreckRoll");
        imagesFolder.mkdirs(); 
        String fileName = "image_" + new Date().getTime() + ".jpg";
        File output = new File(imagesFolder, fileName);
//        while (output.exists()){
//            fileName = "image_" + String.valueOf(imageNum) + ".jpg";
//            output = new File(imagesFolder, fileName);
//        }
//        Uri uriSavedImage = Uri.fromFile(output);
//        imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
        try {
            FileOutputStream out = new FileOutputStream(output);
            backgroundImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    protected void processMovement(Circle point, float touchX, float touchY) {
        boolean moving = false;
        boolean forward = false;
        boolean turning = false;
        boolean right = false;
        
        int stopBarPX = 30;
        if (Math.abs(touchY - point.getY()) > stopBarPX) {
            if (touchY < point.getY()) {
                this.client.forward();
            } else {
                this.client.reverse();
            }
        } else {
            this.client.stop();
        }
        
        if (Math.abs(touchX - point.getX()) > stopBarPX) {
            turning = true;
            if (touchX > point.getX()) {
                this.client.right();
            } else {
                this.client.left();
            }
        } else {
            System.out.println("NOT TURNING");
        }        
    }
    
    private String queryRegistrar(String path){
    	HttpURLConnection connection = null;
    	try {
    		URL url = new URL("http://" + REGISTRAR_ADDRESS + ":8001" + path);
    		connection = (HttpURLConnection)url.openConnection();
    		connection.setDoOutput(false);
    		connection.setRequestMethod("GET");
    		connection.setConnectTimeout(10);
    		connection.connect();
    		if (connection.getResponseCode() == 404){
    			return null;
    		}

    		char [] buf = new char[512];
    		InputStreamReader is = new InputStreamReader(connection.getInputStream());
    		int read = 0;
    		StringBuilder builder = new StringBuilder();
    		while ((read = is.read(buf)) >= 0) {
    		    builder.append(buf, 0, read);
    		}
    		return builder.toString();
    	} catch(IOException ex){
    		return null;
    	}
    }
    private String getRelayIP(){
    	String queryResult = queryRegistrar("/relay");
    	if (queryResult != null){
    		return queryResult;
    	}
    	return "192.168.1.50";
    }
    
    private String getCameraIP(){
    	String queryResult = queryRegistrar("/camera");
    	if (queryResult != null){
    		return queryResult;
    	}
    	return "192.168.1.20";
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        String ipCamera = getCameraIP();
//        int color = 224, 170, 15 or 183, 135, 39
        this.cameraCaptureTask = new CameraCaptureAsyncTask(this, ipCamera, new SpyHardProcessor(MAX_DETECTED_FACES, Color.YELLOW, 50));
        this.cameraCaptureTask.execute();
        
        try {
            this.client = new BufferedClient(new DebugClient()); // DirectArduinoClient();
            ///TODO: connect to registrar
            String ipRelay   = getRelayIP();
            short  relayPort = 6696;
//            this.client = new BufferedClient(new RelayClient(ipRelay, relayPort));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
//        this.steeringWheel = new SteeringWheel(this.client);
//        mSensorManager.registerListener(this.steeringWheel, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        this.cameraCaptureTask.cancel(true);
//        mSensorManager.unregisterListener(this.steeringWheel);
    }

    void setPanelBackground(Bitmap bitmap) {
        ((ControllerBoard)findViewById(R.id.panel)).setBackgroundImage(bitmap);
    }
    
    
    /*
 // Create a constant to convert nanoseconds to seconds.
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4]();
    private float timestamp;

    public void onSensorChanged(SensorEvent event) {
      // This timestep's delta rotation to be multiplied by the current rotation
      // after computing it from the gyro sample data.
      if (timestamp != 0) {
        final float dT = (event.timestamp - timestamp) * NS2S;
        // Axis of the rotation sample, not normalized yet.
        float axisX = event.values[0];
        float axisY = event.values[1];
        float axisZ = event.values[2];

        // Calculate the angular speed of the sample
        float omegaMagnitude = Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

        // Normalize the rotation vector if it's big enough to get the axis
        // (that is, EPSILON should represent your maximum allowable margin of error)
        if (omegaMagnitude > EPSILON) {
          axisX /= omegaMagnitude;
          axisY /= omegaMagnitude;
          axisZ /= omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * dT / 2.0f;
        float sinThetaOverTwo = sin(thetaOverTwo);
        float cosThetaOverTwo = cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * axisX;
        deltaRotationVector[1] = sinThetaOverTwo * axisY;
        deltaRotationVector[2] = sinThetaOverTwo * axisZ;
        deltaRotationVector[3] = cosThetaOverTwo;
      }
      timestamp = event.timestamp;
      float[] deltaRotationMatrix = new float[9];
      SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;
       }
    }*/
}