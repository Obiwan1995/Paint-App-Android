package com.wordpress.priyankvex.paintapp;

/*from   w  w w.  ja  va  2s . c  o m*/

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.Random;

/**
 * This class provides methods to calculate the k-means algorithm
 * @author Victor Martinez, Andrei Jianu
 *
 */
public class Kmeans{
	private final static int K = 6;
	private final static int ITER = 10;

	private static Utils uClass = new Utils();
	private static Rgb[] rgbClusters = null;
	private static int[] cntr;
	private static int[] pixels;

	private static int[] red   = new int[K];
	private static int[] blue  = new int[K];
	private static int[] green = new int[K];

	/**
	 * This method initialize the clusters
	 */
	private static void initClusters()
	{
		Log.d("Kmeans", "Clusters init");
		for(int i = 0; i < K; i++)
		{
			Random rand = new Random();
			rgbClusters[i] = uClass.getColor(pixels[rand.nextInt(pixels.length)]);
		}
	}

	/**
	 * This method runs the k-means algorithm
	 */
	public static Bitmap computeKmeans(Bitmap source)
	{
		Bitmap res = source.copy(Bitmap.Config.ARGB_8888, true);
		int width = source.getWidth();
		int height = source.getHeight();
		int pixelsCount = width*height;

		pixels = new int[pixelsCount];
		res.getPixels(pixels, 0, width, 0, 0, width, height);

		rgbClusters = new Rgb[K];
		initClusters();

		int[] ared   = new int[K];
		int[] ablue  = new int[K];
		int[] agreen = new int[K];

		cntr  = new int[K];
		boolean stop = false;

		for(int i = 0; i < K; i++)
		{
			ared[i]      = 0;
			ablue[i]     = 0;
			agreen[i]    = 0;
		}

		int[] pixelsClusters = assignToClusters();

		int n = 0;
		while (!stop && n < ITER)
		{
			Log.d("Kmeans", "Start iteration " + (n+1) + " of " + ITER);
			updateClusters();

			stop = true;
			for(int i = 0; i < K; i++)
			{
				if(ared[i] != red[i]){stop = false; ared[i] = red[i];}
				if(ablue[i] != blue[i]){stop = false; ablue[i] = blue[i];}
				if(agreen[i] != green[i]){stop = false; agreen[i] = green[i];}
			}

			pixelsClusters = assignToClusters();

			n++;
		}

		return setPixels(res, pixelsClusters);
	}

	private static int[] assignToClusters()
	{
		int[] pixelsClusters = new int[pixels.length];

		for(int i = 0; i < K; i++)
		{
			red[i]      = 0;
			blue[i]     = 0;
			green[i]    = 0;
			cntr[i]     = 0;
		}

		for (int i = 0; i < pixels.length; i++)
		{
			int whatCluster = 0;
			Rgb rgbColor = uClass.getColor(pixels[i]);
			int cDistance = uClass.getDistance(rgbColor, rgbClusters[0]);

			for(int k = 0; k < K; k++)
			{
				int tDistance = uClass.getDistance(rgbColor, rgbClusters[k]);
				if(tDistance < cDistance)
				{
					cDistance = tDistance;
					whatCluster = k;
				}
			}

			pixelsClusters[i] = whatCluster;

			cntr[whatCluster]++;
			red[whatCluster] += rgbColor.getRed();
			blue[whatCluster] += rgbColor.getBlue();
			green[whatCluster] += rgbColor.getGreen();
		}

		return pixelsClusters;
	}

	private static void updateClusters()
	{
		for(int i = 0; i < K; i++)
		{
			if(cntr[i] != 0)
			{
				red[i]      = (red[i] / cntr[i]);
				blue[i]     = (blue[i] / cntr[i]);
				green[i]    = (green[i] / cntr[i]);

				rgbClusters[i] = new Rgb(red[i], green[i], blue[i]);
			}
		}
	}

	private static Bitmap setPixels(Bitmap result, int[] pixelsClusters)
	{
		for (int i = 0; i < pixels.length; i++)
		{
			Rgb rgb = rgbClusters[pixelsClusters[i]];
			pixels[i] = Color.rgb(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
		}

		result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());

		return result;
	}
}