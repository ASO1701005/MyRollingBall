package jp.ac.asojuku.st.myrollingball

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
        ,SensorEventListener,SurfaceHolder.Callback {

    //プロパティ
    private var surfaceWidth:Int = 0; //幅
    private var surfaceHeight:Int = 0; //高さ

    private val radius = 50.0f;
    private val coef = 1000.0f;

    private var ballX:Float = 0f;
    private var ballY:Float = 0f;
    private var vx:Float = 0f;
    private var vy:Float = 0f;
    private var time:Long = 0L;

    //サイボウズ
    private val barrierWidth = 200f;
    private val barrierHeight = 50f;
    private val barrierLeft:Array<Float> = arrayOf(200f,300f,400f);
    private val barrierTop:Array<Float> = arrayOf(200f,500f,900f);
    private val barrierPos = Array(3) { FloatArray(4) };

    private val LEFT = 0;
    private val TOP = 1;
    private val RIGHT = 2;
    private val BOTTOM = 3;

    private var gamingFlg:Boolean = true;

    val imageArray:Array<Int> = arrayOf(
            R.drawable.face,
            R.drawable.s_71101397
    );

    private fun barrierCheck(){
        for(n in this.barrierPos.indices){
            if(this.barrierPos[n][LEFT] < (ballX+radius) && (ballX-radius) < this.barrierPos[n][RIGHT]){
                if(this.barrierPos[n][TOP] < (ballY+radius) && (ballY-radius) < this.barrierPos[n][BOTTOM]){
                    this.gamingFlg=false;
                    //this.fault();
                    return
                }
            }
        }
    }


    //誕生時
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val holder = surfaceView.holder; //サーフェスホルダーを取得
        holder.addCallback(this);//コールバック
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        btn_reset.setOnClickListener{ onResetButtonTapped() };

    }

    //画面表示・歳表示のライフサイクルイベント
    override fun onResume() {
        //親クラスのonResume()処理
        super.onResume()
    }

    //画面が非表示の時のライフサイクルイベント
    override fun onPause() {
        super.onPause()
    }

    //精度が変わった時のイベントコールバック
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
    //センサーの値が変わった時のイベントコールバック
    override fun onSensorChanged(event: SensorEvent?) {

        if(event == null){ return; }
        if(time==0L){ time = System.currentTimeMillis(); }
        //イベントの情報がアクセラメーター
        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
            val x = event.values[0]*-1;
            val y = event.values[1];

            //
            var t = (System.currentTimeMillis() - time).toFloat();
            time = System.currentTimeMillis();
            t /= 1000.0f;

            //
            val dx = (vx*t) + (x*t*t)/2.0f;
            val dy = (vy*t) + (x*t*t)/2.0f;
            ballX += (dx*coef)
            ballY += (dy*coef)
            vx +=(x*t);
            vy +=(y*t);

            if(ballX -radius < 0 && vx<0){
                vx = -vx/1.5f;
                ballX = radius;
            }
            else if( ballX +radius>surfaceWidth && vx>0){
                //ぶつかった時
                vx = -vx/1.5f;
                ballX = (surfaceWidth-radius);
            }
            if( (ballY-radius)<0 && vy<0) {
                vy = -vy/1.5f;
                ballY = radius;
            }
            else if(ballY +radius>surfaceHeight && vy>0){
                //ぶつかった時
                vy = -vy/1.5f;
                ballY = (surfaceHeight-radius);
            }

            //キャンパスに描画
            if(this.gamingFlg){
                //this.wallCheck();
                //this.goalCheck();
                if(ballX < 800f+radius && ballY < 500f+radius && ballY > 400f-radius){
                    vx= vx * 0f;
                    vy= vy * 0f;
                    this.gamingFlg = false;
                    txv_fight.setText("がんばれ");
                }
                if(ballX > 300f-radius && ballY < 900f+radius && ballY > 800f-radius){
                    vx= vx * 0f;
                    vy= vy * 0f;
                    this.gamingFlg = false;
                    txv_fight.setText("がんばれ");
                }
                if(ballX < 100f+radius && ballY < 100f+radius){
                    vx= vx * 0f;
                    vy= vy * 0f;
                    this.gamingFlg = false;
                    txv_fight.setText("おめでとう");
                    imageView.setImageResource(imageArray[0]);
                }
                if(ballX < 300f+radius && ballX > 200f-radius && ballY < 100f+radius){
                    vx= vx * 0f;
                    vy= vy * 0f;
                    this.gamingFlg = false;
                    txv_fight.setText("がんばれ");
                }
                this.drawCanvas();
            }

        }
    }
    //サーフェスが更新された時のイベント
    override fun surfaceChanged(p0: SurfaceHolder?, format: Int,
                                width: Int, height: Int) {
        surfaceWidth = width;
        surfaceHeight = height;
        //ボールの初期値を保存
        ballX = (width/2).toFloat();
        ballY = (height/2).toFloat();
        ballX = 955f;
        ballY = 1200f;


    }
    //サーフェスが破棄された時のイベント
    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        //加速度センサーの登録を解除する流れ
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE)
                as SensorManager;
        //
        sensorManager.unregisterListener(this);
    }
    //サーフェスが作成された時のイベント
    override fun surfaceCreated(holder: SurfaceHolder?) {
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE)
                as SensorManager;
        val accSensor =
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //
        sensorManager.registerListener(
                this,
                accSensor,
                SensorManager.SENSOR_DELAY_GAME
        )
    }
    private fun drawCanvas() {
        //キャンバスをロックして取得
        val canvas = surfaceView.holder.lockCanvas();
        canvas.drawColor(Color.BLACK);
        //キャンバスに円を描いてボールにする
        canvas.drawCircle(ballX,ballY,radius,
                Paint().apply{
                    color = Color.RED;
                });
        canvas.drawRect(0f,400f,800f,500f,
                Paint().apply{
                    color = Color.BLUE;
                });
        canvas.drawRect(300f,800f,1200f,900f,
                Paint().apply{
                    color = Color.BLUE;
                });
        canvas.drawRect(0f,0f,100f,100f,
                Paint().apply{
                    color = Color.GREEN;
                });
        canvas.drawRect(200f,0f,300f,100f,
                Paint().apply{
                    color = Color.YELLOW;
                });

        surfaceView.holder.unlockCanvasAndPost(canvas);
    }

    fun onResetButtonTapped(){
        this.gamingFlg = true;
        ballX = (surfaceWidth/2).toFloat();
        ballY = (surfaceHeight/2).toFloat();
        ballX = 955f;
        ballY = 1200f;
        vx = 0f;
        vy = 0f;
        time = 0L;
        txv_fight.setText("がんばれ");
        imageView.setImageResource(imageArray[1]);
    }
}
