package com.wordpress.priyankvex.paintapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final int WRITE_PERMISSION_REQUEST = 1;
    private DrawingView mDrawingView;
    private ImageButton currPaint, drawButton, newButton, saveButton, addButton, eraseButton, addFilterButton;
    private float smallBrush, mediumBrush, largeBrush;
    private final int RESULT_LOAD_IMG = 2;

	private Dialog filtersDialog;
	private ProgressBar bar;
	private RelativeLayout layout;

    private View.OnClickListener filterListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if (filtersDialog.isShowing())
			{
				filtersDialog.dismiss();
			}
			applyFilter(view);
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawingView = findViewById(R.id.drawing);
        // Getting the initial paint color.
        LinearLayout paintLayout = findViewById(R.id.paint_colors);
        // 0th child is white color, so selecting first child to give black as initial color.
        currPaint = (ImageButton)paintLayout.getChildAt(1);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.pallet_pressed));
        addButton = findViewById(R.id.buttonAdd);
        addButton.setOnClickListener(this);
        addFilterButton = findViewById(R.id.buttonAddFilter);
        addFilterButton.setOnClickListener(this);
        drawButton = findViewById(R.id.buttonBrush);
        drawButton.setOnClickListener(this);
        newButton = findViewById(R.id.buttonNew);
        newButton.setOnClickListener(this);
        eraseButton = findViewById(R.id.buttonErase);
        eraseButton.setOnClickListener(this);
        saveButton = findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(this);

        layout = findViewById(R.id.layout);
        bar = findViewById(R.id.progressBar);

        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        // Set the initial brush size
        mDrawingView.setBrushSize(mediumBrush);

        //Permission check

        int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 5;
        int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 6;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * Method is called when color is clicked from pallet.
     * @param view ImageButton on which click took place.
     */
    public void paintClicked(View view){
        if (view != currPaint){
            // Update the color
            ImageButton imageButton = (ImageButton) view;
            String colorTag = imageButton.getTag().toString();
            mDrawingView.setColor(colorTag);
            // Swap the backgrounds for last active and currently active image button.
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.pallet_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.pallet));
            currPaint = (ImageButton)view;
            mDrawingView.setErase(false);
            mDrawingView.setBrushSize(mDrawingView.getLastBrushSize());
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.buttonBrush:
                // Show brush size chooser dialog
                showBrushSizeChooserDialog();
                break;
            case R.id.buttonAdd:
                //Load a photo from gallery
                showLoadPictureDialog();
                break;
            case R.id.buttonAddFilter:
                //Show add filter dialog
                showFiltersDialog();
                break;
            case R.id.buttonErase:
                // Show erase button alert dialog
                showEraseAlertDialog();
                break;
            case R.id.buttonNew:
                // Show new painting alert dialog
                showNewPaintingAlertDialog();
                break;
            case R.id.buttonSave:
                // Show save painting confirmation dialog.
                showSavePaintingConfirmationDialog();
                break;
        }
    }

    private void showLoadPictureDialog(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                if (imageUri != null)
                {
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    mDrawingView.loadImage(selectedImage.copy(Bitmap.Config.ARGB_8888, true));
					addFilterButton.setVisibility(View.VISIBLE);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(MainActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    private void showFiltersDialog(){
        this.filtersDialog = new Dialog(this);
        this.filtersDialog.setContentView(R.layout.dialog_filters);
        this.filtersDialog.setTitle(R.string.dialog_filters_title);
        LinearLayout filtersDialogLayout = filtersDialog.findViewById(R.id.filtersDialogLayout);
        addFiltersListener(filtersDialogLayout);
		this.filtersDialog.show();
    }

	private void addFiltersListener(LinearLayout layout){
    	for (int i = 0; i < layout.getChildCount(); i++)
		{
			View view = layout.getChildAt(i);
			if (view instanceof TextView)
			{
				view.setOnClickListener(this.filterListener);
			}
		}
	}

    private void showBrushSizeChooserDialog(){
        final Dialog brushDialog = new Dialog(this);
        brushDialog.setContentView(R.layout.dialog_brush_size);
        brushDialog.setTitle("Brush size:");
        ImageButton smallBtn = brushDialog.findViewById(R.id.small_brush);
        smallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawingView.setBrushSize(smallBrush);
                mDrawingView.setLastBrushSize(smallBrush);
                brushDialog.dismiss();
            }
        });
        ImageButton mediumBtn = brushDialog.findViewById(R.id.medium_brush);
        mediumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawingView.setBrushSize(mediumBrush);
                mDrawingView.setLastBrushSize(mediumBrush);
                brushDialog.dismiss();
            }
        });

        ImageButton largeBtn = brushDialog.findViewById(R.id.large_brush);
        largeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mDrawingView.setBrushSize(largeBrush);
                mDrawingView.setLastBrushSize(largeBrush);
                brushDialog.dismiss();
            }
        });
        mDrawingView.setErase(false);
        if (mDrawingView.getLastBrushSize() == largeBrush)
        {
            largeBtn.setImageResource(R.drawable.large_selected);
        }
        else if (mDrawingView.getLastBrushSize() == mediumBrush)
        {
            mediumBtn.setImageResource(R.drawable.medium_selected);
        }
        else
        {
            smallBtn.setImageResource(R.drawable.small_selected);
        }
        brushDialog.show();
    }

    private void showEraseAlertDialog(){
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("Erase all annotations");
        newDialog.setMessage("Erase all annotations (you will lose the current drawing)?");
        newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mDrawingView.eraseAllAnnotations();
                dialog.dismiss();
            }
        });
        newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        newDialog.show();
    }

    private void showNewPaintingAlertDialog() {
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("Start new drawing");
        newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
        newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	addFilterButton.setVisibility(View.GONE);
                mDrawingView.startNewDrawing();
                dialog.dismiss();
            }
        });
        newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        newDialog.show();
    }

    private void showSavePaintingConfirmationDialog(){
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
        saveDialog.setTitle("Save drawing");
        saveDialog.setMessage("Save drawing to device Gallery?");
        saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                //save drawing
                mDrawingView.setDrawingCacheEnabled(true);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
                }
                else
                {
                    savePainting();
                }
            }
        });
        saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        saveDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case WRITE_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    savePainting();
                }
                break;
        }
    }

    public void savePainting()
    {
        String imgSaved = MediaStore.Images.Media.insertImage(
                getContentResolver(), mDrawingView.getDrawingCache(),
                UUID.randomUUID().toString()+".png", "drawing");
        if(imgSaved!=null){
            Toast savedToast = Toast.makeText(getApplicationContext(),
                    "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
            savedToast.show();
        }
        else{
            Toast unsavedToast = Toast.makeText(getApplicationContext(),
                    "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
            unsavedToast.show();
        }
        // Destroy the current cache.
        mDrawingView.destroyDrawingCache();
    }

    public void applyFilter(View view)
	{
		switch (view.getId())
		{
			case R.id.negateFilter:
				mDrawingView.applyNegateFilter();
				break;

			case R.id.grayscaleFilter:
				mDrawingView.applyGrayScaleFilter();
				break;

			case R.id.pastelFilter:
				layout.setAlpha(0.6f);
				bar.setVisibility(View.VISIBLE);
				new PastelTask(this).execute(mDrawingView.getCurrentImage());
				break;

			default:
				mDrawingView.removeAllFilters();
				break;
		}
	}

	public void onPastelFilterResult(Bitmap bitmap)
	{
		mDrawingView.drawBitmap(bitmap);
		bar.setVisibility(View.GONE);
		layout.setAlpha(1.0f);
	}
}
