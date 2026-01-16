package com.example.p2p_lending

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream

class ChartActivity : AppCompatActivity() {

    private val databaseUrl = "https://p2p-lending-app-8d324-default-rtdb.europe-west1.firebasedatabase.app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        val barChart = findViewById<BarChart>(R.id.barChart)
        val btnBack = findViewById<Button>(R.id.btnBackChart)
        val btnShare = findViewById<Button>(R.id.btnShare)

        val usersRef = FirebaseDatabase.getInstance(databaseUrl).getReference("users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = ArrayList<User>()
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    if (user != null && user.totalInvested > 0) {
                        userList.add(user)
                    }
                }
                userList.sortByDescending { it.totalInvested }
                val topUsers = userList.take(10)

                val entries = ArrayList<BarEntry>()
                val names = ArrayList<String>()

                for ((index, user) in topUsers.withIndex()) {
                    entries.add(BarEntry(index.toFloat(), user.totalInvested.toFloat()))
                    names.add(user.username)
                }

                val dataSet = BarDataSet(entries, "")
                dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                dataSet.valueTextSize = 12f

                val barData = BarData(dataSet)
                barChart.data = barData

                val xAxis = barChart.xAxis
                xAxis.valueFormatter = IndexAxisValueFormatter(names)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                xAxis.labelRotationAngle = -0f

                barChart.axisRight.isEnabled = false
                barChart.description.isEnabled = false
                barChart.animateY(1500)
                barChart.invalidate()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        btnBack.setOnClickListener { finish() }

        btnShare.setOnClickListener {
            shareChart()
        }
    }


    private fun shareChart() {
        try {

            val rootView = window.decorView.rootView
            val bitmap = getBitmapFromView(rootView)


            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Clasament P2P", null)
            val uri = Uri.parse(path)

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/jpeg"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Top Investitori P2P")
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Uite clasamentul investitorilor din aplicație!")

            startActivity(Intent.createChooser(shareIntent, "Trimite Clasamentul prin..."))

        } catch (e: Exception) {
            Toast.makeText(this, "Eroare la partajare: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }
}