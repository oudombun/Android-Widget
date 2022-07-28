package com.oudombun.widgettesting

import android.app.ActionBar
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.oudombun.widgettesting.databinding.QrWidgetConfigureBinding
import com.oudombun.widgettesting.event.RxBus
import com.oudombun.widgettesting.event.RxEvent
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*


/**
 * The configuration screen for the [QrWidget] AppWidget.
 */
class QrWidgetConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var binding: QrWidgetConfigureBinding

    var sessionDisposable: Disposable? = null

    private val context = this@QrWidgetConfigureActivity

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        // Set the result to CANCELED.  This will cause the widget host to cancel // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        binding = QrWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        if(MainActivity.isLogin){
            val modalBottomSheet = BottomSheetDialog()
            modalBottomSheet.show(supportFragmentManager, BottomSheetDialog.TAG)

            modalBottomSheet.setOnChooseOutletListener {
                if(it==null){
                    finish()
                    return@setOnChooseOutletListener
                }else{
                    val widgetText = it.toString()
                    saveTitlePref(context, appWidgetId, widgetText)
                    // It is the responsibility of the configuration activity to update the app widget
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    updateAppWidget(context, appWidgetManager, appWidgetId)

                    // Make sure we pass back the original appWidgetId
                    val resultValue = Intent()
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(RESULT_OK, resultValue)
                    finish()
                }
            }
        }else{
            val appWidgetManager = AppWidgetManager.getInstance(context)
            removeAppWidget(context, appWidgetManager, appWidgetId)
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }


    }

    override fun onResume() {
        super.onResume()
        sessionDisposable = RxBus.listen(RxEvent.EventLogout::class.java).subscribe {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            removeAppWidget(context, appWidgetManager, appWidgetId)
            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }

        sessionDisposable = RxBus.listen(RxEvent.EventLogin::class.java).subscribe {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateAppWidget(context, appWidgetManager, appWidgetId)
            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}

class BottomSheetDialog :BottomSheetDialogFragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_outlet, container, false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME,0)

    }

    private var onChooseOutlet: ((result: String?) -> Unit)? = null

    fun setOnChooseOutletListener(chooseListener: ((result: String?) -> Unit)) = apply {
        this.onChooseOutlet = chooseListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)


        val group = view.findViewById<RadioGroup>(R.id.rg_sort)
        val rprms: RadioGroup.LayoutParams =
            RadioGroup.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)

        val list = getOutlet(requireContext())
        list.forEach {
            group.addView(it, rprms)
        }

        group.setOnCheckedChangeListener { _, checkedId ->
            val radioButton =view.findViewById<RadioButton>(checkedId)
            onChooseOutlet?.invoke(radioButton.text.toString())
        }
    }

    override fun onDetach() {
        onChooseOutlet?.invoke(null)
        super.onDetach()
    }
    companion object {
        const val TAG = "ModalBottomSheet"
    }
}

private const val PREFS_NAME = "com.oudombun.widgettesting.QrWidget"
private const val PREF_PREFIX_KEY = "appwidget_"
private const val QR_VALUE = "https://play.google.com/store/apps/details?id=com.canadiabank.mobileapp.android"

internal fun loadImageBitMap(): Bitmap {
    val writer = QRCodeWriter()

    val hints: MutableMap<EncodeHintType, Any> = EnumMap(
        EncodeHintType::class.java
    )
    hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
    hints[EncodeHintType.MARGIN] = 1
    val matrix = writer.encode(QR_VALUE, BarcodeFormat.QR_CODE,165,165,hints)
    val width= matrix.width
    val height= matrix.height
    val bmp = Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565)
    for (x in 0 until width){
        for (y in 0 until height){
            bmp.setPixel(x,y,if(matrix[x,y]) Color.BLACK else Color.WHITE)
        }
    }
    return bmp
}

// Write the prefix to the SharedPreferences object for this widget
internal fun saveTitlePref(context: Context, appWidgetId: Int, text: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
    prefs.apply()
}

// Read the prefix from the SharedPreferences object for this widget.
// If there is no preference saved, get the default from a resource
internal fun loadTitlePref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
    return titleValue ?: context.getString(R.string.appwidget_text)
}

internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}

internal fun getOutlet(context: Context): MutableList<RadioButton> {
    val radioList = mutableListOf<RadioButton>()
    for (i in 0..2) {
        val radioButton = RadioButton(context)
        radioButton.text = "Outlet ${i+1}"
        radioButton.id = View.generateViewId()
        radioButton.setTextColor(ContextCompat.getColorStateList(context, R.color.black));
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_enabled)
            ), intArrayOf(
                Color.BLACK,  // disabled
                Color.BLACK, // enabled,
            )
        )
        radioButton.buttonTintList = colorStateList // set the color tint list
        radioButton.invalidate() // Could not be necessary
        radioList.add(radioButton)

    }
    return radioList
}
