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
import com.jp.editabletextview.EditableTextView;
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
public class MeProfileFragment extends Fragment implements EditableTextView.EditableTextViewListener {
    @BindView(R.id.userAvatar)
    ImageView userAvatar;

    @BindView(R.id.userNameTv)
    EditableTextView userNameTv;

    @BindView(R.id.userTitleTv)
    EditableTextView userTitleTv;

    @BindView(R.id.userSummaryTv)
    EditableTextView userSummaryTv;

    @BindView(R.id.userFollowerTv)
    TextView userFollowersTv;

    @BindView(R.id.questFeeTv)
    EditableTextView questFeeTv;

    @BindView(R.id.userTotalRevenueTv)
    TextView totalRevenueTv;

    private DatabaseReference databaseReference;

    private DatabaseReference meReference;

    private ValueEventListener meInfoListener;

    private User me;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_me_profile, container, false);
        ButterKnife.bind(this, rootView);

        bindEditableTextViewListener();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        meReference = databaseReference.child(TableConstants.TABLE_USERS).child(currentUserId);

        return rootView;
    }

    private void bindEditableTextViewListener() {
        userNameTv.setEditTextViewListener(this);
        userTitleTv.setEditTextViewListener(this);
        userSummaryTv.setEditTextViewListener(this);
        questFeeTv.setEditTextViewListener(this);
    }

    private void updateBasicUI(User user) {
        me = user;
        if (user.photoUrl != null && !user.photoUrl.isEmpty()) {
            ImageLoader.getInstance().displayImage(user.photoUrl, userAvatar);
        }
        userNameTv.setText(user.username);
        userTitleTv.setText(user.title);
        userSummaryTv.setText(user.desc);
        questFeeTv.setText(getString(R.string.my_quest_fee, user.questFee));
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

    @Override
    public void onEditModeStart(View v) {
    }

    @Override
    public void onEditModeFinish(View v, String text) {
        switch (v.getId()) {
            case R.id.userNameTv:
                if (text.trim().isEmpty()) {
                    userNameTv.setText(me.username);
                } else if (!text.trim().equals(me.username)) {
                    me.username = text.trim();
                    meReference.setValue(me);
                }
                break;
            case R.id.userTitleTv:
                if (text.trim().isEmpty()) {
                    userTitleTv.setText(me.title);
                } else if (!text.trim().equals(me.title)) {
                    me.title = text.trim();
                    meReference.setValue(me);
                }
                break;
            case R.id.userSummaryTv:
                if (text.trim().isEmpty()) {
                    userSummaryTv.setText(me.desc);
                } else if (!text.trim().equals(me.desc)) {
                    me.desc = text.trim();
                    meReference.setValue(me);
                }
                break;
            case R.id.questFeeTv:
                if (text.trim().isEmpty()) {
                    questFeeTv.setText(getString(R.string.my_quest_fee, me.questFee));
                } else {
                    try {
                        float newFee = Float.valueOf(text.trim());
                        if (newFee != me.questFee) {
                            me.questFee = newFee;
                            meReference.setValue(me);
                        }
                    } catch (NumberFormatException e) {

                    } finally {
                        questFeeTv.setText(getString(R.string.my_quest_fee, me.questFee));
                    }
                }
                break;
        }
    }
}
