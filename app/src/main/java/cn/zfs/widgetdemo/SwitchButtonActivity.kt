package cn.zfs.widgetdemo

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.snail.widget.textview.SwitchButton
import kotlinx.android.synthetic.main.activity_switch_button.*

/**
 * 开关
 *
 * date: 2019/1/8 23:14
 * author: zengfansheng
 */
class SwitchButtonActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_switch_button)
        sb1.backColor = SwitchButton.generateBackColorWithTintColor(ContextCompat.getColor(this, R.color.colorPrimary))
    }
}