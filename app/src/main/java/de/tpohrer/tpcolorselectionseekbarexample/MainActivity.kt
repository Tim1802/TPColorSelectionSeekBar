package de.tpohrer.tpcolorselectionseekbarexample

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import de.tpohrer.tpcolorselectionseekbar.TPColorSelectionSeekBar

class MainActivity : AppCompatActivity() {
    private lateinit var barDefault: TPColorSelectionSeekBar
    private lateinit var barDefaultVertical: TPColorSelectionSeekBar
    private lateinit var barDefaultVerticalAlpha: TPColorSelectionSeekBar
    private lateinit var barDefaultAlpha: TPColorSelectionSeekBar
    private lateinit var barCustomized: TPColorSelectionSeekBar
    private lateinit var barCustomizedAlpha: TPColorSelectionSeekBar
    private lateinit var viewDefault: View
    private lateinit var viewDefaultVertical: View
    private lateinit var viewDefaultVerticalAlpha: View
    private lateinit var viewDefaultAlpha: View
    private lateinit var viewCustomized: View
    private lateinit var viewCustomizedAlpha: View
    private lateinit var buttonSheet: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewDefault = findViewById(R.id.viewDefault)
        viewDefaultVertical = findViewById(R.id.viewDefaultVertical)
        viewDefaultVerticalAlpha = findViewById(R.id.viewDefaultVerticalAlpha)
        viewCustomized = findViewById(R.id.viewCustomized)
        viewCustomizedAlpha = findViewById(R.id.viewCustomizedAlpha)
        viewDefaultAlpha = findViewById(R.id.viewDefaultAlpha)

        barDefault = findViewById(R.id.colorBarDefault)
        barDefault.setColorSelectionChangedListener(::colorChangedListener)

        colorChangedListener(barDefault.getCurrentColor(), barDefault.id)

        barCustomized = findViewById(R.id.colorBarCustomized)
        barCustomized.setColorSelectionChangedListener(::colorChangedListener)

        colorChangedListener(barCustomized.getCurrentColor(), barCustomized.id)

        barDefaultAlpha = findViewById(R.id.colorBarDefaultAlpha)
        barDefaultAlpha.setColorSelectionChangedListener(::colorChangedListener)

        colorChangedListener(barDefaultAlpha.getCurrentColor(), barDefaultAlpha.id)

        barCustomizedAlpha = findViewById(R.id.colorBarCustomizedAlpha)
        barCustomizedAlpha.setColorSelectionChangedListener(::colorChangedListener)

        colorChangedListener(barCustomizedAlpha.getCurrentColor(), barCustomizedAlpha.id)

        barDefaultVertical = findViewById(R.id.colorBarDefaultVertical)
        barDefaultVertical.setColorSelectionChangedListener(::colorChangedListener)

        colorChangedListener(barDefaultVertical.getCurrentColor(), barDefaultVertical.id)

        barDefaultVerticalAlpha = findViewById(R.id.colorBarDefaultVerticalAlpha)
        barDefaultVerticalAlpha.setColorSelectionChangedListener(::colorChangedListener)

        colorChangedListener(barDefaultVerticalAlpha.getCurrentColor(), barDefaultVerticalAlpha.id)

        buttonSheet = findViewById(R.id.buttonShowSheet)
        buttonSheet.setOnClickListener {
            BottomSheet().show(supportFragmentManager, "bottomSheet")
        }
    }

    private fun colorChangedListener (color: Int, viewId : Int) {
        when (viewId) {
            R.id.colorBarDefault              -> viewDefault.setBackgroundColor(color)
            R.id.colorBarDefaultVertical      -> viewDefaultVertical.setBackgroundColor(color)
            R.id.colorBarCustomized           -> viewCustomized.setBackgroundColor(color)
            R.id.colorBarDefaultAlpha         -> viewDefaultAlpha.setBackgroundColor(color)
            R.id.colorBarCustomizedAlpha      -> viewCustomizedAlpha.setBackgroundColor(color)
            R.id.colorBarDefaultVerticalAlpha -> viewDefaultVerticalAlpha.setBackgroundColor(color)
        }
    }
}
