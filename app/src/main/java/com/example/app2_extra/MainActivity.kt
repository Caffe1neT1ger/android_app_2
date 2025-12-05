package com.example.app2_extra

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Логин и пароль для пользователя
    private val correctLogin = "admin"
    private val correctPassword = "12345"
    // Кол-во попыток
    private val MAX_ATTEMPTS = 3
    // Ключи для доступа к данным из shared preferences
    private val PREFS_NAME = "login_prefs"
    private val KEY_ATTEMPTS = "attempts_cnt"
    // Ключ количества неудач
    private var attemptCount = 0

    // Запрос на ввод системного PIN/пароля/отпечатка
    private val credentialIntentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Пользователь корректно ввел PIN код
            startActivity(Intent(this, SecondActivity::class.java))
        } else {
            // Некорректный ввод или отмена ввода
            Toast.makeText(this, getString(R.string.error_device_pin), Toast.LENGTH_SHORT).show()
        }
    }
    // Обработчик для кнопки Login
    private fun handleLogin(
        login: String,
        password: String,
        btnLogin: Button
    ) {

        // Проверка введенных данных
        if (login == correctLogin && password == correctPassword) {
            // Сброс кол-ва попыток
            resetAttempts()
            // Функция вызова системного интерфейса PIN кода
            askForDeviceCredential()
        } else {
            // Увеличиваем кол-во неуспешных попыток
            attemptCount++
            // Сохраняем кол-во неуспешных попыток в shared preferences
            saveAttempts()

            // Логика проверки кол-ва неуспешных попыток и блокировка кнопки
            if (attemptCount >= MAX_ATTEMPTS) {
                // Блокировка кнопки
                blockLoginButton(btnLogin)
                // Вызов тоста
                Toast.makeText(
                    this,
                    "Превышено количество попыток. Кнопка отключена.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Кол-во оставшихся попыток
                val left = MAX_ATTEMPTS - attemptCount
                // Вызов информационного тоста
                Toast.makeText(
                    this,
                    "Осталось попыток: $left",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    // Блокировка кнопки
    private fun blockLoginButton(button: Button) {
        button.isEnabled = false
        // Изменение прозрачности
        button.alpha = 0.5f
    }
    // Сохранение кол-ва неуспешных попыток в shared preferences
    private fun saveAttempts() {
        // Получение данных по ключу из shared preferences
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Изменение данных
        prefs.edit().putInt(KEY_ATTEMPTS, attemptCount).apply()
    }

    // Обнуление кол-ва неуспешных попыток
    private fun resetAttempts() {
        attemptCount = 0
        // Получение данных по ключу из shared preferences
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Изменение данных
        prefs.edit().remove(KEY_ATTEMPTS).apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        attemptCount = prefs.getInt(KEY_ATTEMPTS, 0)

        // Получение объектов UI по их шв для взаимодействия с ними
        val etLogin = findViewById<EditText>(R.id.etLogin)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Обработчик событий для кнопки
        btnLogin.setOnClickListener {
            if (btnLogin.isEnabled) {
                handleLogin(
                    // Передаем данные из текстовых полей
                    login = etLogin.text.toString(),
                    password = etPassword.text.toString(),
                    btnLogin = btnLogin
                )
            }
        }
    }

    // Вызов системного интерфейса PIN кода
    private fun askForDeviceCredential() {
        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        // Проверка на наличие функций защиты (PIN, отпечаток, сканер и т.д.)
        if (km.isDeviceSecure) {
            val intent = km.createConfirmDeviceCredentialIntent(
                "Требуется подтверждение",
                "Введите PIN/пароль/отпечаток"
            )
            // Если создается объект, то есть устройство поддерживает данный функционал
            if (intent != null) {
                credentialIntentLauncher.launch(intent)
                return
            }
        }
        // Тост отсутсвия функций защиты
        Toast.makeText(this, "Устройство не защищено PIN‑кодом", Toast.LENGTH_SHORT).show()
    }
}