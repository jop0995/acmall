package com.example.user.accessaryshopping;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class ChacttingTest extends AppCompatActivity {

    Handler handler;
    String data;
    SocketChannel socketChannel;
    private static final String HOST = "192.168.0.205";
    private static final int PORT = 5001;
    String msg;

    EditText inputTxt;
    Button sendBtn;
    TextView textView;

    String roomName = "나라미//--입장했습니다.--";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chactting_test);

        sendBtn = (Button) findViewById(R.id.sentBtn);
        inputTxt = (EditText) findViewById(R.id.inputTxt);
        textView = (TextView) findViewById(R.id.textView);

        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));
                    //입장할때 메세지 보내기
                    new  SendmsgTask().execute("방이름//닉네임//--입장했습니다--");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("asd", e.getMessage() + "a");

                }
                checkUpdate.start();


            }
        }).start();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String return_msg = inputTxt.getText().toString();
                    if (!TextUtils.isEmpty(return_msg)) {
                        new SendmsgTask().execute("방이름//닉네임//"+return_msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

    }//onCreate

    private class SendmsgTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("EUC-KR")); //서버로
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    inputTxt.setText("");
                }
            });

        }
    }

    void receive() {
        while (true) {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = socketChannel.read(byteBuffer); //데이터받기
                Log.e("readByteCount", readByteCount + "");
                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (readByteCount == -1) {
                    throw new IOException();
                }
                byteBuffer.flip(); //문자열로 반환
                Charset charset = Charset.forName("EUC-KR");
                data = charset.decode(byteBuffer).toString();
                Log.e("receive", "msg :" + data);
                handler.post(showUpdate);

            } catch (IOException e) {
                Log.e("getMsg", e.getMessage() + "");
                try {
                    //나갈때 메세지 보내기
                    new  SendmsgTask().execute("방이름//닉네임//--퇴장했습니다--");
                    Thread.sleep(200);
                    socketChannel.close();
                    break;
                } catch (IOException ee) {
                    ee.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

            }
        }
    }

    private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                String line;
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private Runnable showUpdate = new Runnable() {
        @Override
        public void run() {
            String receive = "Coming word :"+ data;
            textView.setText(receive);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //나갈때 메세지 보내기

        try {
            new  SendmsgTask().execute("방이름//닉네임//--퇴장했습니다--");
            //정보가 바로 넘어가지 않기 때문에 .
            Thread.sleep(200);
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}//Class
