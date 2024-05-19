package com.actofit.vitalscan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

class DisplayResultsActivity: ComponentActivity(){

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val backToMainScreen: Intent = Intent(this, UserDetailsActivity::class.java)
        backToMainScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(backToMainScreen)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.results)

        val hr = intent.getStringExtra("hr")
        val ibi = intent.getStringExtra("ibi")
        val sdnn = intent.getStringExtra("sdnn")
        val rmssd = intent.getStringExtra("rmssd")
        val pnn20 = intent.getStringExtra("pnn20")
        val pnn50 = intent.getStringExtra("pnn50")
        val hrv = intent.getStringExtra("hrv")
        val rr = intent.getStringExtra("rr")
//        val prq = intent.getStringExtra("prq")
        val sysbp = intent.getStringExtra("sysbp")
        val diabp = intent.getStringExtra("diabp")
        val spo2 = intent.getStringExtra("spo2")
        val vo2max = intent.getStringExtra("vo2max")
        val si = intent.getStringExtra("si")
        val mhr = intent.getStringExtra("mhr")
        val hrr = intent.getStringExtra("hrr")
        val thr = intent.getStringExtra("thr")
        val co = intent.getStringExtra("co")
        val map = intent.getStringExtra("map")
        val hu = intent.getStringExtra("hu")
        val bv = intent.getStringExtra("bv")
        val tbw = intent.getStringExtra("tbw")
        val bwp = intent.getStringExtra("bwp")
        val bmi = intent.getStringExtra("bmi")
        val bf = intent.getStringExtra("bf")
        val asth_risk = intent.getStringExtra("asth_risk")

//        val hrValue = findViewById<TextView>(R.id.bpm_value)
//        val ibiValue = findViewById<TextView>(R.id.ibi_value)
//        val sdnnValue = findViewById<TextView>(R.id.sdnn_value)
//        val rmssdValue = findViewById<TextView>(R.id.rmssd_value)
//        val pnn20Value = findViewById<TextView>(R.id.pnn20_value)
//        val pnn50Value = findViewById<TextView>(R.id.pnn50_value)
//        val hrvValue = findViewById<TextView>(R.id.hrv_value)
//        val rrValue = findViewById<TextView>(R.id.rr_value)
//        val prqValue = findViewById<TextView>(R.id.prq_value)
//        val sysbpValue = findViewById<TextView>(R.id.sysbp_value)
//        val diabpValue = findViewById<TextView>(R.id.diabp_value)
//        val spo2Value = findViewById<TextView>(R.id.spo2_value)
//        val vo2MaxValue = findViewById<TextView>(R.id.vo2max_value)
//        val siValue = findViewById<TextView>(R.id.si_value)
//        val mhrValue = findViewById<TextView>(R.id.mhr_value)
//        val hrrValue = findViewById<TextView>(R.id.hrr_value)
//        val thrValue = findViewById<TextView>(R.id.thr_value)
//        val coValue = findViewById<TextView>(R.id.co_value)
//        val mapValue = findViewById<TextView>(R.id.map_value)
//        val huValue = findViewById<TextView>(R.id.hu_value)
//        val bvValue = findViewById<TextView>(R.id.bv_value)
//        val tbwValue = findViewById<TextView>(R.id.tbw_value)
//        val bwValue = findViewById<TextView>(R.id.bw_value)
//        val bmiValue = findViewById<TextView>(R.id.bmi_value)


//        hrValue.text = hr!!.slice(IntRange(1, 5))
//        ibiValue.text = ibi!!.slice(IntRange(1, 5))
//        sdnnValue.text = sdnn!!.slice(IntRange(1, 5))
//        rmssdValue.text = rmssd!!.slice(IntRange(1, 5))
//        pnn20Value.text = pnn20!!.slice(IntRange(1, 5))
//        pnn50Value.text = pnn50!!.slice(IntRange(1, 5))
//        hrvValue.text = hrv!!.slice(IntRange(1, 5))
//        rrValue.text = rr!!.slice(IntRange(1, 5))
////        prqValue.text = prq!!.slice(IntRange(0, 5))
//        sysbpValue.text = sysbp!!.slice(IntRange(1, 5))
//        diabpValue.text = diabp!!.slice(IntRange(1, 5))
//        spo2Value.text = spo2!!.slice(IntRange(3, 5))
//        vo2MaxValue.text = vo2max!!.slice(IntRange(1, 5))
//        siValue.text = si!!.slice(IntRange(1, 5))
//        mhrValue.text = mhr!!.slice(IntRange(1, 5))
//        hrrValue.text = hrr!!.slice(IntRange(1, 5))
//        thrValue.text = thr!!.slice(IntRange(1, 5))
//        coValue.text = co!!.slice(IntRange(3, 5))
//        mapValue.text = map!!.slice(IntRange(3, 5))
//        huValue.text = hu!!.slice(IntRange(1, 5))
//        bvValue.text = bv!!.slice(IntRange(1, 5))
//        tbwValue.text = tbw!!.slice(IntRange(1, 5))
//        bwValue.text = bwp!!.slice(IntRange(1, 5))
//        bmiValue.text = bmi!!.slice(IntRange(1, 5))


        val webView = findViewById<WebView>(R.id.webView)
        // WebViewClient allows you to handle
        // onPageFinished and override Url loading.
        webView.webViewClient = WebViewClient()

        // this will load the url of the website

//        https://kind-bay-0eed8bd00.5.azurestaticapps.net
        webView.loadUrl("http://kind-bay-0eed8bd00.5.azurestaticapps.net?hr=$hr&rr=$rr&spo2=$spo2&sysbp=$sysbp&diabp=$diabp" +
                "&ibi=$ibi&sdnn=$sdnn&rmssd=$rmssd&pnn20=$pnn20&pnn50=$pnn50&hrv=$hrv&bmi=$bmi&co=$co&vo2max=$vo2max&hu=$hu&map=$map&asth_risk=$asth_risk")

//        Toast.makeText(applicationContext, "http://192.168.1.4:3000?hr=$hr&rr=$rr&spo2=$spo2&sysbp=$sysbp&diabp=$diabp" +
//                "&ibi=$ibi&sdnn=$sdnn&rmssd=$rmssd&pnn20=$pnn20&pnn50=$pnn50&hrv=$hrv&bmi=$bmi&co=$co&vo2max=$vo2max&hu=$hu&map=$map", Toast.LENGTH_SHORT).show()

        // this will enable the javascript settings, it can also allow xss vulnerabilities
        webView.settings.javaScriptEnabled = true

    }
}