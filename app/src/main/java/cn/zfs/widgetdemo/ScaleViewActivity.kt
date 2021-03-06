package cn.zfs.widgetdemo

import android.os.Bundle
import cn.wandersnail.commons.util.UiUtils
import kotlinx.android.synthetic.main.activity_scale_view.*
import java.text.DecimalFormat

/**
 *
 *
 * date: 2019/4/6 08:59
 * author: zengfansheng
 */
class ScaleViewActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scale_view)
        val params = scaleView.obtainParams()
        params.setScope(0, 1700)
        params.setLabelFormatter { value -> DecimalFormat("0.00").format(value) }
        params.setOnValueUpdateCallback { value -> tv.text = value.toString() }
        params.apply()
        scaleView.value = 800f
        
        val params1 = scaleView1.obtainParams()
        params1.setScope(0, 100)
        params1.setShortLongScaleRatio(1f)
        params1.setScaleSpace(UiUtils.dp2px(30f))
        params1.setLabelFormatter { value -> value.toInt().toString() }
        params1.setOnValueUpdateCallback { value -> tv.text = value.toString() }
        params1.apply()
        scaleView1.value = 50f
    }
}