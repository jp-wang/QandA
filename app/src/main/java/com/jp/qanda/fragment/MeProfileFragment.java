package com.jp.qanda.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jp.qanda.R;
import com.jp.qanda.TableConstants;
import com.jp.qanda.splash.SplashActivity;
import com.jp.qanda.vo.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author jpwang
 * @since 6/2/16
 */
public class MeProfileFragment extends Fragment {
    @BindView(R.id.userAvatar)
    ImageView userAvatar;

    @BindView(R.id.userNameTv)
    TextView userNameTv;

    @BindView(R.id.userTitleTv)
    TextView userTitleTv;

    @BindView(R.id.userFollowerTv)
    TextView userFollowersTv;

    @BindView(R.id.questFeeTv)
    TextView questFeeTv;

    @BindView(R.id.userTotalRevenueTv)
    TextView totalRevenueTv;

    private DatabaseReference databaseReference;

    private DatabaseReference meReference;

    private ValueEventListener meInfoListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_me_profile, container, false);
        ButterKnife.bind(this, rootView);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        meReference = databaseReference.child(TableConstants.TABLE_USERS).child(currentUserId);

        return rootView;
    }

    private void updateBasicUI(User user) {
        if (user.photoUrl != null) {
            ImageLoader.getInstance().displayImage(user.photoUrl, userAvatar);
        }
        userNameTv.setText(user.username);
        userTitleTv.setText(user.title);
        questFeeTv.setText(getString(R.string.my_quest_fee_desc, user.questFee));
        userFollowersTv.setText(getString(R.string.user_followers_count, user.followers));
        totalRevenueTv.setText(getString(R.string.my_total_revenue, user.totalRevenue));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        meInfoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateBasicUI(dataSnapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        meReference.addValueEventListener(meInfoListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        meReference.removeEventListener(meInfoListener);
    }

    @OnClick(R.id.myQuestions)
    void gotoMyQuestions(View view) {
        new MyQuestionsFragment().launchInActivity(this.getContext(), "My Questions");
    }

    @OnClick(R.id.myAnswers)
    void gotoMyAnswers(View view) {
        new MyAnswersFragment().launchInActivity(this.getContext(), "My Answers");
    }

    @OnClick(R.id.myListening)
    void gotoMyListening(View view) {

    }

    @OnClick(R.id.logout)
    void doLogout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this.getContext(), SplashActivity.class));
        this.getActivity().finish();
    }
}
