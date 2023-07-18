package br.dev.murilopereira.todo.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.dev.murilopereira.todo.databinding.ActivitySubtaskListBinding

class ActivitySubtaskList : AppCompatActivity() {
    private val binding by lazy {
        ActivitySubtaskListBinding.inflate(layoutInflater);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}