package com.jbs.img;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class Main implements Runnable {
	private static int portNumber = 10000;
	private JFrame frame;
	private BufferedImage img;
	private JLabel label;

	public Main() {
	}
	
	public static void main(String[] args) {
		Main obj = new Main();
		boolean isStopped = false;
		SwingUtilities.invokeLater(obj);
		try {
			ServerSocket serverSocket = new ServerSocket(portNumber);
			System.out.println("Image receiving server started.");
			while (!isStopped) {
				Socket s = serverSocket.accept();
				System.out.println("Accepting incoming connection.");
				new ClientHandler(obj,s).start();
			}
			serverSocket.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static class ClientHandler extends Thread {
		private Main main = null;
		private Socket socket = null;
		
		public ClientHandler(Main m, Socket s) {
			main = m;
			socket = s;
		}
		
		public void run() {
            try {
            	//System.out.println("Client handler thread started.");
                DataInputStream in = new DataInputStream(socket.getInputStream());
                while (true) {
                   BufferedImage img = null;
                   try {
                	   byte buf[] = new byte[262144];
                	   int read = in.read(buf);
                	   // make sure we read till end of image
                	   while (read> 0 && buf[read-2]!=-1 && buf[read-1]!=-39) {
                	      System.out.println("Attempting to read until EOI: "+ read+":"+ buf[read-2]+" "+buf[read-1]);
                	      if (in.available()>0) {
                	    	  read = in.read(buf,read,262144-read);
                	    	  System.out.println("After attempting to read until EOI: "+read+":"+ buf[read-2]+" "+buf[read-1]);
                	      } else {
                	    	  break;
                	      }
                	   }
                	   // EOI found
            		   img=ImageIO.read(new ByteArrayInputStream(buf));
                       if (img!=null) {
                    	   main.updateImage(img);
                       }
                       if (socket.isConnected()) {
                    	   socket.getOutputStream().write(0x01);
                    	   socket.getOutputStream().flush();
                       }
                   } catch (Exception ex) {
                	   ex.printStackTrace();
                	   break;
                   }
                }
            } catch (Exception e) {
            	e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		frame = new JFrame("Received Image");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.setLayout(new FlowLayout());
        frame.pack();
        // load image
        /*
        try {
			img = ImageIO.read(new File("out.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
        label = new JLabel();
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        if (img!=null) {
        	label.setIcon(new ImageIcon(img));
        }
        frame.add(label);
        frame.setVisible(true);		
	}
	
	public void updateImage(BufferedImage newimage) {
		System.out.println("Updating Image");
		label.setIcon(new ImageIcon(newimage));
		frame.repaint();
	}

}
