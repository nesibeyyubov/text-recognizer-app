package com.nesib.flowerdesign

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.net.URI

class MainActivity : AppCompatActivity() {
    private var recognizedTextValue: String = ""
    private lateinit var recognizedTextView: TextView
    private lateinit var uploadImageText: TextView
    private lateinit var selectedImageView: ImageView
    private lateinit var selectImageBtn: Button
    private lateinit var recognizeTextBtn: Button
    private lateinit var loadingProgressBar: ProgressBar
    private val PICK_IMAGE = 1
    private var selectedImageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recognizedTextView = findViewById(R.id.recognizedTextView)
        selectedImageView = findViewById(R.id.selectedImageView)
        selectImageBtn = findViewById(R.id.selectImageBtn)
        recognizeTextBtn = findViewById(R.id.recognizeTextBtn)
        uploadImageText = findViewById(R.id.uploadImageText)
        loadingProgressBar = findViewById(R.id.progressBar)

        selectImageBtn.setOnClickListener {
            openImageSelectionActivity()
        }

        recognizeTextBtn.setOnClickListener {
            toggleProgressBar()
            runTextRecognition()
            recognizedTextView.text = ""
            recognizedTextValue = ""
            recognizeTextBtn.isEnabled = false
            recognizeTextBtn.setBackgroundColor(Color.GRAY)
        }
    }

    private fun toggleProgressBar() {
        loadingProgressBar.visibility =
            if (loadingProgressBar.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun handleTextRecognitionResult(text: Text) {
        val blocks = text.textBlocks
        if (blocks.size == 0) {
            Toast.makeText(this, "No text found", Toast.LENGTH_LONG).show()
            toggleProgressBar()
            return
        }
        for (i in 0 until blocks.size) {
            val lines = blocks[i].lines
            for (j in 0 until lines.size) {
                val elements = lines[j].elements
                for (k in 0 until elements.size) {
                    val textOfElement = elements[k].text
                    recognizedTextValue += " ${textOfElement}"
                }
                recognizedTextValue += "\n\n"
            }
        }
        toggleProgressBar()
        recognizedTextView.text = recognizedTextValue
    }

    private fun runTextRecognition() {
        val image = InputImage.fromFilePath(this, selectedImageUri!!)
        val textRecognizer = TextRecognition.getClient()

        textRecognizer.process(image)
            .addOnSuccessListener {
                handleTextRecognitionResult(it)
            }.addOnFailureListener {
                toggleProgressBar()
            }

    }

    private fun openImageSelectionActivity() {
        var intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            recognizeTextBtn.isEnabled = true
            recognizeTextBtn.setBackgroundColor(Color.BLACK)

            CropImage.activity(uri).start(this)
        }
        else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data!=null){
            val result = CropImage.getActivityResult(data)
            selectedImageUri = result.uri
            selectedImageView.setImageURI(result.uri)
        }
    }
}