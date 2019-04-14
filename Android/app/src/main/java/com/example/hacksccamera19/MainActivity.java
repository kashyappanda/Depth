package com.example.hacksccamera19;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import co.polarr.ml.Segmentation;
import co.polarr.ml.SegmentationMask;

public class MainActivity extends AppCompatActivity {

    // Define the pic id
    private static final int pic_id = 123;

    // Define the button and imageview type variable
    Button camera_open_id;
    ImageView click_image_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

//        final Button button = findViewById(R.id.button_id);
//        final Button btn1 = findViewById(R.id.button1_id);
//        final TextView txtView = findViewById(R.id.textView_id);
//
//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                txtView.setText("Team Sonder Overflow");
//            }
//        });
//
//        btn1.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                txtView.setText("Goodbye");
//            }
//        });

        // camera button
        camera_open_id = (Button)findViewById(R.id.camera_button);
        click_image_id = (ImageView)findViewById(R.id.click_image);

        // Camera_open button is for open the camera
        // and add the setOnClickListener in this button
        camera_open_id.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Create the camera_intent ACTION_IMAGE_CAPTURE
                // it will open the camera for capture the image
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Start the activity with camera_intent,
                // and request pic id
                startActivityForResult(camera_intent, pic_id);
            }
        });
    }

    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Match the request 'pic id with requestCode
        if (requestCode == pic_id) {

            // BitMap is data structure of image file
            // which store the image in memory
            Bitmap photo = (Bitmap)data.getExtras().get("data");

            // Set the image in imageview for display

            //Bitmap sourceImage = BitmapFactory.decodeFile("photo");
            Bitmap segmentedImage = Segmentation.on(this, photo);
            Bitmap segmentationMask = SegmentationMask.on(this, photo);

            Bitmap blurred = blurRenderScript(this,photo, 2);

            Bitmap finalBitMap = createSingleImageFromMultipleImages(blurred, segmentedImage);

            click_image_id.setImageBitmap(finalBitMap);
        }
    }
    public static Bitmap blurRenderScript(Context context, Bitmap smallBitmap, int radius) {
        try {
            smallBitmap = RGB565toARGB888(smallBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap bitmap = Bitmap.createBitmap(
                smallBitmap.getWidth(), smallBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(context);

        Allocation blurInput = Allocation.createFromBitmap(renderScript, smallBitmap);
        Allocation blurOutput = Allocation.createFromBitmap(renderScript, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript,
                Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(radius); // radius must be 0 < r <= 25
        blur.forEach(blurOutput);

        blurOutput.copyTo(bitmap);
        renderScript.destroy();

        return bitmap;

    }

    private static Bitmap RGB565toARGB888(Bitmap img) throws Exception {
        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];

        //Get JPEG pixels.  Each int is the color values for one pixel.
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

        //Create a Bitmap of the appropriate format.
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        //Set RGB pixels.
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    }

    private Bitmap createSingleImageFromMultipleImages(Bitmap firstImage, Bitmap secondImage){

        Bitmap result = Bitmap.createBitmap(firstImage.getWidth(), firstImage.getHeight(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(firstImage, 0f, 0f, null);
        canvas.drawBitmap(secondImage, 0, 0, null);
        return result;
    }
}


