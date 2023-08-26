package com.scrollz.golfai.data.aimodels

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.arthenica.mobileffmpeg.FFmpeg
import com.scrollz.golfai.ml.LiteModelMovenetSingleposeThunderTfliteFloat164
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.nio.FloatBuffer
import javax.inject.Inject

class VideoProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun processVideo(videoUri: Uri) {

        Log.e("predict", "${System.currentTimeMillis()}")

        val framesFolder = withContext(Dispatchers.IO) {
            val cacheDir = context.cacheDir
            val outputFolder = File(cacheDir, FRAMES_DIRECTORY)
            outputFolder.mkdirs()
            outputFolder.listFiles()?.forEach { file -> file.delete() }
            outputFolder
        }

        val fullSizeFramesFolder = withContext(Dispatchers.IO) {
            val cacheDir = context.cacheDir
            val outputFolder = File(cacheDir, FULL_SIZE_FRAMES_DIRECTORY)
            outputFolder.mkdirs()
            outputFolder.listFiles()?.forEach { file -> file.delete() }
            outputFolder
        }

        convertVideoToFrames(videoUri, framesFolder, fullSizeFramesFolder)

        val (videoData, numberOfFrames) = convertFramesToData(framesFolder)

        Log.e("predict", numberOfFrames.toString())
        val keyFrames = detectKeyFrames(videoData, numberOfFrames)

