package de.tpohrer.tpcolorselectionseekbarexample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import de.tpohrer.tpcolorselectionseekbar.TPColorSelectionSeekBar

class MainActivity : AppCompatActivity(), TPColorSelectionSeekBar.ISelectedColorChangedListener {
    private lateinit var barDefault: TPColorSelectionSeekBar
    private lateinit var barCustomized: TPColorSelectionSeekBar
    private lateinit var viewDefault: View
    private lateinit var viewCustomized: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewDefault = findViewById(R.id.viewDefault)
        viewCustomized = findViewById(R.id.viewCustomized)

        barDefault = findViewById(R.id.colorBarDefault)
        barDefault.setColorSelectionChangedListener(this)

        onSelectedColorChanged(barDefault.getCurrentColor(), barDefault.id)

        barCustomized = findViewById(R.id.colorBarCustomized)
        barCustomized.setColorSelectionChangedListener(this)

        onSelectedColorChanged(barCustomized.getCurrentColor(), barCustomized.id)
    }

    override fun onSelectedColorChanged(color: Int, viewId: Int) {
        when(viewId) {
            R.id.colorBarDefault -> viewDefault.setBackgroundColor(color)
            R.id.colorBarCustomized -> viewCustomized.setBackgroundColor(color)
        }
    }
}
