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

    // "База" пользователей – только для примера
    private val correctLogin = "admin"
    private val correctPassword = "12345"
    private val MAX_ATTEMPTS = 3                     // сколько попыток разрешено
    private val PREFS_NAME = "login_prefs"           // имя файла SharedPreferences
    private val KEY_ATTEMPTS = "attempts_cnt"
    private var attemptCount = 0 // ключ количества неудач

    // Запрос на ввод системного PIN/пароля/отпечатка
    private val credentialIntentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Пользователь прошёлку устройства →ходим к com.example.app2_extra.SecondActivity
            startActivity(Intent(this, SecondActivity::class.java))
        } else {
            // НЕ прошёл (отменила форма или ввёл неверно)
            Toast.makeText(this, getString(R.string.error_device_pin), Toast.LENGTH_SHORT).show()
        }
    }
    /**рабатывает ввод логина/пароля */
    private fun handleLogin(
        login: String,
        password: String,
        btnLogin: Button
    ) {
        if (login == correctLogin && password == correctPassword) {
            // ✅ Успешный вход – сбрасываем счётчик
            resetAttempts()
            askForDeviceCredential()
        } else {
            // ❌ Ошибка – увеличиваем счётчик
            attemptCount++
            saveAttempts()

            if (attemptCount >= MAX_ATTEMPTS) {
                // Достигнут лимит – блокируем кнопку
                blockLoginButton(btnLogin)
                Toast.makeText(
                    this,
                    "Превышено количество попыток. Кнопка отключена.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Сообщаем сколько осталось попыток
                val left = MAX_ATTEMPTS - attemptCount
                Toast.makeText(
                    this,
                    "Осталось попыток: $left",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun blockLoginButton(button: Button) {
        button.isEnabled = false
        // Изменяем прозрачность, чтобы пользователь видел, что кнопка отключена
        button.alpha = 0.5f
    }
    /** Сохраняем текущее количество неверных попыток */
    private fun saveAttempts() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_ATTEMPTS, attemptCount).apply()
    }

    /** После успешного входа обнуляем сччик */
    private fun resetAttempts() {
        attemptCount = 0
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_ATTEMPTS).apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        attemptCount = prefs.getInt(KEY_ATTEMPTS, 0)

        val etLogin = findViewById<EditText>(R.id.etLogin)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            if (btnLogin.isEnabled) {
                handleLogin(
                    login = etLogin.text.toString(),
                    password = etPassword.text.toString(),
                    btnLogin = btnLogin
                )
            }
        }
    }

    /** Показывает системный диалог подтверждения учётных данных устройства */
    private fun askForDeviceCredential() {
        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        // Если устройство поддерживает такой запрос
        if (km.isDeviceSecure) {
            val intent = km.createConfirmDeviceCredentialIntent(
                "Требуется подтверждение",
                "Введите PIN/пароль/отпечаток"
            )
            // Если пользователь вообще может подтвердить (все устройства с Android 5+)
            if (intent != null) {
                credentialIntentLauncher.launch(intent)
                return
            }
        }
        // Если по какой‑то причине нельзя (например, без экрана блокировки)
        Toast.makeText(this, "Устройство не защищено PIN‑кодом", Toast.LENGTH_SHORT).show()
    }
}