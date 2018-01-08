package anko

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import org.jetbrains.anko.*

import com.linyuzai.demo4mediataker.R

/**
 * Generate with Plugin
 * @plugin Kotlin Anko Converter For Xml
 * @version 1.2.1
 */
class MainActivity : Activity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		linearLayout {
			orientation = LinearLayout.VERTICAL
			//tools:context = com.linyuzai.demo4mediataker.MainActivity //not support attribute
			imageView {
				id = R.id.image
				scaleType = ImageView.ScaleType.CENTER_INSIDE
			}.lparams(width = matchParent, height = dip(100))
			videoView {
				id = R.id.video
			}.lparams(width = matchParent, height = dip(100))
			button {
				id = R.id.take_photo
				text = "take photo"
			}.lparams(width = matchParent)
			button {
				id = R.id.select_picture
				text = "select picture"
			}.lparams(width = matchParent)
			button {
				id = R.id.select_video
				text = "select video"
			}.lparams(width = matchParent)
			button {
				id = R.id.record_video
				text = "record video"
			}.lparams(width = matchParent)
		}
	}
}
