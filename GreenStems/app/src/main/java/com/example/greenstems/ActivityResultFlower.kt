package com.example.greenstems

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.speech.RecognitionListener
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import com.bumptech.glide.Glide
import org.tensorflow.lite.examples.classification.ml.model
import org.tensorflow.lite.Interpreter;

class ActivityResultFlower : AppCompatActivity() {
    var result: TextView? = null
    var image: ImageView? = null

    private var bitmap: Bitmap? = null
    private var uri: String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activityresultflower)
        result = findViewById(R.id.resulttext)
        image = findViewById<View>(R.id.imageresult) as ImageView


        val bundle = getIntent().getExtras();
        if (bundle != null) {
            val intent = intent
            val bitmap = intent.getParcelableExtra<Parcelable>("sendImage") as Bitmap?
            uri = bundle.getString("selectedImage")

            if (bitmap != null)
                image!!.setImageBitmap(bitmap)
            else
                Glide.with(this).load(uri).into(image!!)
        }

    }



    private class ImageAnalyzer(ctx: Context, private val listener: RecognitionListener) :
            ImageAnalysis.Analyzer {

        // TODO 1: Add class variable TensorFlow Lite Model
        // Initializing the flowerModel by lazy so that it runs in the same thread when the process
        // method is called.
        private val flowerModel: FlowerModel by lazy{

            // TODO 6. Optional GPU acceleration
            val compatList = CompatibilityList()

            val options = if(compatList.isDelegateSupportedOnThisDevice) {
                Log.d(TAG, "This device is GPU Compatible ")
                Model.Options.Builder().setDevice(Model.Device.GPU).build()
            } else {
                Log.d(TAG, "This device is GPU Incompatible ")
                Model.Options.Builder().setNumThreads(4).build()
            }

            // Initialize the Flower Model
            FlowerModel.newInstance(ctx, options)
        }

        override fun analyze(imageProxy: ImageProxy) {

            val items = mutableListOf<Recognition>()

            // TODO 2: Convert Image to Bitmap then to TensorImage
            val tfImage = TensorImage.fromBitmap(toBitmap(imageProxy))

            // TODO 3: Process the image using the trained model, sort and pick out the top results
            val outputs = flowerModel.process(tfImage)
                    .probabilityAsCategoryList.apply {
                        sortByDescending { it.score } // Sort with highest confidence first
                    }.take(MAX_RESULT_DISPLAY) // take the top results

            // TODO 4: Converting the top probability items into a list of recognitions
            for (output in outputs) {
                items.add(Recognition(output.label, output.score))
            }

//            // START - Placeholder code at the start of the codelab. Comment this block of code out.
//            for (i in 0 until MAX_RESULT_DISPLAY){
//                items.add(Recognition("Fake label $i", Random.nextFloat()))
//            }
//            // END - Placeholder code at the start of the codelab. Comment this block of code out.

            // Return the result
            listener(items.toList())

            // Close the image,this tells CameraX to feed the next image to the analyzer
            imageProxy.close()
        }

        /**
         * Convert Image Proxy to Bitmap
         */
        private val yuvToRgbConverter = YuvToRgbConverter(ctx)
        private lateinit var bitmapBuffer: Bitmap
        private lateinit var rotationMatrix: Matrix

        @SuppressLint("UnsafeExperimentalUsageError")
        private fun toBitmap(imageProxy: ImageProxy): Bitmap? {

            val image = imageProxy.image ?: return null

            // Initialise Buffer
            if (!::bitmapBuffer.isInitialized) {
                // The image rotation and RGB image buffer are initialized only once
                Log.d(TAG, "Initalise toBitmap()")
                rotationMatrix = Matrix()
                rotationMatrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                bitmapBuffer = Bitmap.createBitmap(
                        imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
                )
            }

            // Pass image to an image analyser
            yuvToRgbConverter.yuvToRgb(image, bitmapBuffer)

            // Create the Bitmap in the correct orientation
            return Bitmap.createBitmap(
                    bitmapBuffer,
                    0,
                    0,
                    bitmapBuffer.width,
                    bitmapBuffer.height,
                    rotationMatrix,
                    false
            )
        }

    }
}