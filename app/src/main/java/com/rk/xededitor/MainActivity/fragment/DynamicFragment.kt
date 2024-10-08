package com.rk.xededitor.MainActivity.fragment

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.rk.xededitor.BaseActivity
import com.rk.xededitor.MainActivity.MainActivity
import com.rk.xededitor.MainActivity.StaticData
import com.rk.xededitor.R
import com.rk.xededitor.Settings.SettingsData
import com.rk.xededitor.rkUtils
import com.rk.xededitor.rkUtils.runOnUiThread
import com.rk.xededitor.setupEditor
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentIO
import io.github.rosemoe.sora.widget.CodeEditor
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class DynamicFragment : Fragment {
  lateinit var fileName: String
  var file: File? = null
  private var ctx: Context? = null
  lateinit var editor: CodeEditor
  private var editorx: CodeEditor? = null
  var content: Content? = null
  var isModified: Boolean = false
  var isSearching = false


  constructor() {
      com.rk.libcommons.After(100) {
          runOnUiThread {
              val fragmentManager =
                  BaseActivity.getActivity(MainActivity::class.java)?.supportFragmentManager
              val fragmentTransaction = fragmentManager?.beginTransaction()
              fragmentTransaction?.remove(this)
              fragmentTransaction?.commitNowAllowingStateLoss()
          }
      }
  }

  constructor(file: File, ctx: Context) {


    this.fileName = file.name
    this.ctx = ctx
    this.file = file
    editor = CodeEditor(ctx)
    editorx = editor

    setupEditor(editor, ctx).setupLanguage(fileName)
    editor.isCursorAnimationEnabled = SettingsData.getBoolean(ctx,"CursorAnimation",true)
    val tabSize = SettingsData.getSetting(ctx,"tabsize","4").toInt()

    editor.props.deleteMultiSpaces = tabSize
    editor.props.deleteEmptyLineFast = false
   // editor.props.autoIndent = SettingsData.getBoolean(ctx,"autoIndent",true)
    editor.props.useICULibToSelectWords = true
    editor.tabWidth = tabSize

    editor.setPinLineNumber(SettingsData.getBoolean(ctx,"pinline",false))

    if (SettingsData.isDarkMode(ctx)) {
      setupEditor(editor, ctx).ensureTextmateTheme()
    } else {
      Thread { setupEditor(editor, ctx).ensureTextmateTheme() }.start()
    }

    

    val wordwrap = SettingsData.getBoolean(ctx, "wordwrap", false)



    Thread {
      try {
        val inputStream: InputStream = FileInputStream(file)
        content = ContentIO.createFrom(inputStream)
        inputStream.close()
        runOnUiThread { editor.setText(content) }
        if (wordwrap) {
          val length = content.toString().length
          if (length > 700 && content.toString().split("\\R".toRegex())
              .dropLastWhile { it.isEmpty() }.toTypedArray().size < 100
          ) {
            runOnUiThread {
              rkUtils.toast(
                ctx, resources.getString(R.string.ww_wait)
              )
            }
          }
          if (length > 1500) {
            runOnUiThread {
              Toast.makeText(
                ctx, resources.getString(R.string.ww_wait), Toast.LENGTH_LONG
              ).show()
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }.start()


    editor.typefaceText = Typeface.createFromAsset(ctx.assets, "JetBrainsMono-Regular.ttf")
    editor.setTextSize(SettingsData.getSetting(ctx, "textsize", "14").toFloat())
    editor.isWordwrap = wordwrap




    setListener()
  }

  private fun setListener() {
    editor.subscribeAlways(
      ContentChangeEvent::class.java
    ) {
      updateUndoRedo()
      val tab = StaticData.mTabLayout.getTabAt(StaticData.mTabLayout.selectedTabPosition)
      if (isModified) {
        tab!!.setText("$fileName*")
      }
      isModified = true
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    return editorx
  }

  fun updateUndoRedo() {
      if (StaticData.menu == null && !isSearching){
        return
      }
      StaticData.menu.findItem(R.id.redo)?.setEnabled(editor.canRedo())
      StaticData.menu.findItem(R.id.undo)?.setEnabled(editor.canUndo())
  }

  fun releaseEditor() {
    editor.release()
    content = null
  }

  fun Undo() {
    if (editor.canUndo()) {
      editor.undo()
    }
  }

  fun Redo() {
    if (editor.canRedo()) {
      editor.redo()
    }
  }


}