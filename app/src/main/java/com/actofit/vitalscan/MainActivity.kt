@file:Suppress("DEPRECATION")

package com.actofit.vitalscan

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.graphics.YuvImage
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.media.Image
import android.media.ImageReader
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.ByteBuffer
import java.util.Base64
import kotlin.system.exitProcess


var yuvImageByteArrays = ArrayList<ByteArray>()
//const val appName = "vitalscanv2"
//const val vitalscanv2ImageSeparationIdentifier = "vitalscanv2ImageSeparationIdentifier"
private val outputResolution = ArrayList<Int>()

//320, 240


const val batchSize = 100
//var howMuchDone = 0
//
//@SuppressLint("SimpleDateFormat")
//fun getCurrentTimeFormatted(): String {
//    val dateFormat = SimpleDateFormat("yyyyMMddHHmmssSSS")
//    return dateFormat.format(Date())
//}

data class ImageParams(
    val yuvImageByteArrays: ArrayList<ByteArray>,
    val loadProbeIterator: Int,
    val requestUID: String,
    val context: Context,
    val outputResolution: List<Int>,
    val a: Int,
    val intent: Intent
)

@Suppress("DEPRECATION", "PLUGIN_IS_NOT_ENABLED")
class ImageProcessingTask : AsyncTask<ImageParams, Void, Void>() {


    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg imageParams: ImageParams): Void? {

        Log.e("inside", "doInBackground")
        val params = imageParams[0]
        val yuvImageByteArrays: ArrayList<ByteArray> = params.yuvImageByteArrays
        val loadProbeIterator: Int = params.loadProbeIterator
        val requestUID: String = params.requestUID
        val context: Context = params.context
        val outputResolution = params.outputResolution
//        val mSocket = params.socket
        val a = params.a
        val intent = params.intent

        writeImageToLocalStorage(
            yuvImageByteArrays,
            loadProbeIterator,
            requestUID,
            context,
            outputResolution,
//            mSocket,
            a,
            intent
        )

        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun writeImageToLocalStorage(
        yuvImageByteArrays: ArrayList<ByteArray>,
        loadProbeIterator: Int,
        requestUID: String,
        context: Context,
        outputResolution: List<Int>,
//        socket: Socket,
        a: Int,
        intent: Intent
    ) {

        val width = outputResolution[0]
        val height = outputResolution[1]

        var it = a - batchSize
//        var it = 0
        val iterator: MutableIterator<ByteArray> = yuvImageByteArrays.iterator()
        val b64ImageStrings = ArrayList<String>()
        while (iterator.hasNext()) {

            val currentYuvImageByteArray = iterator.next()

            val yuvImage = YuvImage(currentYuvImageByteArray, ImageFormat.NV21, width, height, null)

            val os = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, os)
            val jpegByteArray = os.toByteArray()
            os.close()

            val bitmap = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.size)

            if (bitmap == null) {
                // Bitmap decoding failed
                Log.e("BitmapDecoding", "Failed to decode byte array to Bitmap")
            }

            val resizedWidth = 176
            val resizedHeight = 144
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true)
            bitmap.recycle()

            try {
                val pngOs = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, pngOs)
                val pngByteArray = pngOs.toByteArray()
                resizedBitmap.recycle()
                pngOs.close()

                Log.e("Written image", "$it")

                b64ImageStrings.add(Base64.getEncoder().encodeToString(pngByteArray))

