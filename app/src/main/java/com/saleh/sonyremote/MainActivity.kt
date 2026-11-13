package com.saleh.sonyremote

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.hardware.ConsumerIrManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.text.InputType
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(17, 23, 25)
    private val panel = Color.rgb(27, 37, 39)
    private val teal = Color.rgb(52, 205, 185)
    private val pale = Color.rgb(231, 243, 241)
    private val muted = Color.rgb(157, 180, 177)
    private val purple = Color.rgb(196, 146, 255)
    private val prefs by lazy { getSharedPreferences("verified_codes", Context.MODE_PRIVATE) }
    private var shiftOn = false
    private lateinit var status: TextView
    private lateinit var ir: ConsumerIrManager

    private data class Key(val id: String, val title: String, val sub: String = "", val style: Int = 0, val uiOnly: Boolean = false)
    private fun k(id: String, title: String, sub: String = "", style: Int = 0) = Key(id, title, sub, style)
    private val sections = listOf(
        "تلویزیون — کنترل جداگانه" to listOf(k("tv_power","TV POWER","روشن/خاموش",2), k("tv_input","TV INPUT","ورودی تلویزیون",2)),
        "دستگاه DAV-DZ950 / HBD-DZ950" to listOf(k("system_power","SYSTEM POWER","روشن/خاموش",2), k("one_touch_play","ONE-TOUCH PLAY","پخش با یک لمس"), k("dvd_cd","DVD/CD"), k("bluetooth","BLUETOOTH"), k("usb","USB"), k("fm","FM"), k("tv_hdmi","TV (HDMI)"), k("optical_audio_in","OPTICAL/AUDIO IN"), k("eq","EQ"), k("surround","SURROUND"), k("audio","AUDIO"), k("karaoke","KARAOKE"), k("mic_vol_plus","MIC VOL +"), k("mic_vol_minus","MIC VOL −")),
        "منو و تنظیمات" to listOf(k("dvd_top_menu","DVD TOP MENU"), k("dvd_menu","DVD MENU"), k("program","PROGRAM"), k("repeat","REPEAT"), k("sleep","SLEEP"), k("time","TIME"), k("dimmer","DIMMER"), k("setup","SETUP"), k("display","DISPLAY"), k("muting","MUTING")),
        "عددها" to (1..9).map { k("digit_$it", it.toString()) } + listOf(k("digit_0","0")),
        "جهت‌یابی و صدا" to listOf(k("nav_up","▲","بالا",1), k("nav_left","◀","چپ",1), k("nav_enter","ENTER","تأیید",1), k("nav_right","▶","راست",1), k("nav_down","▼","پایین",1), k("volume_plus","VOLUME +","صدای دستگاه"), k("volume_minus","VOLUME −","صدای دستگاه")),
        "رادیو" to listOf(k("preset_plus","PRESET +"), k("preset_minus","PRESET −"), k("tuning_plus","TUNING +"), k("tuning_minus","TUNING −")),
        "پخش DVD" to listOf(k("dvd_previous","PREV / PREVIOUS"), k("dvd_next","NEXT"), k("dvd_rewind","REV / REWIND"), k("dvd_forward","FWD / FORWARD"), k("dvd_slow","SLOW"), k("dvd_play","PLAY", "▶",1), k("dvd_pause","PAUSE", "Ⅱ",1), k("dvd_stop","STOP", "■",1)),
        "کنترل متنی بنفش" to listOf(Key("shift_ui","SHIFT","فقط تغییر وضعیت رابط؛ کد IR ندارد",3,true))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ir = getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg); layoutDirection = View.LAYOUT_DIRECTION_RTL }
        val scroll = ScrollView(this).apply { isFillViewport = true; overScrollMode = View.OVER_SCROLL_NEVER }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18),dp(14),dp(18),dp(28)) }
        val title = text("ریموت Sony DAV-DZ950", 24f, pale, true).apply { gravity = Gravity.RIGHT }
        content.addView(title)
        content.addView(text("HBD-DZ950  •  RM-ADU162  •  گیرندهٔ مادون‌قرمز", 13f, muted).apply { gravity = Gravity.RIGHT; setPadding(0,dp(5),0,dp(12)) })
        val warning = text("هیچ کد IR از پیش حدس زده نشده است. هر دکمه تا زمان ثبت کد تأییدشده غیرفعال می‌ماند.", 14f, pale)
        warning.setPadding(dp(14),dp(12),dp(14),dp(12)); warning.background = shape(panel, 14)
        content.addView(warning)
        status = text("وضعیت: بررسی فرستندهٔ IR…", 13f, teal).apply { gravity = Gravity.RIGHT; setPadding(0,dp(10),0,dp(10)) }
        content.addView(status)
        sections.forEach { (heading, keys) ->
            val header = text(heading, 16f, teal, true).apply { gravity = Gravity.RIGHT; setPadding(0,dp(17),0,dp(9)) }
            content.addView(header)
            val grid = GridLayout(this).apply { columnCount = 3; layoutDirection = View.LAYOUT_DIRECTION_RTL }
            keys.forEach { key ->
                val button = Button(this).apply {
                    text = key.title + if (key.sub.isNotEmpty() && key.id != "shift_ui") "\n${key.sub}" else ""
                    textSize = if (key.title.length > 12) 11f else 13f
                    setTextColor(if (key.style == 3) purple else pale)
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    isAllCaps = false
                    gravity = Gravity.CENTER
                    minHeight = dp(58); minimumHeight = dp(58)
                    setPadding(dp(4),dp(4),dp(4),dp(4))
                    background = shape(if (key.style == 2) Color.rgb(75,55,54) else if(key.style==1) Color.rgb(32,67,66) else panel, 13)
                    contentDescription = key.title + if(key.sub.isNotEmpty()) ", ${key.sub}" else ""
                }
                val lp = GridLayout.LayoutParams().apply { width=0; height=dp(64); columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); setMargins(dp(4),dp(4),dp(4),dp(4)) }
                grid.addView(button, lp)
                refreshButton(button,key)
                button.setOnClickListener {
                    if (key.uiOnly) { shiftOn = !shiftOn; button.text = if(shiftOn) "SHIFT ✓\nوضعیت رابط روشن" else "SHIFT\nفقط وضعیت رابط"; return@setOnClickListener }
                    val code = prefs.getString(key.id,null)
                    if (code.isNullOrBlank()) Toast.makeText(this,"برای ${key.title} کد تأییدشده ثبت نشده است؛ لمس طولانی برای ویرایش.",Toast.LENGTH_SHORT).show()
                    else transmit(key,code)
                }
                button.setOnLongClickListener { if (!key.uiOnly) editCode(key); true }
            }
            content.addView(grid)
        }
        content.addView(text("راهنما: روی دکمهٔ دستگاه لمس طولانی کنید تا تعریف تأییدشده را وارد/حذف کنید. SHIFT فقط نشانگر رابط است و چیزی ارسال نمی‌کند.", 13f, muted).apply { gravity=Gravity.RIGHT; setPadding(0,dp(18),0,dp(4)) })
        scroll.addView(content); root.addView(scroll); setContentView(root)
        updateStatus()
    }

    private fun refreshButton(button: Button, key: Key) {
        if (key.uiOnly) return
        val configured = !prefs.getString(key.id,null).isNullOrBlank()
        button.alpha = if(configured) 1f else 0.62f
        button.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
        button.contentDescription = "${key.title}; ${if(configured) "کد تنظیم شده" else "بدون کد؛ لمس طولانی برای تنظیم"}"
    }

    private fun editCode(key: Key) {
        val input = EditText(this).apply {
            setText(prefs.getString(key.id,"")); hint = "SIRC:12:device:command"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            textSize=14f; setPadding(dp(14),dp(12),dp(14),dp(12)); layoutDirection=View.LAYOUT_DIRECTION_LTR
        }
        val wrapper = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(20),dp(8),dp(20),0) }
        wrapper.addView(text("کد فقط باید از منبعی که خودتان راستی‌آزمایی کرده‌اید وارد شود. شماره‌ها ده‌دهی هستند. خالی گذاشتن و ذخیره، کد را حذف می‌کند.",13f,muted))
        wrapper.addView(input)
        AlertDialog.Builder(this).setTitle("تعریف IR برای ${key.title}").setView(wrapper)
            .setPositiveButton("اعتبارسنجی و ذخیره") { _,_ ->
                val value=input.text.toString().trim()
                if(value.isEmpty()) { prefs.edit().remove(key.id).apply(); Toast.makeText(this,"کد حذف شد",Toast.LENGTH_SHORT).show() }
                else try { parseDefinition(value); prefs.edit().putString(key.id,value).apply(); Toast.makeText(this,"تعریف معتبر ذخیره شد؛ ارسال هنوز به سخت‌افزار IR نیاز دارد.",Toast.LENGTH_LONG).show() }
                catch(e: IllegalArgumentException) { Toast.makeText(this,e.message ?: "تعریف نامعتبر",Toast.LENGTH_LONG).show() }
                buildUi()
            }.setNegativeButton("انصراف",null).show()
    }

    private data class Signal(val frequency:Int, val pattern:IntArray)
    private fun parseDefinition(raw:String): Signal {
        val p=raw.split(':')
        if(p[0].equals("RAW",true)) {
            require(p.size==3) { "قالب RAW: RAW:frequency:on,off,..." }
            val freq=p[1].toIntOrNull() ?: throw IllegalArgumentException("فرکانس باید عدد صحیح باشد.")
            require(freq in 1..1_000_000) { "فرکانس نامعتبر است." }
            val tokens=p[2].split(',')
            require(tokens.size>=2 && tokens.size%2==0) { "الگوی RAW باید تعداد زوج مدت‌های on,off داشته باشد." }
            val values=tokens.map { it.toIntOrNull() ?: throw IllegalArgumentException("مدت‌های RAW باید عدد صحیح میکروثانیه باشند.") }
            require(values.all { it>0 }) { "هر مدت on/off باید مثبت باشد." }
            require(values.sum().toLong()<2_000_000L) { "مدت RAW باید کمتر از ۲ ثانیه باشد." }
            val ranges=ir.carrierFrequencies
            if(ranges!=null && ranges.isNotEmpty()) require(ranges.any { freq>=it.minFrequency && freq<=it.maxFrequency }) { "فرکانس $freq Hz در بازهٔ پشتیبانی گوشی نیست." }
            return Signal(freq,values.toIntArray())
        }
        require(p[0].equals("SIRC",true)) { "قالب پشتیبانی‌شده: SIRC:12/15/20:device:command[:extended] یا RAW." }
        require(p.size==4 || p.size==5) { "قالب SIRC نامعتبر است." }
        val bits=p[1].toIntOrNull() ?: throw IllegalArgumentException("نوع SIRC باید 12، 15 یا 20 باشد.")
        require(bits==12 || bits==15 || bits==20) { "فقط SIRC 12، 15 و 20 پشتیبانی می‌شوند." }
        require((bits==20 && p.size==5) || (bits!=20 && p.size==4)) { "SIRC20 نیازمند extended است؛ SIRC12/15 نباید فیلد اضافه داشته باشند." }
        val device=p[2].toIntOrNull() ?: throw IllegalArgumentException("device باید عدد ده‌دهی باشد.")
        val command=p[3].toIntOrNull() ?: throw IllegalArgumentException("command باید عدد ده‌دهی باشد.")
        require(device in 0..if(bits==15) 255 else 31) { "محدودهٔ device برای این SIRC نامعتبر است." }
        require(command in 0..127) { "command باید ۷ بیتی (۰ تا ۱۲۷) باشد." }
        val extended=if(bits==20) p[4].toIntOrNull() ?: throw IllegalArgumentException("extended باید عدد ده‌دهی باشد.") else 0
        require(bits!=20 || extended in 0..255) { "extended باید ۸ بیتی (۰ تا ۲۵۵) باشد." }
        val pattern=ArrayList<Int>(2+bits*2+(3*2))
        repeat(3) { frame ->
            pattern.add(2400); pattern.add(600)
            fun append(value:Int, n:Int) { for(i in 0 until n) { pattern.add(if(((value shr i) and 1)==0) 600 else 1200); pattern.add(600) } }
            append(command,7); append(device,if(bits==15) 8 else 5); if(bits==20) append(extended,8)
            if(frame<2) {
                val used=pattern.takeLast(2+bits*2).sum()
                val gap=45_000-used
                require(gap>0) { "طول فریم SIRC نامعتبر است." }
                pattern.add(gap)
            }
        }
        require(pattern.sum().toLong()<2_000_000L) { "مدت ارسال باید کمتر از ۲ ثانیه باشد." }
        return Signal(40_000,pattern.toIntArray())
    }

    private fun transmit(key:Key,code:String) {
        if(!ir.hasIrEmitter()) { Toast.makeText(this,"این دستگاه فرستندهٔ IR ندارد.",Toast.LENGTH_LONG).show(); return }
        try {
            val sig=parseDefinition(code)
            if(android.os.Build.VERSION.SDK_INT>=24 && !ir.hasCarrierFrequency(sig.frequency)) { Toast.makeText(this,"فرکانس پشتیبانی نمی‌شود.",Toast.LENGTH_LONG).show(); return }
            ir.transmit(sig.frequency,sig.pattern)
            Toast.makeText(this,"ارسال ${key.title}",Toast.LENGTH_SHORT).show()
        } catch(e: Exception) { Toast.makeText(this,e.message ?: "ارسال ناموفق",Toast.LENGTH_LONG).show() }
    }

    private fun updateStatus() {
        val capable=ir.hasIrEmitter()
        status.text=if(capable) "وضعیت: فرستندهٔ IR در گوشی موجود است؛ کدها فقط پس از افزودن کدهای تأییدشده ارسال می‌شوند." else "وضعیت: فرستندهٔ IR در این گوشی شناسایی نشد. این برنامه به IR blaster نیاز دارد."
    }
    private fun text(s:String,size:Float,color:Int,bold:Boolean=false)=TextView(this).apply { text=s; textSize=size; setTextColor(color); if(bold) typeface=Typeface.create("sans-serif",Typeface.BOLD); setLineSpacing(dp(2).toFloat(),1f) }
    private fun shape(color:Int,radius:Int)=GradientDrawable().apply { setColor(color); cornerRadius=dp(radius).toFloat(); setStroke(dp(1),Color.rgb(45,66,66)) }
    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
}
