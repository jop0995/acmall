package com.example.user.accessaryshopping.liveStreaming.kurentoandroid.broadcaster;

import android.Manifest;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.user.accessaryshopping.liveStreaming.kurentoandroid.liveChattingRecycler.LiveChattingRecyclerAdapter;
import com.example.user.accessaryshopping.liveStreaming.kurentoandroid.liveChattingRecycler.LiveChattingRecyclerItem;
import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.example.user.accessaryshopping.R;
import com.nhancv.npermission.NPermission;
import com.nhancv.webrtcpeer.rtc_plugins.ProxyRenderer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by nhancao on 7/20/17.
 */
@EActivity(R.layout.activity_broadcaster)
public class BroadCasterActivity extends MvpActivity<BroadCasterView, BroadCasterPresenter>
        implements BroadCasterView, NPermission.OnPermissionResult {
    private static final String TAG = BroadCasterActivity.class.getSimpleName();

    @ViewById(R.id.vGLSurfaceViewCall)
    protected SurfaceViewRenderer vGLSurfaceViewCall;
    private NPermission nPermission;
    private EglBase rootEglBase;
    private ProxyRenderer localProxyRenderer;
    private Toast logToast;
    private boolean isGranted;

    SharedPreferences sp, membersp;
    SharedPreferences.Editor editor;

    //방이름
    String room;
    //닉네임 값
    String nickname;

    //버튼 변수
    Button presenterCloseBtn;

    //tcp message 통신을 위한 변수들
    Handler handler;
    String data;
    SocketChannel socketChannel;
    private static final String HOST = "54.180.186.195";
    private static final int PORT = 5001;

    //메세지 전송할때 필요한 변수
    EditText messageEdit;
    Button messageSendBtn;

    //recyclerview 에 필요한 변수들
    RecyclerView recyclerView;
    LiveChattingRecyclerAdapter recyclerAdapter;
    ArrayList<LiveChattingRecyclerItem> items = new ArrayList<>();


    @AfterViews
    protected void init() {

        nPermission = new NPermission(true);

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        //config peer
        localProxyRenderer = new ProxyRenderer();
        rootEglBase = EglBase.create();

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        vGLSurfaceViewCall.setEnableHardwareScaler(true);
        vGLSurfaceViewCall.setMirror(true);
        localProxyRenderer.setTarget(vGLSurfaceViewCall);

        presenter.initPeerConfig();
    }

    @Override
    public void disconnect() {
        localProxyRenderer.setTarget(null);
        if (vGLSurfaceViewCall != null) {
            vGLSurfaceViewCall.release();
            vGLSurfaceViewCall = null;
        }

        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        //리사이클러뷰 id 값 잡아주기
        recyclerView = (RecyclerView) findViewById(R.id.liveChattingRecyclerView);

        presenterCloseBtn = (Button) findViewById(R.id.presenterCloseBtn);
        presenterCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.disconnect();
            }
        });

        //방 번호값 가져오기 위해서
        sp = getApplicationContext().getSharedPreferences("webrtc_roomname",MODE_PRIVATE);
        Toast.makeText(this,sp.getString("roomname","Error"),Toast.LENGTH_SHORT).show();
        room = sp.getString("roomname","Error");
        //닉네임 값 가져오기 위해서
        membersp = getApplicationContext().getSharedPreferences("member_autologin", MODE_PRIVATE);
        nickname = membersp.getString("nickname","error");

        //Toast.makeText(this,"BroadCasterActivity",Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT < 23 || isGranted) {
            presenter.startCall(room);
        } else {
            nPermission.requestPermission(this, Manifest.permission.CAMERA);
        }

        messageEdit = (EditText)findViewById(R.id.messageEdit);
        //메세지 전송 버튼 누르면
        messageSendBtn=(Button)findViewById(R.id.messageSendBtn);
        messageSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageEdit.getText().toString().length() != 0){
                    String return_msg = messageEdit.getText().toString();
                    //리사이클러 뷰에 내가 쓴 채팅 아이템 추가

                    items.add(new LiveChattingRecyclerItem(nickname,return_msg));

                    //내가쓴 채팅 내용 상대방에게 보내기
                    new SendmsgTask().execute(room+"//"+nickname+"//"+return_msg);
                    messageEdit.setText("");
                    setRecyclerView();
                }
            }
        });

        //방입장했을때 처음 소켓 통신을 위한 연결 작업
        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));
                    //입장할때 메세지 보내기
                    new  SendmsgTask().execute(room+"//"+nickname+"//--입장했습니다--");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("asd", e.getMessage() + "a");

                }
                checkUpdate.start();


            }
        }).start();

        setRecyclerView();
    }//on Resume

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        nPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionResult(String permission, boolean isGranted) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                this.isGranted = isGranted;
                if (!isGranted) {
                    nPermission.requestPermission(this, Manifest.permission.CAMERA);
                } else {
                    //nPermission.requestPermission(this, Manifest.permission.RECORD_AUDIO);

                    presenter.startCall(room);

                }
                break;
            default:
                break;
        }
    }

    @NonNull
    @Override
    public BroadCasterPresenter createPresenter() {
        return new BroadCasterPresenter(getApplication());
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        presenter.disconnect();
    }

    @Override
    public void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    @Override
    public VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            if (!captureToTexture()) {
                return null;
            }
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            return null;
        }
        return videoCapturer;
    }

    @Override
    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    @Override
    public VideoRenderer.Callbacks getLocalProxyRenderer() {
        return localProxyRenderer;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && presenter.getDefaultConfig().isUseCamera2();
    }

    private boolean captureToTexture() {
        return presenter.getDefaultConfig().isCaptureToTexture();
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    //recyclerview 실행 메소드
    void setRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        //adapter에 데이터 넣는 줄
        recyclerAdapter = new LiveChattingRecyclerAdapter(getApplicationContext(), items);
        recyclerView.setAdapter(recyclerAdapter);

        //리사이클러뷰 맨빝에서 보여주기 위해서
        recyclerView.scrollToPosition(items.size()-1);
    }

    private class SendmsgTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8")); //서버로
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
                    messageEdit.setText("");
                }
            });

        }
    }//SendmsgTask

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
                Charset charset = Charset.forName("UTF-8");
                data = charset.decode(byteBuffer).toString();
                Log.e("receive", "msg :" + data);
                handler.post(showUpdate);

            } catch (IOException e) {
                Log.e("getMsg", e.getMessage() + "");
                try {
                    //나갈때 메세지 보내기
                    new  SendmsgTask().execute(room+"//"+nickname+"//--퇴장했습니다--");
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
    }//receive
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
            //이방에 맞는 메세지가 들어왔는지 판별하기 위해서
            String[] datasplit = data.split("//");
            Log.e("뭐라고 오는지",data);
            Log.e("뭐라고 오는",datasplit[0]);
            if (datasplit[0].equals(room)){
                items.add(new LiveChattingRecyclerItem(datasplit[1],datasplit[2]));
                setRecyclerView();
            }
            //String receive = "Coming word :"+ data;
            //textView.setText(receive);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //나갈때 메세지 보내기

        try {
            new  SendmsgTask().execute(room+"//"+nickname+"//--퇴장했습니다--");
            //정보가 바로 넘어가지 않기 때문에 .
            Thread.sleep(200);
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