//                val filePath = "${context.getExternalFilesDir("Downloads")}/$it.png"
//
//                // Write the PNG byte array to a file
//                val outputStream = FileOutputStream(filePath)
//                outputStream.write(pngByteArray)
//                outputStream.close()


            } catch (e: Exception) {
                Log.e("FileWriting", "Failed to write image to storage", e)
            } finally {
                bitmap.recycle()
                resizedBitmap.recycle()
            }
            iterator.remove()
            it += 1
        }

        runBlocking {
            val client = HttpClient {
                install(JsonFeature) {
                    serializer = KotlinxSerializer()
                }
            }

            Log.e("TRYING", "1")
            Log.e("array size", b64ImageStrings.size.toString())
            try {
                Log.e("TRYING", "2")
                val response: String = client.post("http://20.124.20.8:5000/") {
                    contentType(ContentType.Application.Json)
                    body = mapOf("000#$loadProbeIterator#000$requestUID" to b64ImageStrings)
                }
                val metricsJsonObject: JsonObject = Json.parseToJsonElement(response).jsonObject
                Log.e("NEW BACKEND RETURN", metricsJsonObject["hr"].toString())

                if (metricsJsonObject.containsKey("opcode")){

                    if ( metricsJsonObject["opcode"].toString() == "200"){
                        if ( loadProbeIterator == 9){

                            val userDetailsName = intent.getStringExtra("name")
                            val userDetailsHeight = intent.getStringExtra("height")
                            val userDetailsWeight = intent.getStringExtra("weight")
                            val userDetailsAge = intent.getStringExtra("age")
                            val userDetailsPno = intent.getStringExtra("pno")

                            val bodyParamsArrayList = ArrayList<String>()


                            bodyParamsArrayList.add(userDetailsName.toString())
                            bodyParamsArrayList.add(userDetailsHeight.toString())
                            bodyParamsArrayList.add(userDetailsWeight.toString())
                            bodyParamsArrayList.add(userDetailsAge.toString())
                            bodyParamsArrayList.add(userDetailsPno.toString())

                            println(bodyParamsArrayList)

                            val bodyParamsResponse: String = client.post("http://20.124.20.8:5000/") {
                                contentType(ContentType.Application.Json)
                                body = mapOf("bodyParams$requestUID" to bodyParamsArrayList)
                            }

                            val metricsJsonObjectBodyParams: JsonObject = Json.parseToJsonElement(bodyParamsResponse).jsonObject

                            println("body params response $metricsJsonObjectBodyParams.toString()" )

                            if ( metricsJsonObjectBodyParams["opcode"].toString() == "500"){
                                println("Response measurement failed")
                                val failedIntent = Intent(context, MeasurementFailedActivity::class.java)
                                failedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(context, failedIntent, null)
                            }

                            if (metricsJsonObjectBodyParams.containsKey("hr")) {
                                val resultsIntent = Intent(
                                    context,
                                    DisplayResultsActivity::class.java
                                )
                                resultsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                resultsIntent.putExtra("hr", metricsJsonObjectBodyParams["hr"].toString())
                                resultsIntent.putExtra("ibi", metricsJsonObjectBodyParams["ibi"].toString())
                                resultsIntent.putExtra("sdnn", metricsJsonObjectBodyParams["sdnn"].toString())
                                resultsIntent.putExtra("rmssd", metricsJsonObjectBodyParams["rmssd"].toString())
                                resultsIntent.putExtra("pnn20", metricsJsonObjectBodyParams["pnn20"].toString())
                                resultsIntent.putExtra("pnn50", metricsJsonObjectBodyParams["pnn50"].toString())
                                resultsIntent.putExtra("hrv", metricsJsonObjectBodyParams["hrv"].toString())
                                resultsIntent.putExtra("rr", metricsJsonObjectBodyParams["rr"].toString())
                                resultsIntent.putExtra("sysbp", metricsJsonObjectBodyParams["sysbp"].toString())
                                resultsIntent.putExtra("diabp", metricsJsonObjectBodyParams["diabp"].toString())
                                resultsIntent.putExtra("spo2", metricsJsonObjectBodyParams["spo2"].toString())
                                resultsIntent.putExtra("vo2max", metricsJsonObjectBodyParams["vo2max"].toString())
                                resultsIntent.putExtra("si", metricsJsonObjectBodyParams["si"].toString())
                                resultsIntent.putExtra("mhr", metricsJsonObjectBodyParams["mhr"].toString())
                                resultsIntent.putExtra("hrr", metricsJsonObjectBodyParams["hrr"].toString())
                                resultsIntent.putExtra("thr", metricsJsonObjectBodyParams["thr"].toString())
                                resultsIntent.putExtra("co", metricsJsonObjectBodyParams["co"].toString())
                                resultsIntent.putExtra("map", metricsJsonObjectBodyParams["map"].toString())
                                resultsIntent.putExtra("hu", metricsJsonObjectBodyParams["hu"].toString())
                                resultsIntent.putExtra("bv", metricsJsonObjectBodyParams["bv"].toString())
                                resultsIntent.putExtra("tbw", metricsJsonObjectBodyParams["tbw"].toString())
                                resultsIntent.putExtra("bwp", metricsJsonObjectBodyParams["bwp"].toString())
                                resultsIntent.putExtra("bmi", metricsJsonObjectBodyParams["bmi"].toString())
                                resultsIntent.putExtra("bf", metricsJsonObjectBodyParams["bf"].toString())
                                resultsIntent.putExtra("asth_risk", metricsJsonObjectBodyParams["asth_risk"].toString())
                                startActivity(context, resultsIntent, null)
                            }
                        }
                    }
                    else if (metricsJsonObject["opcode"].toString() == "500"){
                        println("Response measurement failed")
                        val failedIntent = Intent(context, MeasurementFailedActivity::class.java)
                        failedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(context, failedIntent, null)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                client.close()
                b64ImageStrings.clear()
            }
        }
        yuvImageByteArrays.clear()
    }
}

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {


    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        Toast.makeText(this, "Please exit app for quitting scan", Toast.LENGTH_SHORT).show()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private external fun convertYUV420888ToNV21(
        imageWidth: Int,
        imageHeight: Int,
        yPlane: ByteBuffer,
        uPlane: ByteBuffer,
        vPlane: ByteBuffer,
        uPixelStride: Int,
        vPixelStride: Int,
        uRowStride: Int,
        vRowStride: Int,
    ): ByteArray

    companion object {
        // Load the native library
        init {
            System.loadLibrary("native-lib")
        }
    }

    private var previousBrightness: Float = -1.0f

    private fun maximizeScreenBrightness() {
        this.window?.let { window ->
            window.attributes?.apply {
                previousBrightness = screenBrightness
                screenBrightness = 1f
                window.attributes = this
            }
        }
        this.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private fun restoreScreenBrightness() {
        this.window?.let { window ->
            window.attributes?.apply {
                screenBrightness = previousBrightness
                window.attributes = this
            }
        }
    }


    private fun sendLogToServer(message: String) {

        Log.e("TAG", message)
        // Implement network operation to send message to your server
        runBlocking {
            val client = HttpClient {
                install(JsonFeature) {
                    serializer = KotlinxSerializer()
                }
            }
            try {
                val response: String = client.post("http://20.124.20.8:5000/crashlogs") {
                    contentType(ContentType.Application.Json)
                    body = mapOf("crashlog" to message)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                client.close()
            }
        }
    }

    private fun getStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }



    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            sendLogToServer(
                getStackTrace(e)
            )
        }

        maximizeScreenBrightness()

//        val progressBarView = findViewById<View>(R.id.progressBar)
//        val requestUID = UUID.randomUUID().toString()
        val requestUID = intent.getStringExtra("pno").toString()
//        val howMuchDoneTV = findViewById<TextView>(R.id.howMuchDone)

        var recordingStarted = 0

        val textureView: TextureView = findViewById(R.id.textureView)
        val recordButton = findViewById<Button>(R.id.recordButton)
        val timerView = findViewById<TextView>(R.id.timerView)

//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED ||
//            ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED ||
//            ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.INTERNET
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(
//                    Manifest.permission.CAMERA,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.INTERNET
//                ),
//                101
//            )
//
//            return
//        }


        // start socket connection to server socket
//
////        try {
//        val options = IO.Options()
////        options.apply {
////            query = "maxHttpBufferSize=10000000&timeout=0&pingInterval=2147483647"
////        }
//
////        val socket = IO.socket("http://20.124.20.8:5000/", options)
//        val socket = IO.socket("http://20.124.20.8:5000/", options)
//        socket.on(Socket.EVENT_CONNECT) {
////                Toast.makeText(applicationContext, "Connected", Toast.LENGTH_SHORT).show()
//            Log.e("SOCKET LOG", "Connected")
//
//        }.on(Socket.EVENT_DISCONNECT) {
////                Toast.makeText(applicationContext, "Disconnected", Toast.LENGTH_SHORT).show()
//            val disconnectIntent = Intent(
//                applicationContext,
//                MeasurementFailedActivity::class.java
//            )
//            startActivity(disconnectIntent)
//            Log.e("SOCKET LOG", "Disconnected")
//        }
//            .on(Socket.EVENT_CONNECT_ERROR) {
////                Toast.makeText(applicationContext, "Error connecting to network", Toast.LENGTH_SHORT).show()
//                Log.e(
//                    "SOCKET LOG",
//                    "Error making initial connection to server socket ///// kill program"
//                )
////                exitProcess(-1)
//            }.on("message_received") {
//                Log.e("SOCKET LOG", "message received")
//            }.on("results") { args ->
//
//                Log.e("results", "inside results")
//
//                val dataFromServer = args[0].toString()
//                val resultsIntent = Intent(
//                    applicationContext,
//                    DisplayResultsActivity::class.java
//                )
//                Log.e("results", dataFromServer)
//
//                val resultsObject = Json.parseToJsonElement(dataFromServer).jsonObject
//                resultsIntent.putExtra("hr", resultsObject["hr"].toString())
//                resultsIntent.putExtra("ibi", resultsObject["ibi"].toString())
//                resultsIntent.putExtra("sdnn", resultsObject["sdnn"].toString())
//                resultsIntent.putExtra("rmssd", resultsObject["rmssd"].toString())
//                resultsIntent.putExtra("pnn20", resultsObject["pnn20"].toString())
//                resultsIntent.putExtra("pnn50", resultsObject["pnn50"].toString())
//                resultsIntent.putExtra("hrv", resultsObject["hrv"].toString())
//                resultsIntent.putExtra("rr", resultsObject["rr"].toString())
//                resultsIntent.putExtra("sysbp", resultsObject["sysbp"].toString())
//                resultsIntent.putExtra("diabp", resultsObject["diabp"].toString())
//                resultsIntent.putExtra("spo2", resultsObject["spo2"].toString())
//                resultsIntent.putExtra("vo2max", resultsObject["vo2max"].toString())
//                resultsIntent.putExtra("si", resultsObject["si"].toString())
//                resultsIntent.putExtra("mhr", resultsObject["mhr"].toString())
//                resultsIntent.putExtra("hrr", resultsObject["hrr"].toString())
//                resultsIntent.putExtra("thr", resultsObject["thr"].toString())
//                resultsIntent.putExtra("co", resultsObject["co"].toString())
//                resultsIntent.putExtra("map", resultsObject["map"].toString())
//                resultsIntent.putExtra("hu", resultsObject["hu"].toString())
//                resultsIntent.putExtra("bv", resultsObject["bv"].toString())
//                resultsIntent.putExtra("tbw", resultsObject["tbw"].toString())
//                resultsIntent.putExtra("bwp", resultsObject["bwp"].toString())
//                resultsIntent.putExtra("bmi", resultsObject["bmi"].toString())
//                resultsIntent.putExtra("bf", resultsObject["bf"].toString())
//                resultsIntent.putExtra("asth_risk", resultsObject["asth_risk"].toString())
//                startActivity(resultsIntent)
//
//            }
//
//        if (!socket.connected()) socket.connect()
//        else {
//            socket.disconnect()
//            socket.connect()
//        }
////        } catch (e: URISyntaxException) {
////            e.printStackTrace()
////            Log.e("socket err", "URI Syntax Exception ///// kill program")
////        }



        val cameraManager: CameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        var cameraID = "-1"
        for (id in cameraManager.cameraIdList) {
            if (cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.LENS_FACING)
                == CameraCharacteristics.LENS_FACING_FRONT
            ) {
                cameraID = id
            }
        }

//            Toast.makeText(
//                applicationContext,
//                cameraManager.getCameraCharacteristics(cameraID).get(INFO_SUPPORTED_HARDWARE_LEVEL)
//                    .toString(),
//                Toast.LENGTH_SHORT
//            ).show()

        val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID)

        val cameraConfigMap = cameraCharacteristics.get(SCALER_STREAM_CONFIGURATION_MAP)
        for (format in cameraConfigMap!!.outputFormats) {
            Log.e("Availablee Outputs", format.toString())
        }


        outputResolution.clear()
        for (size in cameraConfigMap.getOutputSizes(ImageFormat.YUV_420_888)) {

            if (size.width * size.height < 100000) {
                outputResolution.add(size.width)
                outputResolution.add(size.height)
                break
            }
            Log.e("Availablee Output Sizes", size.toString())
        }

        val rawSize = Size(outputResolution[0], outputResolution[1])

        if (outputResolution.isEmpty()) {
            exitProcess(-1)
        }

