package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartBuild;
import kr.koogle.android.smartconstruction.http.SmartBuildService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartBuildFragment extends Fragment {

    private static final String TAG = "SmartBuildFragment";
    private View rootView;
    private SmartBuildAdapter adapter;
    // public static ArrayList<SmartBuild> smartBulids; // SmartSingleton.smartBuilds -> smartBuilds 변경 사용할 경우 !!
    private LayoutInflater mInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_smartbuild, container, false);
        mInflater = inflater; //getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Settings 값 !!
        RbPreference pref = new RbPreference(getContext());

        // Lookup the recyclerview in activity layout
        RecyclerView rvSmartBuilds = (RecyclerView) rootView.findViewById(R.id.rvSmartBuilds);

        // Initialize smartBulids - do not reinitialize an existing reference used by an adapter
        // smartBulids = SmartBuild.createSmartBuildsList(20);
        // SmartSingleton.getInstance();
        // SmartSingleton.arrSmartBuilds = new ArrayList<SmartBuild>();

        // Create adapter passing in the sample user data
        adapter = new SmartBuildAdapter(getContext(), SmartSingleton.arrSmartBuilds);

        if(SmartSingleton.arrSmartBuilds.size() <= 0) {
            /******************************************************************************************/
            // SmartBuild 값 불러오기 (진행중인 현장)
            Log.d(TAG, "SmartBuildService.checkLoginToken 실행!! / accessToken : " + pref.getValue("accessToken", ""));
            SmartBuildService smartBuildService = ServiceGenerator.createService(SmartBuildService.class, pref.getValue("accessToken", ""));
            Call<ArrayList<SmartBuild>> call = smartBuildService.getSmartBuilds();

            call.enqueue(new Callback<ArrayList<SmartBuild>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartBuild>> call, Response<ArrayList<SmartBuild>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartBuild> responseSmartBuilds = response.body();

                        SmartSingleton.arrSmartBuilds.addAll(responseSmartBuilds);
                        // 최근 카운트 체크
                        int curSize = adapter.getItemCount();
                        adapter.notifyItemRangeInserted(curSize, responseSmartBuilds.size());
                    } else {
                        Toast.makeText(getContext(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();

                        Intent intentLogin = new Intent(getContext(), LoginActivity.class);
                        startActivity(intentLogin);
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartBuild>> call, Throwable t) {
                    Toast.makeText(getContext(), "네트워크 상태가 좋지 않습니다!!!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
            /******************************************************************************************/
        }
        /*
        smartBulids.addAll(SmartBuild.createSmartBuildsList(10));

        // 새로운 리스트 추가하기!!
        ArrayList<SmartBuild> newItems = SmartBuild.createSmartBuildsList(3);
        smartBulids.addAll(newItems);

        // 기존 리스트에 추가하기!!
        adapter.notifyItemRangeInserted(curSize, newItems.size());

        // 중간에 아이템 추가하기!!
        smartBulids.add(0, new SmartBuild());
        adapter.notifyItemInserted(0);

        // Scrolling to New Items
        adapter.notifyItemInserted(12);
        rvSmartBuilds.scrollToPosition(12);

        adapter.notifyItemInserted(smartBulids.size() - 1); // Last element position
        rvSmartBuilds.scrollToPosition(adapter.getItemCount() - 1); // update based on adapter
        */

        rvSmartBuilds.setHasFixedSize(true);

        // Attach the adapter to the recyclerview to populate items
        /*
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(adapter);
        alphaInAnimationAdapter.setDuration(1000);
        rvSmartBuilds.setAdapter(new ScaleInAnimationAdapter(alphaInAnimationAdapter));
        */
        rvSmartBuilds.setAdapter(adapter);

        // Set layout manager to position the items
        rvSmartBuilds.setLayoutManager(new LinearLayoutManager(getContext()));

        /*
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.scrollToPosition(0);
        rvSmartBuilds.setLayoutManager(layoutManager);

        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        */
/*
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        rvSmartBuilds.addItemDecoration(itemDecoration);
*/

        rvSmartBuilds.setItemAnimator(new SlideInUpAnimator());

        /***************************************************************************/
        adapter.setOnItemClickListener(new SmartBuildAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String name = SmartSingleton.arrSmartBuilds.get(position).strName;
                SmartSingleton.arrSmartBuilds.get(position).strName = "변경되었습니다.";
                adapter.notifyItemChanged(position);
                Toast.makeText(getContext(), name + " was clicked!", Toast.LENGTH_SHORT).show();
            }
        });
        /***************************************************************************/

        // Handling Touch Events
        rvSmartBuilds.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                // Handle on touch events here
                //Log.d(TAG, "onTouchEvent : touched !!");
            }

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        /* 가장 쉽게 클릭 이벤츠 핸들러 만들기
        ItemClickSupport.addTo(rvSmartBuilds).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        // do it
                        Log.d("LLL", "position: " + position);
                    }
                }
        );
         */

        return rootView;
    }
}
