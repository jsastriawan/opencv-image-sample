package com.jbs.cv;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

public class Main {

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture capture = new VideoCapture(0);
		capture.open(0);
		int cnt = 0;
		while (!capture.isOpened() && cnt<3) {
			System.out.println("Camera is not opened.");			
			try {
				Thread.sleep(1000);
				cnt++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Socket connection 
		int port = 10000;
		Socket socket;
		try {
			socket = new Socket("localhost", port);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		Mat mat = new Mat();
		int count = 100;
		boolean ack = true;
		while (capture.isOpened() && count>0)
		{
			System.out.println("Capturing Picture "+count);
			capture.read(mat);
			MatOfByte buf = new MatOfByte();
			MatOfInt quality = new MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY,60);
			boolean res = Imgcodecs.imencode(".jpg", mat, buf, quality);
			if (res && socket.isConnected() && ack) {
				try {
					ack = false; // need to check if we receive ack
					socket.getOutputStream().write(buf.toArray());
					socket.getOutputStream().flush();
					System.out.println("Sending picture "+buf.total());
					Thread.sleep(200);
					byte resp[] = new byte[8]; 
					int rlen = socket.getInputStream().read(resp);
					if (rlen>0 && resp[0]==0x01) {
						System.out.println("Ack received");
						ack = true;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
			count--;
		}
		capture.release();
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
