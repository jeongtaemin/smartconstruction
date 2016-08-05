package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.SmartWork;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartWorkViewActivity extends AppCompatActivity {
    private static final String TAG = "SmartWorkViewActivity";
    private RbPreference pref;

    @Bind(R.id.txt_work_view_build_name) TextView _txtBuildName;
    @Bind(R.id.txt_work_view_date) TextView _txtDate;
    @Bind(R.id.txt_work_view_weather) TextView _txtWeather;
    @Bind(R.id.txt_work_view_memo) TextView _txtMemo;

    public static RecyclerView recyclerViewLabor;
    private SmartWorkLaborAdapter adapterLabor;
    private RecyclerView.LayoutManager layoutManagerLabor;

    public static RecyclerView recyclerViewPhoto;
    private SmartWorkPhotoAdapter adapterPhoto;
    private RecyclerView.LayoutManager layoutManagerPhoto;

    // intent 로 넘어온 값 받기
    private Intent intent;
    private String strBuildCode;
    private String smartCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_view);
        ButterKnife.bind(this);
        // intent 등록
        intent = getIntent();

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(this);

        // 리스트 클릭시 넘어온값 받기 !!
        strBuildCode = getIntent().getExtras().getString("strBuildCode");
        smartCode = getIntent().getExtras().getString("strCode");

        // RecyclerView 저장
        recyclerViewLabor = (RecyclerView) findViewById(R.id.rv_work_view_labors);
        // LayoutManager 저장
        layoutManagerLabor = new LinearLayoutManager(SmartWorkViewActivity.this);
        // RecycleView에 LayoutManager 세팅
        recyclerViewLabor.setLayoutManager(layoutManagerLabor);

        /******************************************************************************************/
        // Adapter 생성
        adapterLabor = new SmartWorkLaborAdapter(this, SmartSingleton.smartWork.arrSmartLabors);
        // RecycleView 에 Adapter 세팅
        recyclerViewLabor.setAdapter(adapterLabor);
        // 리스트 표현하기 !!
        recyclerViewLabor.setItemAnimator(new SlideInUpAnimator());

        /***************************************************************************/
        adapterLabor.setOnItemClickListener(new SmartWorkLaborAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final String intId = String.valueOf(SmartSingleton.smartWork.arrSmartLabors.get(position).intId);
                adapterLabor.notifyItemChanged(position);
            }
        });
        /***************************************************************************/

        // RecyclerView 저장
        recyclerViewPhoto = (RecyclerView) findViewById(R.id.rv_work_view_photos);
        // LayoutManager 저장
        layoutManagerPhoto = new LinearLayoutManager(SmartWorkViewActivity.this);
        // RecycleView에 LayoutManager 세팅
        recyclerViewPhoto.setLayoutManager(layoutManagerPhoto);

        /******************************************************************************************/
        // Adapter 생성
        adapterPhoto = new SmartWorkPhotoAdapter(this, SmartSingleton.smartWork.arrSmartPhotos);
        // RecycleView 에 Adapter 세팅
        recyclerViewPhoto.setAdapter(adapterPhoto);
        // 리스트 표현하기 !!
        recyclerViewPhoto.setItemAnimator(new SlideInUpAnimator());

        /***************************************************************************/
        adapterPhoto.setOnItemClickListener(new SmartWorkPhotoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final String intId = String.valueOf(SmartSingleton.smartWork.arrSmartPhotos.get(position).intId);
                adapterPhoto.notifyItemChanged(position);
            }
        });
        /***************************************************************************/

        // 내용 넣는 부분
        if( !smartCode.equals("") ) {
            writeWork();
        }

        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_work_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ico_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmartWorkViewActivity.this.finish();
            }
        });

    }

    private void writeWork() {
        /******************************************************************************************/
        // SmartBuild 값 불러오기 (진행중인 현장)
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));

        //final Map<String, String> mapOptions = new HashMap<String, String>();
        //mapOptions.put("offset", String.valueOf(layoutManager.getItemCount()));
        Call<SmartWork> call = smartService.getSmartWork(strBuildCode, smartCode);

        call.enqueue(new Callback<SmartWork>() {
            @Override
            public void onResponse(Call<SmartWork> call, Response<SmartWork> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final SmartWork responses = response.body();

                    if( !responses.strCode.equals("") ) {
                        Log.d(TAG, "responses : strCode " + responses.strCode);

                        _txtBuildName.setText(responses.strBuildCode);
                        _txtDate.setText(responses.strDate);
                        _txtWeather.setText(String.valueOf(responses.intWeather));
                        _txtMemo.setText(responses.strMemo);

                        SmartSingleton.smartWork.arrSmartLabors.addAll(responses.arrSmartLabors);
                        // 최근 카운트 체크
                        int curSizeLabor = adapterLabor.getItemCount();
                        adapterLabor.notifyItemRangeInserted(curSizeLabor, responses.arrSmartLabors.size());

                        SmartSingleton.smartWork.arrSmartPhotos.addAll(responses.arrSmartPhotos);
                        // 최근 카운트 체크
                        int curSizePhoto = adapterPhoto.getItemCount();
                        adapterPhoto.notifyItemRangeInserted(curSizePhoto, responses.arrSmartPhotos.size());

                    } else {
                        Snackbar.make(SmartWorkActivity.recyclerView, "마지막 리스트 입니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } else {
                    Toast.makeText(getApplication(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "responses : 데이터가 정확하지 않습니다.");
                }
            }

            @Override
            public void onFailure(Call<SmartWork> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "네트워크 상태가 좋지 않습니다!!!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());
            }
        });
        /******************************************************************************************/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // 프로그래스 실행 !!
            //showIndeterminateProgressDialog(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