//        Toast.makeText(applicationContext, "Size selected is $rawSize", Toast.LENGTH_LONG).show()
        Log.e(
            "Availablee min frame duration",
            (cameraConfigMap.getOutputMinFrameDuration(
                ImageFormat.YUV_420_888,
                rawSize
            ) / 1_000_000.0).toString()
        )

        Log.e(
            "Availablee min stall duration",
            cameraConfigMap.getOutputStallDuration(ImageFormat.YUV_420_888, rawSize).toString()
        )


        val backgroundThread = HandlerThread("BackgroundThread")
        backgroundThread.start()
        Handler(backgroundThread.looper)

        val imageReader: ImageReader = ImageReader.newInstance(
            outputResolution[0],
            outputResolution[1],
            ImageFormat.YUV_420_888,
            1
        )


        var a = 0
        var loadProbeIterator = 0
        imageReader.setOnImageAvailableListener({
            val mImage: Image = it.acquireNextImage()
            val yuvByteArray = convertYUV420888ToNV21(
                mImage.width, mImage.height,
                mImage.planes[0].buffer, mImage.planes[1].buffer, mImage.planes[2].buffer,
                mImage.planes[1].pixelStride, mImage.planes[2].pixelStride,
                mImage.planes[1].rowStride, mImage.planes[2].rowStride
            )

            yuvImageByteArrays.add(yuvByteArray)
            a += 1
//            println("A => $a")

            // create uid for session
            if (yuvImageByteArrays.size % batchSize == 0 && a <= 900) {
                loadProbeIterator += 1
//                    howMuchDoneTV.text = loadProbeIterator.toString()

                val copyArr = ArrayList(yuvImageByteArrays)
                yuvImageByteArrays.clear()
                val imageParams =
                    ImageParams(
                        copyArr,
                        loadProbeIterator,
                        requestUID,
                        applicationContext,
                        outputResolution,
                        a,
                        intent
                    )
                ImageProcessingTask().execute(imageParams)

                if ( a >= 899){
                    setContentView(R.layout.loading_screen)
                }
            }

            mImage.close()

        }, null)

