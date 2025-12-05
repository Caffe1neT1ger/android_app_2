package com.example.app2_extra

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val btnExit = findViewById<Button>(R.id.btnExit)

        // Выход из приложения – закрыть все активити
        btnExit.setOnClickListener {
            // finishAffinity() закрывает текущую и все родительские задачи
            finishAffinity()
        }
    }
}
