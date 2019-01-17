package sSquare;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG;

public class TCounter implements Runnable {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	static int factor = 0; 
	Mat imag;
	static Mat imag2; 
	
	int signal;
	JFrame[] jframe = new JFrame[2];
	JLabel[] vidpanel = new JLabel[2];
	  Thread runner;
	  public TCounter() {
	    this.runner = new Thread(this);
	    this.runner.start();
	  }
	  static double pre = 0,f=1 , pre_t = 0,ff = 1.0;
	  public  int calculate() // calculate area of white space in frame returned by getFrame method.
		{
			
			int time =0;
			int nwp =0;
			
			try {
			nwp = Core.countNonZero(imag2);//number of white pixel
			}
			catch(Exception e)
			{
				System.out.println("tumse na ho payega");
			}
			double tp =  imag2.size().width * imag2.size().height;		//total pixels
			double percentage = nwp/tp;
			percentage *= 100; 
			int per = (int)percentage;
			int c = 0;
			System.out.println("nwp="+nwp+" tp="+tp+" percentage ="+ percentage + " per ="+per + " c="+c);
			if((per>=0)&&(per<25))
				c = 0;
			if((per>=25)&&(per<50))
				c= 1;
			if((per>=50)&&(per<75))
				c= 2;
			if((per>=75)&&(per<=100))
				c= 3;
			
			switch(c)
			{
			case(0):
				time = (int) ( 5 + (c*f));
				break;
			case(1):
				time = (int) ( 20 + (c*f) * 2);
				break;
			case(2):
				time = (int) ( 40 + (c*f) * 3);
				break;
			case(3):
				time =(int) (  60 + (c*f) * 4);
				break;
			default:
				break;
			}
			
			
			cal_f(time);
			
			return time;
			
		}
	  
	  private void cal_f(int time) {
		if(time<pre_t)
		{
			if(pre == -1 )	{
				
				ff = ff/10;
				ff = (ff<0.01) ? 0.01 : ff ;
				f -= ff;
			}
			else if(pre == 1) {
				
				ff = 0.1;
				f -=  ff; 
			}
			pre = -1;
		}
		if (time >pre_t)
		{
			if(pre == -1 )	{
				ff  = 0.1;
				f +=  ff;
			}
			else if(pre == 1) {
				
				ff = ff/10;
				ff = (ff<0.01) ? 0.01 : ff ;
				f += ff;
				
			}
			pre = 1;
		}
	}
	@Override
	  public void run() {
		  jframe[1] = new JFrame();
			jframe[0] = new JFrame();
		  System.out.println("Running....");
			for (int counter = 0; counter < 2; counter++) {
				
				jframe[counter].setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				vidpanel[counter] = new JLabel();
				jframe[counter].setContentPane(vidpanel[counter]);
				jframe[counter].setSize(640, 480);
				jframe[counter].setVisible(true);
				jframe[counter].setLocation(640 * (counter % 2), 0);

			}
			
			
			

			Mat frame = new Mat();
			Mat outerbox = new Mat();

			Mat diffframe = null;
			Mat temponframe = null;
			// ArrayList<Rect> array = new ArrayList<Rect>();

			BackgroundSubtractorMOG mBGsub = new BackgroundSubtractorMOG(3, 4, 0.8);
			VideoCapture camera = new VideoCapture();
			camera.open(0);
			Size sz = new Size(640, 480);
			int i = 0;

			// KalmanFilter kf = new KalmanFilter(2,1);

			if (!camera.isOpened()) {
				System.out.println("Cannot open the camera, Please try again later");
			}
			

		synchronized(this) {
			while (true) { // while loop start
				if (camera.read(frame)) {
					Imgproc.resize(frame, frame, sz);
					imag = frame.clone();
					imag2 = frame.clone();
					processFrame(camera, frame, imag2, mBGsub);
					////////////////////////////////////////
					outerbox = new Mat(frame.size(), CvType.CV_8UC1);
					Imgproc.cvtColor(frame, outerbox, Imgproc.COLOR_BGR2GRAY);
					//Imgproc.erode(outerbox, outerbox, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8, 8)));
					Imgproc.erode(frame, outerbox, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8, 8)));
					Imgproc.dilate(outerbox, outerbox, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8, 8)));
					Imgproc.GaussianBlur(outerbox, outerbox, new Size(3, 3), 0);
				
					if (i == 0) {
						jframe[1].setSize(frame.width(), frame.height());
						diffframe = new Mat(outerbox.size(), CvType.CV_8UC3);
						temponframe = new Mat(outerbox.size(), CvType.CV_8UC3);
						diffframe = outerbox.clone();
					}
					if(i==1) {
						processFrame(camera, temponframe, diffframe, mBGsub);
						Imgproc.adaptiveThreshold(diffframe, diffframe, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
								Imgproc.THRESH_BINARY_INV, 5, 2);

					}

					

					ImageIcon image = new ImageIcon(Mat2bufferedImage(imag));
					vidpanel[0].setIcon(image);
					temponframe = outerbox.clone();
					ImageIcon image2 = new ImageIcon(Mat2bufferedImage(imag2));
					vidpanel[1].setIcon(image2);
					

				}
			}			}	// while loop ends
			  
	  }

	  protected static void processFrame(VideoCapture capture, Mat mRgba, Mat mFGMask, BackgroundSubtractorMOG mBGSub) {
			capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME);
			//capture.retrieve(mRgba ,Highgui.CV_CAP_ANDROID_COLOR_FRAME_GREY_FRAME);
			mBGSub.apply(mRgba, mFGMask, 0);
			//Imgproc.cvtColor(mFGMask, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);

		}

		private static BufferedImage Mat2bufferedImage(Mat image) {
			MatOfByte bytemat = new MatOfByte();
			Highgui.imencode(".jpg", image, bytemat);
			byte[] bytes = bytemat.toArray();
			InputStream in = new ByteArrayInputStream(bytes);
			BufferedImage img = null;
			try {
				img = ImageIO.read(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return img;
		}
	
	}
