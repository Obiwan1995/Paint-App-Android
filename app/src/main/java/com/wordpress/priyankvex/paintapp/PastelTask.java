package com.wordpress.priyankvex.paintapp;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class PastelTask extends AsyncTask<Bitmap, Void, Bitmap>
{
	private MainActivity activity;

	public PastelTask(MainActivity activity)
	{
		this.activity = activity;
	}

	@Override
	protected Bitmap doInBackground(Bitmap... bitmaps)
	{
		if (bitmaps.length < 1)
		{
			return null;
		}

		return Kmeans.computeKmeans(bitmaps[0]);
	}

	@Override
	protected void onPostExecute(Bitmap bitmap)
	{
		this.activity.onPastelFilterResult(bitmap);
		super.onPostExecute(bitmap);
	}
}
