package com.wordpress.priyankvex.paintapp;

import android.graphics.Bitmap;
import android.graphics.Color;

public class Grayscale
{
	public static Bitmap toGrayscale(Bitmap src)
	{
		Bitmap res = src.copy(Bitmap.Config.RGB_565, true);

		// Get info about Bitmap
		int width = res.getWidth();
		int height = res.getHeight();
		int pixelsSize = width * height;

		// Get original pixels
		int[] pixels = new int[pixelsSize];
		res.getPixels(pixels, 0, width, 0, 0, width, height);

		for (int i = 0; i < pixelsSize; i++)
		{
			int pixel = pixels[i];
			// retrieve color of all channels
			int a = Color.alpha(pixel);
			int r = Color.red(pixel);
			int g = Color.green(pixel);
			int b = Color.blue(pixel);
			// take conversion up to one single value
			r = g = b = (int) (0.299 * r + 0.587 * g + 0.114 * b);
			pixels[i] = Color.argb(a, r, g, b);
		}
		res.setPixels(pixels, 0, width, 0, 0, width, height);

		return res;
	}
}
