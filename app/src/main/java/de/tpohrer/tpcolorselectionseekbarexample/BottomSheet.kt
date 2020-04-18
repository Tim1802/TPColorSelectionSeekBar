package de.tpohrer.tpcolorselectionseekbarexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.tpohrer.tpcolorselectionseekbar.TPColorSelectionSeekBar

/**
 * Created by tpohrer on 2020-03-18.
 */
 
class BottomSheet : BottomSheetDialogFragment() {

    private var colorSelectionSeekBar: TPColorSelectionSeekBar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view =  inflater.inflate(R.layout.sheet_main, container)
        colorSelectionSeekBar = view.findViewById(R.id.sheetBar)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        colorSelectionSeekBar?.cleanUp()
        colorSelectionSeekBar = null
    }
}