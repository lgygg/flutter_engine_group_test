package com.example.flutter_engine_group_test

import android.R
import android.app.ListActivity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.flutter_engine_group_test.DataModel

class MultFlutterTestActivity : ListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val array = arrayOf(
            "addObserver",
            "removeObserver",
            "SingleFlutterActivity",
            "DoubleFlutterActivity",
            "get",
            "set"
        )
        listAdapter = ArrayAdapter(this, R.layout.simple_list_item_1, array)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Deprecated("Deprecated in Java")
    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        when (position) {
            0 -> DataModel.addObserver(::observer)
            1 -> DataModel.removeObserver(this::observer)
            2 -> startActivity(Intent(this, SingleFlutterActivity::class.java))
            3 -> startActivity(Intent(this, DoubleFlutterActivity::class.java))
            4 -> Toast.makeText(this, "data: ${DataModel.data}", Toast.LENGTH_SHORT).show()
            5 -> DataModel.data += 1
        }
    }

    private fun observer(data: Int): Unit = Toast.makeText(this, "observer: $data", Toast.LENGTH_SHORT).show()
}