        val posesData = detectPoses(keyFrames, fullSizeFramesFolder)
        Log.e("predict", "FULL_SUC")

    }
    private suspend fun convertVideoToFrames(videoUri: Uri, framesFolder: File, fullSizeFramesFolder: File): Boolean {

        val videoPath = withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val projection = arrayOf(MediaStore.Video.Media.DATA)
            val cursor = contentResolver.query(videoUri, projection, null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                    val videoPath = cursor.getString(columnIndex)
                    cursor.close()
                    videoPath
                } else {
                    throw NullPointerException()
                }
            } else {
                throw NullPointerException()
            }
        }

        return withContext(Dispatchers.Default) {
            val cmd = "-i $videoPath -vf crop=min(iw\\,ih):min(iw\\,ih),scale=$IMAGE_WIDTH:$IMAGE_HEIGHT ${framesFolder.absolutePath}/frame_%05d.jpg"
            val cmdFS = "-i $videoPath -vf crop=min(iw\\,ih):min(iw\\,ih) ${fullSizeFramesFolder.absolutePath}/frame_%05d.jpg"

            val result = FFmpeg.execute(cmd)
            val resultFS = FFmpeg.execute(cmdFS)

            result == 0 && resultFS == 0
        }
    }

    private suspend fun convertFramesToData(framesFolder: File): Pair<FloatBuffer, Int> {
        val files = framesFolder.listFiles()?.apply { sortBy { it.name } }
        return if (files != null) {
            withContext(Dispatchers.Default) {
                Log.e("frame", files.size.toString())
                val videoData = FloatBuffer.allocate(files.size * DIM_PIXEL_SIZE * IMAGE_WIDTH * IMAGE_HEIGHT)
                videoData.rewind()

                files.forEachIndexed { index, file ->
                    val imageStride = IMAGE_WIDTH * IMAGE_HEIGHT
                    val frameStride = DIM_PIXEL_SIZE * imageStride * index
                    val imageData = IntArray(imageStride)
                    BitmapFactory.decodeFile(file.absolutePath).getPixels(imageData, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)

                    for (i in 0 until IMAGE_WIDTH) {
                        for (j in 0 until IMAGE_HEIGHT) {
                            val idx = IMAGE_HEIGHT * i + j
                            val pixelValue = imageData[idx]
                            videoData.put(frameStride + idx, (((pixelValue shr 16 and 0xFF) / 255f - 0.485f) / 0.229f))
                            videoData.put(frameStride + idx + imageStride, (((pixelValue shr 8 and 0xFF) / 255f - 0.456f) / 0.224f))
                            videoData.put(frameStride + idx + imageStride * 2, (((pixelValue and 0xFF) / 255f - 0.406f) / 0.225f))
                        }
                    }
                }

                videoData.rewind()

                Pair(videoData, files.size)
            }
        } else {
            throw NullPointerException()
        }
    }

    private suspend fun detectKeyFrames(videoData: FloatBuffer, numberOfFrames: Int): IntArray {
        return withContext(Dispatchers.Default) {
            Log.e("predict", "${System.currentTimeMillis()}")
            val assetManager = context.assets
            val env = OrtEnvironment.getEnvironment()
            val session = env.createSession(assetManager.open(KEY_FRAMES_DETECTOR_MODEL_PATH).readBytes())
            val inputName = session?.inputNames?.iterator()?.next()
            val shape = longArrayOf(1, numberOfFrames.toLong(), DIM_PIXEL_SIZE.toLong(), IMAGE_WIDTH.toLong(), IMAGE_HEIGHT.toLong())

            val tensor = OnnxTensor.createTensor(env, videoData, shape)

            val result = session.run(mapOf(inputName to tensor))
            Log.e("predict", "1#${System.currentTimeMillis()}")
            val output = (result[0].value) as LongArray

            env.close()

            output.map { it.toInt() }.toIntArray()
        }
    }

    private suspend fun detectPoses(keyFrames: IntArray, fullSizeFramesFolder: File): FloatBuffer {
        val files = fullSizeFramesFolder.listFiles()?.apply { sortBy { it.name } }
        if (files != null) {
            return withContext(Dispatchers.Default) {
                val imageProcessor = ImageProcessor.Builder()
                    .add(ResizeOp(TFLITE_IMAGE_HEIGHT, TFLITE_IMAGE_WIDTH, ResizeOp.ResizeMethod.BILINEAR))
                    .build()

                val posesData = FloatBuffer.allocate(NUMBER_OF_KEY_FRAMES * NUMBER_OF_POSE_KEYPOINTS * 2)
                posesData.rewind()

                val model = LiteModelMovenetSingleposeThunderTfliteFloat164.newInstance(context)

                keyFrames.forEachIndexed { index, frame ->
                    val tensorImage = TensorImage(DataType.UINT8)
                    val bitmap = BitmapFactory.decodeFile(files[frame].absolutePath)
                    saveImageToGallery(bitmap, index.toString())
                    tensorImage.load(bitmap)

                    val input = TensorBuffer.createFixedSize(intArrayOf(1, TFLITE_IMAGE_WIDTH, TFLITE_IMAGE_HEIGHT, 3), DataType.UINT8)
                    input.loadBuffer(imageProcessor.process(tensorImage).buffer)

                    val outputs = model.process(input).outputFeature0AsTensorBuffer.floatArray

                    val frameStride = NUMBER_OF_KEY_FRAMES * index
                    for (i in 0 until NUMBER_OF_POSE_KEYPOINTS) {
                        posesData.put(frameStride + i, outputs[i * 3 + 1])
                        posesData.put(frameStride + i + 1, outputs[i * 3])
                    }
                }

                model.close()

                posesData.rewind()
                posesData
            }
        } else {
            throw NullPointerException()
        }
    }

    private suspend fun saveImageToGallery(bitmap: Bitmap, name: String) {
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val imageDetails = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + "GolfAI Results")
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageDetails)

            imageUri?.let {
                val outputStream = resolver.openOutputStream(it)
                outputStream?.use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                }
                MediaScannerConnection.scanFile(context, arrayOf(it.path), null, null)
            }
        }
    }

    companion object {
        const val FRAMES_DIRECTORY = "golfAI_frames"
        const val FULL_SIZE_FRAMES_DIRECTORY = "golfAI_fullsize_frames"
        const val KEY_FRAMES_DETECTOR_MODEL_PATH = "keyframe_detector_fp32.onnx"

        const val NUMBER_OF_KEY_FRAMES = 8
        const val NUMBER_OF_POSE_KEYPOINTS = 17
        const val DIM_PIXEL_SIZE = 3

        const val IMAGE_WIDTH = 160
        const val IMAGE_HEIGHT = 160
        const val TFLITE_IMAGE_WIDTH = 256
        const val TFLITE_IMAGE_HEIGHT = 256
    }

}
