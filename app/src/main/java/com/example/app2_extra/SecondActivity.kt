package com.example.app2_extra

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Получение обхекта кнопки по id
        val btnExit = findViewById<Button>(R.id.btnExit)

        // Обработчик событий на кнопку на нажатие
        btnExit.setOnClickListener {
            // finishAffinity() закрывает текущую и все родительские задачи
            finishAffinity()
        }
    }
}
