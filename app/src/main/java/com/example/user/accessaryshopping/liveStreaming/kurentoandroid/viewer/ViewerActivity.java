package com.example.user.accessaryshopping.liveStreaming.kurentoandroid.viewer;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.user.accessaryshopping.APIService;
import com.example.user.accessaryshopping.NetworkClient;
import com.example.user.accessaryshopping.goodsList.GoodsListItem;
import com.example.user.accessaryshopping.liveStreaming.LiveDialogRecyclerAdapter;
import com.example.user.accessaryshopping.liveStreaming.kurentoandroid.liveChattingRecycler.LiveChattingRecyclerAdapter;
import com.example.user.accessaryshopping.liveStreaming.kurentoandroid.liveChattingRecycler.LiveChattingRecyclerItem;
import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.example.user.accessaryshopping.R;
import com.nhancv.webrtcpeer.rtc_plugins.ProxyRenderer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Created by nhancao on 7/20/17.
 */
@EActivity(R.layout.activity_viewer)
public class ViewerActivity extends MvpActivity<ViewerView, ViewerPresenter> implements ViewerView {
    private static final String TAG = ViewerActivity.class.getSimpleName();



    @ViewById(R.id.vGLSurfaceViewCall)
    protected SurfaceViewRenderer vGLSurfaceViewCall;
    private EglBase rootEglBase;
    private ProxyRenderer remoteProxyRenderer;
    private Toast logToast;

    //tcp message 통신을 위한 변수들
    Handler handler;
    String data;
    SocketChannel socketChannel;
    private static final String HOST = "54.180.186.195";
    private static final int PORT = 5001;


    //방번호 값 가져오기 위해 sharedpreference 를 쓴다.
    SharedPreferences sp, membersp;
    SharedPreferences.Editor editor;
    //방 값 넣기위해서
    String room, roomnickname;

    Button closeBtn;
    //recyclerview 에 필요한 변수들
    RecyclerView recyclerView;
    LiveChattingRecyclerAdapter recyclerAdapter;
    ArrayList<LiveChattingRecyclerItem> items = new ArrayList<>();

    //다이얼로그 리사이클러뷰 를 위한 변수
    //커스텀 다이얼로그 띄우기 위한 변수
    CusDialog mapDialog = new CusDialog();
    FragmentManager fragment = getFragmentManager();
    ArrayList<GoodsListItem> dialogitems = new ArrayList<>();
    ImageView goodsList;


    //메세지 전송할때 필요한 변수
    EditText messageEdit;
    Button messageSendBtn;

    //닉네임 값
    String nickname;

    //아이디 별로 상품 리스트 json으로 받아오기
    JSONArray goodsListresult = null;


