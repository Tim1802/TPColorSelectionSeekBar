package de.tpohrer.tpcolorselectionseekbarexample

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import de.tpohrer.tpcolorselectionseekbar.TPColorSelectionSeekBar

class MainActivity : AppCompatActivity(), TPColorSelectionSeekBar.ISelectedColorChangedListener {
    private lateinit var barDefault: TPColorSelectionSeekBar
    private lateinit var barDefaultAlpha: TPColorSelectionSeekBar
    private lateinit var barCustomized: TPColorSelectionSeekBar
    private lateinit var barCustomizedAlpha: TPColorSelectionSeekBar
    private lateinit var viewDefault: View
    private lateinit var viewDefaultAlpha: View
    private lateinit var viewCustomized: View
    private lateinit var viewCustomizedAlpha: View
    private lateinit var buttonSheet: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewDefault = findViewById(R.id.viewDefault)
        viewCustomized = findViewById(R.id.viewCustomized)
        viewCustomizedAlpha = findViewById(R.id.viewCustomizedAlpha)
        viewDefaultAlpha = findViewById(R.id.viewDefaultAlpha)

        barDefault = findViewById(R.id.colorBarDefault)
        barDefault.setColorSelectionChangedListener(this)

        onSelectedColorChanged(barDefault.getCurrentColor(), barDefault.id)

        barCustomized = findViewById(R.id.colorBarCustomized)
        barCustomized.setColorSelectionChangedListener(this)

        onSelectedColorChanged(barCustomized.getCurrentColor(), barCustomized.id)

        barDefaultAlpha = findViewById(R.id.colorBarDefaultAlpha)
        barDefaultAlpha.setColorSelectionChangedListener(this)

        onSelectedColorChanged(barDefaultAlpha.getCurrentColor(), barDefaultAlpha.id)

        barCustomizedAlpha = findViewById(R.id.colorBarCustomizedAlpha)
        barCustomizedAlpha.setColorSelectionChangedListener(this)

        onSelectedColorChanged(barCustomizedAlpha.getCurrentColor(), barCustomizedAlpha.id)

        buttonSheet = findViewById(R.id.buttonShowSheet)
        buttonSheet.setOnClickListener {
            showBottomSheet()
        }
    }

    override fun onSelectedColorChanged(color: Int, viewId: Int) {
        when(viewId) {
            R.id.colorBarDefault -> viewDefault.setBackgroundColor(color)
            R.id.colorBarCustomized -> viewCustomized.setBackgroundColor(color)
            R.id.colorBarDefaultAlpha -> viewDefaultAlpha.setBackgroundColor(color)
            R.id.colorBarCustomizedAlpha -> viewCustomizedAlpha.setBackgroundColor(color)
        }
    }

    private fun showBottomSheet() {
        val fragmentManager = supportFragmentManager

        val sheet = BottomSheet()
        sheet.show(fragmentManager, "bottomSheet")
    }
}
