package io.agora.rtmdemo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.LinkStateEvent;
import io.agora.rtm.MessageEvent;
import io.agora.rtm.PresenceEvent;
import io.agora.rtm.PublishOptions;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmConfig;
import io.agora.rtm.RtmConstants;
import io.agora.rtm.RtmEventListener;
import io.agora.rtm.SubscribeOptions;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class MainActivity extends Activity {
    @BindView(R.id.appid)
    EditText mAppId;
    @BindView(R.id.token)
    EditText mToken;
    @BindView(R.id.uid)
    EditText mUserId;
    @BindView(R.id.channel_name)
    EditText mChannelName;
    @BindView(R.id.message)
    EditText mMessage;
    @BindView(R.id.log_scroll_view)
    ScrollView mLogScrollView;
    @BindView(R.id.log_text_view)
    TextView mLogTextView;



    // RTM 客户端实例
    private RtmClient mRtmClient;
    private Handler uiHandler;

    //定义一个静态 PublishOptions 对象
    private static PublishOptions options2 = new PublishOptions(RtmConstants.RtmChannelType.USER, "xxx");

    //定义一个静态int变量
    private static byte[] tmp = new byte[10];



    private String csvFile = "/storage/sdcard0/Android/data/io.agora.rtmdemo/files/delay.csv"; // CSV 文件路径
    private BufferedWriter writer = null;



    private RtmEventListener eventListener = new RtmEventListener() {
        @Override
        public void onMessageEvent(MessageEvent event) {



//            //收到消息后，立即发送回执
//            byte[] message = (byte[]) event.getMessage().getData();
//
//            mRtmClient.publish("tony", message, options2, new ResultCallback<Void>() {
//                @Override
//                public void onSuccess(Void responseInfo) {
//                    //log打印inc的值
//                    Log.e("yust-receive", String.format("send message %d success!", inc++) );
//                }
//
//                @Override
//                public void onFailure(ErrorInfo errorInfo) {
//                    printLog("send message failed! " + errorInfo.toString());
//                }
//            });


            byte[] data = (byte[]) event.getMessage().getData();

            int seq1 = 0;



            // 获取当前时间戳，毫秒级
            long nowtime = System.currentTimeMillis();
            long oldtime = 0;
            long difftime =0;

            // 解析sequence
            tmp[0] = data[0];
            tmp[1] = data[1];
            seq1 = ((tmp[0] & 0xFF) << 8) + (tmp[1] & 0xFF);

            // 解析oldtime
            tmp[2] = data[2];
            tmp[3] = data[3];
            tmp[4] = data[4];
            tmp[5] = data[5];
            tmp[6] = data[6];
            tmp[7] = data[7];
            tmp[8] = data[8];
            tmp[9] = data[9];

            oldtime = ((long) (tmp[2] & 0xFF) << 56)
                    + ((long) (tmp[3] & 0xFF) << 48)
                    + ((long) (tmp[4] & 0xFF) << 40)
                    + ((long) (tmp[5] & 0xFF) << 32)
                    + ((long) (tmp[6] & 0xFF) << 24)
                    + ((long) (tmp[7] & 0xFF) << 16)
                    + ((long) (tmp[8] & 0xFF) << 8)
                    + (tmp[9] & 0xFF);

            // 计算时间差
            difftime = (nowtime - oldtime)/2;

            // 打印信息
           String str = String.format("%d, %d, %d, %d\n", seq1, oldtime, nowtime, difftime);
            //将打印写入到一个 csv文件中
            try {
                writer.write(str);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }



        }

        @Override
        public void onPresenceEvent(PresenceEvent event) {
            printLog(event.toString());
        }

        @Override
        public void onLinkStateEvent(LinkStateEvent event) {
            printLog(event.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);
        this.uiHandler = new Handler(Looper.getMainLooper());

        Random random = new Random();
        //this.mUserId.setText("user_" + String.format("%04d", random.nextInt(10000)));
        this.mUserId.setText("tony"); //selfid
        this.mChannelName.setText("user_5126");//peerid
        this.mMessage.setText("hello rtm2");
        String appid = getApplicationContext().getString(R.string.agora_app_id);
        if (!appid.contains("#")) {
            this.mAppId.setText(appid);
            this.mToken.setText(appid);
        }




    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 登录按钮
    @OnClick(R.id.login_button)
    void onLoginBtnClicked()
    {
        try {
            String userId = mUserId.getText().toString();
            if (userId == null || userId.isEmpty()) {
                showToast("invalid userId");
                return;
            }
            RtmConfig config = new RtmConfig.Builder(mAppId.getText().toString(), userId)
                    .eventListener(eventListener)
                    .build();
            mRtmClient = RtmClient.create(config);
        } catch (Exception e) {
            showToast("create rtm client is null");
        }

        if (mRtmClient == null) {
            showToast("rtm client is null");
            return;
        }

        // 登录 RTM 系统
        mRtmClient.login(mToken.getText().toString(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                printLog("login success!");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                CharSequence text = "login failed! " + errorInfo.toString();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });


        //open the file
        try {
            // 使用FileWriter打开文件，第二个参数为true，表示追加模式
            writer = new BufferedWriter(new FileWriter(csvFile, true));
            Log.e("yust", "open file success");

        } catch (IOException e) {
            e.printStackTrace();
        }


        }

    // 加入频道按钮
    @OnClick(R.id.join_button)
    void onJoinBtnClicked()
    {
        if (mRtmClient == null) {
            showToast("rtm client is null");
            return;
        }

        String channelName = mChannelName.getText().toString();
        SubscribeOptions options = new SubscribeOptions();
        mRtmClient.subscribe(channelName, options, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                printLog("join channel success!");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                printLog("join channel failed! " + errorInfo.toString());
            }
        });
    }

    // 登出按钮
    @OnClick(R.id.logout_button)
    void onLogoutBtnClicked()
    {
        if (mRtmClient == null) {
            showToast("rtm client is null");
        }
        // 登出 RTM 系统
        mRtmClient.logout(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                printLog("logout success!");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                printLog("logout failed! " + errorInfo.toString());
            }
        });
        stopTimer();

        // 关闭文件
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }



    }

    // 离开频道按钮
    @OnClick(R.id.leave_button)
    void onLeaveBtnClicked()
    {
        if (mRtmClient == null) {
            showToast("rtm client is null");
            return;
        }
        String channelName = mChannelName.getText().toString();
        // 离开 RTM 频道
        mRtmClient.unsubscribe(channelName, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                printLog("leave channel success!");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                printLog("leave channel failed! " + errorInfo.toString());
            }
        });
    }

    private Handler handler2 = new Handler();
    private Runnable runnable2;
    private final int DELAY = 20; // 20ms


    private static int seq = 0;


    // 定时器启动方法
    private void startTimer() {
        runnable2 = new Runnable() {
            @Override
            public void run() {
                send(); // 调用send()函数

                // 再次调用自己实现20ms的定时
                handler2.postDelayed(this, DELAY);
            }
        };

        // 启动定时器
        handler2.post(runnable2);
    }

    // 创建11字节的数组
    byte[] tmp1 = new byte[11];

    // 示例的send函数
    private void send() {





                seq++;
                long now = System.currentTimeMillis(); // 获取当前时间戳，毫秒级



                // 将seq放在前两个字节
                tmp1[0] = (byte) ((seq >> 8) & 0xFF);
                tmp1[1] = (byte) (seq & 0xFF);

                // 将时间戳放在最后8个字节
                tmp1[2] = (byte) ((now >> 56) & 0xFF);
                tmp1[3] = (byte) ((now >> 48) & 0xFF);
                tmp1[4] = (byte) ((now >> 40) & 0xFF);
                tmp1[5] = (byte) ((now >> 32) & 0xFF);
                tmp1[6] = (byte) ((now >> 24) & 0xFF);
                tmp1[7] = (byte) ((now >> 16) & 0xFF);
                tmp1[8] = (byte) ((now >> 8) & 0xFF);
                tmp1[9] = (byte) (now & 0xFF);
                tmp1[10] = '\0';


        // 发送点对点消息（二进制）
        String channelName = mChannelName.getText().toString();
        mRtmClient.publish(channelName, tmp1, options2, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                printLog("send message success!");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                printLog("send message failed! " + errorInfo.toString());
            }
        });


    }

    // 如果需要停止定时器，调用这个函数
    private void stopTimer() {
        if (runnable2 != null) {
            handler2.removeCallbacks(runnable2);
        }
    }




    // 发送频道消息按钮
    @OnClick(R.id.send_channel_msg_button)
    void onSendChannelMsgBtnClicked()
    {

        startTimer();
        /*
        // 发送频道消息
        String message = mMessage.getText().toString();
        String channelName = mChannelName.getText().toString();
        PublishOptions options = new PublishOptions();
        options.setCustomType("");
        mRtmClient.publish(channelName, message, options, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                printLog("send message success!");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                printLog("send message failed! " + errorInfo.toString());
            }
        });
        */

/*
        // 发送点对点消息（二进制）
        byte[] message = new byte[] {96, 76, 82, 69};
        //String message = mMessage.getText().toString();
        String channelName = mChannelName.getText().toString();
        PublishOptions options = new PublishOptions();
        options.setChannelType(RtmConstants.RtmChannelType.USER);
        options.setCustomType("ByteArray");
        mRtmClient.publish(channelName, message, options, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                printLog("send message success!");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                printLog("send message failed! " + errorInfo.toString());
            }
        });

 */

        /*
        // 发送点对点消息（字符串）
        String message = mMessage.getText().toString();
        String channelName = mChannelName.getText().toString();
        PublishOptions options = new PublishOptions();
        options.setChannelType(RtmConstants.RtmChannelType.USER);
        options.setCustomType("PlainText");
        mRtmClient.publish(channelName, message, options, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                printLog("send message success!");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                printLog("send message failed! " + errorInfo.toString());
            }
        });
*/



    }

    @OnClick(R.id.clear_log_btn)
    void onClearLogBtnClicked() {
        mLogTextView.setText("");
    }

    // 将消息记录写入 TextView
    public void printLog(String record) {
        SpannableStringBuilder spannableString = new SpannableStringBuilder("[INFO] " + record + "\n");
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        uiHandler.post(() -> {
            mLogTextView.append(spannableString + "\n");
            mLogScrollView.fullScroll(View.FOCUS_DOWN);
        });
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}