    @AfterViews
    protected void init() {
        //config peer
        remoteProxyRenderer = new ProxyRenderer();
        rootEglBase = EglBase.create();

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        vGLSurfaceViewCall.setEnableHardwareScaler(true);
        vGLSurfaceViewCall.setMirror(true);
        remoteProxyRenderer.setTarget(vGLSurfaceViewCall);

        presenter.initPeerConfig();
        //방값으로 연결해주기 위해서
        sp = getApplicationContext().getSharedPreferences("webrtc_roomname", MODE_PRIVATE);
        room = sp.getString("roomname", "Error");
        roomnickname =sp.getString("nickname","Error");
        presenter.startCall(room);




    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("ddd",roomnickname);
        //리사이클러뷰 id 값 잡아주기
        recyclerView = (RecyclerView) findViewById(R.id.liveChattingRecyclerView);

        //채팅을 위한 닉네임 값 가져오기 위해서
        membersp = getApplicationContext().getSharedPreferences("member_autologin", MODE_PRIVATE);
        nickname = membersp.getString("nickname","error");
        //방나가기 버튼
        closeBtn = (Button) findViewById(R.id.viewerCloseBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.disconnect();
            }
        });

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



        goodsList = (ImageView)findViewById(R.id.goodsListimageView);
        goodsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                for (int i =0 ; i <10 ; i++){
//                    dialogitems.add(new GoodsListItem("383","dkdkd","dkdk","dkd"));
//                }
                goodsListHttpConnect();

                Toast.makeText(ViewerActivity.this,"눌리냐",Toast.LENGTH_SHORT).show();
            }
        });
    }//onresum


    @Override
    public void disconnect() {
        remoteProxyRenderer.setTarget(null);
        if (vGLSurfaceViewCall != null) {
            vGLSurfaceViewCall.release();
            vGLSurfaceViewCall = null;
        }

        finish();
    }

    @NonNull
    @Override
    public ViewerPresenter createPresenter() {
        return new ViewerPresenter(getApplication());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        presenter.disconnect();
    }

    @Override
    public void stopCommunication() {
        onBackPressed();
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
    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    @Override
    public VideoRenderer.Callbacks getRemoteProxyRenderer() {
        return remoteProxyRenderer;
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

            if (datasplit[0].equals(room)){
                items.add(new LiveChattingRecyclerItem(datasplit[1],datasplit[2]));
                setRecyclerView();
            }
            //String receive = "Coming word :"+ data;
            //textView.setText(receive);
        }
    };

    //커스텀 다이얼로그뷰
    @SuppressLint("ValidFragment")
    class CusDialog extends DialogFragment {
        RecyclerView dialogRecyclerView;
        LiveDialogRecyclerAdapter dialogRecyclerAdapter;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.liveviewr_custom_dialog_recycler_view,container);

            //후에 유지보수로
            /*DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics(); //디바이스 화면크기를 구하기위해
            int width = dm.widthPixels; //디바이스 화면 너비
            int height = dm.heightPixels; //디바이스 화면 높이*/

            //recycler dialog view 띄우기
            dialogRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler);
            dialogRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
            Window window=getDialog().getWindow();
            //주변이 검정색으로 변하는거 막기
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            //넓이 조절 하고 싶지만 잘안됨 후에 유지보수로
            //window.getAttributes().width = width;
            //window.getAttributes().height = height/2;
            //setdata
            dialogRecyclerAdapter = new LiveDialogRecyclerAdapter(this.getActivity(),dialogitems);
            dialogRecyclerView.setAdapter(dialogRecyclerAdapter);

            return rootView;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            dialogitems.clear();
        }
    }//CustomDialog

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

    @Override
    protected void onPause() {
        super.onPause();
        //잠깐이라도 다른앱으로 갈때 꺼지기
        stopCommunication();
    }

    //룸에있는 방송자에 상품 리스트 가져오기
    public void goodsListHttpConnect() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);

        APIService service = retrofit.create(APIService.class);

        class syncTaskUploadClass extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... voids) {
                Map<String, Object> params = new HashMap<>();
                params.put("nickname", roomnickname);

                Call<ResponseBody> call = service.goodsListRecycler(params);
                try {
                    return call.execute().body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    JSONObject jsonObj = new JSONObject(s);
                    goodsListresult = jsonObj.getJSONArray("result");
                    if (goodsListresult.isNull(0)) {
                        //값이 없을때

                    } else {
                        //값이 있을때
                        for (int i = 0; i < goodsListresult.length(); i++) {
                            JSONObject goodsdetail = goodsListresult.getJSONObject(i);
                            String goodsTitle = goodsdetail.getString("goodstitle");
                            String goodsPrice = goodsdetail.getString("goodsprice");
                            String img = goodsdetail.getString("img");
                            String no = goodsdetail.getString("no");
                            Log.e("goodsTitle", goodsTitle);
                            dialogitems.add(new GoodsListItem(goodsTitle, goodsPrice, img, no));

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mapDialog.show(fragment,"dd");//다이얼 로그

            }
        }//syncTaskUploadClass
        syncTaskUploadClass asyncTaskUploadClassRe = new syncTaskUploadClass();
        asyncTaskUploadClassRe.execute();

    }//goodsListHttpConnect

}
