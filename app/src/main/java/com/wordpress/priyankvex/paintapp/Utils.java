package com.wordpress.priyankvex.paintapp;
//w ww. jav a2  s  .c  om
import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * This class provides a set of utilities for clusters and colors
 *
 * @author Victor Martinez
 * @see Rgb
 */
public class Utils {

	/**
	 * This method return the distance between two RGB points
	 *
	 * @param c1
	 *            : RGB point one
	 * @param c2
	 *            : RGB point two
	 * @return Integer with distance
	 */
	public int getDistance(Rgb c1, Rgb c2) {
		int dRed = c1.getRed() - c2.getRed();
		int dBlue = c1.getBlue() - c2.getBlue();
		int dGreen = c1.getGreen() - c2.getGreen();

		return ((dRed * dRed) + (dBlue * dBlue) + (dGreen * dGreen));
	}


	/**
	 * This method returns the color at the specific pixel on a bitmap
	 *
	 * @param pixel The pixel of the bitmap
	 * @return RGB point
	 *
	 * @see Rgb
	 * @see Bitmap
	 */

	public Rgb getColor(int pixel) {
		return new Rgb(Color.red(pixel), Color.green(pixel), Color.blue(pixel));
	}

}