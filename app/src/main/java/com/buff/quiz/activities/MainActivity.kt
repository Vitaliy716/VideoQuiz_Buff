package com.buff.quiz.activities

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.buff.quiz.R
import com.buff.quiz.api.RetrofitClient
import com.buff.quiz.models.MyResultResponse
import com.buff.quiz.models.QuizResponse
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream
import java.net.URL


class MainActivity : AppCompatActivity() {

    private var quizIndex = 0
    private val handler = Handler()
    private var totalPts = 0
    private var nProgressSec = 0
    private var isAnswered = false
    private val textViews: ArrayList<TextView> = ArrayList()
    private val answerBtns: ArrayList<androidx.constraintlayout.widget.ConstraintLayout> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation =  (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        setContentView(R.layout.activity_main)

        val packageName = "com.buff.quiz"
        val filePlace = "android.resource://" + packageName + "/raw/" + R.raw.buff
        videoView.setVideoURI(Uri.parse(filePlace))
        videoView.start()

        hideQuiz()

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                quizIndex++
                getVideoQuiz()
                if (quizIndex < 6)
                    mainHandler.postDelayed(this, 10000)
            }
        })


        val tvIds: IntArray = intArrayOf(R.id.answer1_text, R.id.answer2_text, R.id.answer3_text)
        val btnIds: IntArray = intArrayOf(R.id.answer1, R.id.answer2, R.id.answer3)

        for (index in 0 until tvIds.count()) {
            val tv: TextView = findViewById(tvIds[index])
            val btn: androidx.constraintlayout.widget.ConstraintLayout = findViewById(btnIds[index])
            textViews.add(tv)
            answerBtns.add(btn)
        }

        btn_close.setOnClickListener {
            hideQuiz()
        }

        answer1.setOnClickListener {
            answerQuiz()
        }

        answer2.setOnClickListener {
            answerQuiz()
        }

        answer3.setOnClickListener {
            answerQuiz()
        }

    }

    fun getVideoQuiz() {
        RetrofitClient.instance.getQuiz(quizIndex)
            .enqueue(object: retrofit2.Callback<MyResultResponse> {
                override fun onFailure(call: retrofit2.Call<MyResultResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: retrofit2.Call<MyResultResponse>, response: retrofit2.Response<MyResultResponse>) {
                    showQuiz(response.body()?.result!!)
                }
            })
    }

    fun showQuiz(quizInfo: QuizResponse) {
        isAnswered = false
        author_section.visibility = View.VISIBLE
        question_section.visibility = View.VISIBLE
        progress_section.visibility = View.VISIBLE
        answer_section.visibility = View.VISIBLE

        sender_author.text = quizInfo.author.first_name + " " + quizInfo.author.last_name
        DownloadImageTask(avatar).execute(quizInfo.author.image)
        question_title.text = quizInfo.question.title
        question_second.text = quizInfo.time_to_show.toString()
        for (i in 0 until 3) {
            if (i >= quizInfo.answers.count())
                answerBtns[i].visibility = View.INVISIBLE
            else {
                answerBtns[i].visibility = View.VISIBLE
                textViews[i].text = quizInfo.answers[i].title
            }
        }
//        answer1_text.text = quizInfo.answers[0].title
//        answer2_text.text = quizInfo.answers[1].title
//        answer3_text.text = quizInfo.answers[2].title

        var nProgressVal = 100
        nProgressSec = quizInfo.time_to_show * 1000
        nProgressVal = question_progress!!.progress
        Thread(Runnable {
            while (nProgressVal > 0 && !isAnswered) {
                nProgressVal = (((nProgressSec.toDouble() / 1000) / quizInfo.time_to_show) * 100).toInt()
//                Log.e("nprogressval",nProgressVal.toString())
                // Update the progress bar and display the current value
                handler.post(Runnable {
                    question_progress!!.progress = nProgressVal
                    question_second!!.text = (nProgressSec / 1000).toString()//nProgressVal.toString() + "/" + progressBar!!.max
                })
                try {
                    Thread.sleep(100)
                    nProgressSec -= 100
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            if (!isAnswered)
                hideQuiz()
        }).start()
    }

    private fun answerQuiz() {
        isAnswered = true;
        answer_section.visibility = View.INVISIBLE
        progress_section.visibility = View.INVISIBLE

        totalPts += (nProgressSec / 1000)
        Toast.makeText(applicationContext, "Total Pts: $totalPts", Toast.LENGTH_LONG).show()

        Handler().postDelayed({
            hideQuiz()
        }, 2000)
    }

    private fun hideQuiz() {
        author_section.visibility = View.INVISIBLE
        question_section.visibility = View.INVISIBLE
        answer_section.visibility = View.INVISIBLE
    }
}

private class DownloadImageTask(bmImage: ImageView) :
    AsyncTask<String?, Void?, Bitmap?>() {
    val bmImage: ImageView = bmImage
    override fun doInBackground(vararg urls: String?): Bitmap? {
        val urldisplay = urls[0]
        var bmp: Bitmap? = null
        try {
            val `in`: InputStream = URL(urldisplay).openStream()
            bmp = BitmapFactory.decodeStream(`in`)
        } catch (e: Exception) {
            Log.e("Error", e.message)
            e.printStackTrace()
        }
        return bmp
    }

    override fun onPostExecute(result: Bitmap?) {
        bmImage.setImageBitmap(result)
    }
}