package com.scrollz.golfai.data.aimodels

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.arthenica.mobileffmpeg.FFmpeg
import com.scrollz.golfai.domain.model.Report
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
    suspend fun processVideo(videoUri: Uri, dateTime: String): Report {
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
        val keyFrames = detectKeyFrames(videoData, numberOfFrames)
        val posesData = detectPoses(keyFrames, fullSizeFramesFolder, dateTime)
        val orientation = detectOrientation(posesData)

        return if (orientation == 0) { // Лицом
            val errors = detectErrorsFace(posesData)
            Report(
                dateTime = dateTime,
                elbowCornerErrorP1 = errors[0] == 0.0f,
                elbowCornerErrorP7 = errors[1] == 0.0f,
                kneeCornerErrorP1 = errors[2] == 0.0f,
                kneeCornerErrorP7 = errors[3] == 0.0f,
                legsError = errors[4] == 0.0f,
                orientation = orientation
            )
        } else { // Сбоку
            val errors = detectErrorsSide(posesData)
            Report(
                dateTime = dateTime,
                elbowCornerErrorP1 = errors[0] == 0.0f,
                elbowCornerErrorP7 = errors[1] == 0.0f,
                kneeCornerErrorP1 = errors[2] == 0.0f,
                kneeCornerErrorP7 = errors[3] == 0.0f,
                legsError = errors[4] == 0.0f,
                orientation = orientation
            )
        }

    }
    private suspend fun convertVideoToFrames(videoUri: Uri, framesFolder: File, fullSizeFramesFolder: File) {

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

        withContext(Dispatchers.Default) {
            val cmd = "-i $videoPath -r 30 -vf crop=min(iw\\,ih):min(iw\\,ih),scale=$IMAGE_WIDTH:$IMAGE_HEIGHT ${framesFolder.absolutePath}/frame_%05d.jpg"
            val cmdFS = "-i $videoPath -r 30 -vf crop=min(iw\\,ih):min(iw\\,ih),scale=min(iw\\,ih):min(iw\\,ih) ${fullSizeFramesFolder.absolutePath}/frame_%05d.jpg"

            val result = FFmpeg.execute(cmd)
            val resultFS = FFmpeg.execute(cmdFS)

            if (result != 0 || resultFS != 0) {
                throw Exception()
            }
        }
    }

    private suspend fun convertFramesToData(framesFolder: File): Pair<FloatBuffer, Int> {
        val files = framesFolder.listFiles()?.apply { sortBy { it.name } }
        return if (files != null) {
            withContext(Dispatchers.Default) {
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
            val assetManager = context.assets
            val env = OrtEnvironment.getEnvironment()
            val session = env.createSession(assetManager.open(KEY_FRAMES_DETECTOR_MODEL_PATH).readBytes())
            val inputName = session?.inputNames?.iterator()?.next()
            val shape = longArrayOf(1, numberOfFrames.toLong(), DIM_PIXEL_SIZE.toLong(), IMAGE_WIDTH.toLong(), IMAGE_HEIGHT.toLong())

            val tensor = OnnxTensor.createTensor(env, videoData, shape)

            val result = session.run(mapOf(inputName to tensor))
            val output = (result[0].value) as LongArray

            env.close()

            output.map { it.toInt() }.toIntArray()
        }
    }

    private suspend fun detectPoses(keyFrames: IntArray, fullSizeFramesFolder: File, dateTime: String): FloatBuffer {
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
                    val bitmapFS = BitmapFactory.decodeFile(files[frame].absolutePath)
                    val bitmap = BitmapFactory.decodeFile(files[frame].absolutePath)
                    tensorImage.load(bitmap)

                    val input = TensorBuffer.createFixedSize(intArrayOf(1, TFLITE_IMAGE_WIDTH, TFLITE_IMAGE_HEIGHT, 3), DataType.UINT8)
                    input.loadBuffer(imageProcessor.process(tensorImage).buffer)

                    val outputs = model.process(input).outputFeature0AsTensorBuffer.floatArray

                    val pose = when (index) {
                        5 -> 7
                        6 -> 8
                        7 -> 10
                        else -> index + 1
                    }

                    saveImageToGallery(drawSkeleton(bitmapFS, outputs), "GolfAI_${dateTime}_pose_${pose}")

                    val frameStride = NUMBER_OF_POSE_KEYPOINTS * 2 * index
                    for (i in 0 until NUMBER_OF_POSE_KEYPOINTS) {
                        posesData.put(frameStride + i * 2, outputs[i * 3 + 1])
                        posesData.put(frameStride + i * 2 + 1, outputs[i * 3])
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

    private suspend fun detectOrientation(posesData: FloatBuffer): Int {
        return withContext(Dispatchers.Default) {
            val assetManager = context.assets
            val env = OrtEnvironment.getEnvironment()
            val session = env.createSession(assetManager.open(ORIENTATION_MODEL_PATH).readBytes())

            val input = posesData.slice()
            input.limit(NUMBER_OF_POSE_KEYPOINTS * 2)

            val inputName = session?.inputNames?.iterator()?.next()
            val shape = longArrayOf(NUMBER_OF_POSE_KEYPOINTS.toLong(), 2)
            val tensor = OnnxTensor.createTensor(env, input, shape)

            val result = session.run(mapOf(inputName to tensor))
            val output = (result[0].value) as FloatArray

            env.close()

            if(output[0] == 0.0f) {
                0     // Лицом
            } else {
                if (output[1] == 0.0f) {
                    1 // Вправо
                } else {
                    -1// Влево
                }
            }
        }
    }

    private suspend fun detectErrorsFace(posesData: FloatBuffer): FloatArray {
        return withContext(Dispatchers.Default) {
            val assetManager = context.assets
            val env = OrtEnvironment.getEnvironment()
            val session = env.createSession(assetManager.open(FACE_MODEL_PATH).readBytes())

            posesData.rewind()
            val inputName = session?.inputNames?.iterator()?.next()
            val shape = longArrayOf(NUMBER_OF_KEY_FRAMES.toLong(), NUMBER_OF_POSE_KEYPOINTS.toLong(), 2)
            val tensor = OnnxTensor.createTensor(env, posesData, shape)

            val result = session.run(mapOf(inputName to tensor))
            val output = (result[0].value) as FloatArray

            env.close()

            output
        }
    }

    private suspend fun detectErrorsSide(posesData: FloatBuffer): FloatArray {
        return withContext(Dispatchers.Default) {
            val assetManager = context.assets
            val env = OrtEnvironment.getEnvironment()
            val session = env.createSession(assetManager.open(SIDE_MODEL_PATH).readBytes())

            posesData.rewind()
            val inputName = session?.inputNames?.iterator()?.next()
            val shape = longArrayOf(NUMBER_OF_KEY_FRAMES.toLong(), NUMBER_OF_POSE_KEYPOINTS.toLong(), 2)
            val tensor = OnnxTensor.createTensor(env, posesData, shape)

            val result = session.run(mapOf(inputName to tensor))
            val output = (result[0].value) as FloatArray

            env.close()

            output
        }
    }

    private suspend fun drawSkeleton(bitmap: Bitmap, keyPoints: FloatArray): Bitmap {
        return withContext(Dispatchers.IO) {
            val w = bitmap.width
            val h = bitmap.height
            val skeletonX = floatArrayOf(
                keyPoints[1] * w, keyPoints[4] * w, keyPoints[7] * w,
                keyPoints[10] * w, keyPoints[13] * w, keyPoints[16] * w,
                keyPoints[19] * w, keyPoints[22] * w, keyPoints[25] * w,
                keyPoints[28] * w, keyPoints[31] * w, keyPoints[34] * w,
                keyPoints[37] * w, keyPoints[40] * w, keyPoints[43] * w,
                keyPoints[46] * w, keyPoints[49] * w,
            )
            val skeletonY = floatArrayOf(
                keyPoints[0] * w, keyPoints[3] * w, keyPoints[6] * w,
                keyPoints[9] * w, keyPoints[12] * w, keyPoints[15] * w,
                keyPoints[18] * w, keyPoints[21] * w, keyPoints[24] * w,
                keyPoints[27] * w, keyPoints[30] * w, keyPoints[33] * w,
                keyPoints[36] * w, keyPoints[39] * w, keyPoints[42] * w,
                keyPoints[45] * w, keyPoints[48] * w,
            )

            val paintedBitmap = Bitmap.createBitmap(w, h, bitmap.config)
            val canvas = Canvas(paintedBitmap)

            canvas.drawBitmap(bitmap, 0f, 0f, null)

            val paint = Paint()
            paint.color = Color.BLUE
            paint.strokeWidth = 3.0f

            canvas.drawLine(skeletonX[0], skeletonY[0], skeletonX[1], skeletonY[1], paint)
            canvas.drawLine(skeletonX[0], skeletonY[0], skeletonX[2], skeletonY[2], paint)
            canvas.drawLine(skeletonX[1], skeletonY[1], skeletonX[3], skeletonY[3], paint)
            canvas.drawLine(skeletonX[2], skeletonY[2], skeletonX[4], skeletonY[4], paint)
            canvas.drawLine(skeletonX[0], skeletonY[0], skeletonX[5], skeletonY[5], paint)
            canvas.drawLine(skeletonX[0], skeletonY[0], skeletonX[6], skeletonY[6], paint)
            canvas.drawLine(skeletonX[5], skeletonY[5], skeletonX[6], skeletonY[6], paint)
            canvas.drawLine(skeletonX[5], skeletonY[5], skeletonX[7], skeletonY[7], paint)
            canvas.drawLine(skeletonX[7], skeletonY[7], skeletonX[9], skeletonY[9], paint)
            canvas.drawLine(skeletonX[6], skeletonY[6], skeletonX[8], skeletonY[8], paint)
            canvas.drawLine(skeletonX[8], skeletonY[8], skeletonX[10], skeletonY[10], paint)
            canvas.drawLine(skeletonX[6], skeletonY[6], skeletonX[12], skeletonY[12], paint)
            canvas.drawLine(skeletonX[5], skeletonY[5], skeletonX[11], skeletonY[11], paint)
            canvas.drawLine(skeletonX[11], skeletonY[11], skeletonX[12], skeletonY[12], paint)
            canvas.drawLine(skeletonX[12], skeletonY[12], skeletonX[14], skeletonY[14], paint)
            canvas.drawLine(skeletonX[14], skeletonY[14], skeletonX[16], skeletonY[16], paint)
            canvas.drawLine(skeletonX[11], skeletonY[11], skeletonX[13], skeletonY[13], paint)
            canvas.drawLine(skeletonX[13], skeletonY[13], skeletonX[15], skeletonY[15], paint)

            paint.strokeWidth = 6.0f

            for (i in skeletonX.indices) { canvas.drawPoint(skeletonX[i], skeletonY[i], paint) }

            paintedBitmap
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
        const val ORIENTATION_MODEL_PATH = "orientation_model.onnx"
        const val FACE_MODEL_PATH = "tech_estimation_face_on.onnx"
        const val SIDE_MODEL_PATH = "tech_estimation_down_the_line.onnx"

        const val NUMBER_OF_KEY_FRAMES = 8
        const val NUMBER_OF_POSE_KEYPOINTS = 17
        const val DIM_PIXEL_SIZE = 3

        const val IMAGE_WIDTH = 160
        const val IMAGE_HEIGHT = 160
        const val TFLITE_IMAGE_WIDTH = 256
        const val TFLITE_IMAGE_HEIGHT = 256
    }

}