//        val mediaRecorder = MediaRecorder(applicationContext)
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//        mediaRecorder.setVideoSize(outputResolution[0], outputResolution[1])
//        mediaRecorder.setVideoFrameRate(30)
//        mediaRecorder.setOutputFile("${getExternalFilesDir("Downloads")}/examplevid.mp4")
//        mediaRecorder.setVideoEncodingBitRate(10_000_000)
//
//        mediaRecorder.prepare()

        textureView.surfaceTextureListener = object : SurfaceTextureListener {
            @SuppressLint("MissingPermission")
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
//                        val surfaceTexture: SurfaceTexture? = textureView.surfaceTexture
                surface.setDefaultBufferSize(1920, 1080)
                val previewSurface: Surface = Surface(surface)
//                val recordingSurface = mediaRecorder.surface


                cameraManager.openCamera(cameraID, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        val captureRequestBuilder =
                            camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
                        captureRequestBuilder.addTarget(previewSurface)
//                        captureRequestBuilder.addTarget(recordingSurface)
                        captureRequestBuilder.addTarget(imageReader.surface)

                        val captureStateVideoCallback =
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Configuration Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                override fun onConfigured(session: CameraCaptureSession) {

                                    captureRequestBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureResult.CONTROL_AF_MODE_CONTINUOUS_VIDEO
                                    )
                                    try {
                                        session.setRepeatingRequest(
                                            captureRequestBuilder.build(), null,
                                            null
                                        )

//                                        mediaRecorder.start()
                                    } catch (e: CameraAccessException) {
                                        e.printStackTrace()
                                        Log.e(
                                            TAG,
                                            "Failed to start camera preview because it couldn't access the camera"
                                        )
                                    } catch (e: IllegalStateException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        recordButton.apply {
                            setOnClickListener {
                                if (recordingStarted == 0) {

                                    if (isNetworkAvailable()) {
                                        recordButton.text = "Scanning..."
                                        recordingStarted = 1

//                                        Toast.makeText(
//                                            applicationContext,
//                                            "Started Recording",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
                                        camera.createCaptureSession(
                                            listOf(
                                                previewSurface,
//                                                recordingSurface,
                                                imageReader.surface
                                            ), captureStateVideoCallback, null
                                        )

                                        val timer = object : CountDownTimer(35000, 1000) {
                                            override fun onTick(millisUntilFinished: Long) {
                                                timerView.text =
                                                    (millisUntilFinished / 1000).toString();
                                            }

                                            override fun onFinish() {
//                                                Toast.makeText(
//                                                    applicationContext,
//                                                    "Stopped Recording",
//                                                    Toast.LENGTH_SHORT
//                                                ).show()

//                                                Log.e("SOCKET LOG", "TRANSMISSION ENDED")

                                                recordButton.text = "Getting results..."

//                                    if (socket.connected()){
//                                        socket.emit("message_end")
//                                    }else{
//                                        Log.e("socket err", "Transmission end socket connection couldn't be established")
//                                    }
//                                                mediaRecorder.stop()
//                                                mediaRecorder.reset()
                                            }
                                        }
                                        timer.start()
                                    } else {
                                        Toast.makeText(applicationContext, "Please connect to the internet", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "Scan has already started",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        val captureRequestBuilderPreview =
                            camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                        captureRequestBuilderPreview.addTarget(previewSurface)
                        val captureRequestPreview = captureRequestBuilderPreview.build()

                        camera.createCaptureSession(
                            listOf(previewSurface),
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigured(session: CameraCaptureSession) {
                                    session.setRepeatingRequest(captureRequestPreview, null, null)
                                }

                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Capture Session Configuration Failed", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            null
                        )
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        Log.e(TAG, "Camera OnDisconnected called")

                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        Log.e(TAG, "Camera OnError called")

                    }
                }, null)

            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                Log.e(TAG, "Surface destroyed")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Log.e(TAG, "Surface destroyed")
                return true;
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
//                Log.e(TAG, "Surface updated")
            }
        }
    }
}


