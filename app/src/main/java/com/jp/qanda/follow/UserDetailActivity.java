package com.jp.qanda.follow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
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
import com.jp.qanda.vo.Question;
import com.jp.qanda.vo.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author jpwang
 * @since 6/2/16
 */
public class UserDetailActivity extends AppCompatActivity {
    private final static String U_ID = "uid";

    public static Intent createIntent(Context context, String uid) {
        Intent intent = new Intent();
        intent.setClass(context, UserDetailActivity.class);
        intent.putExtra(U_ID, uid);
        return intent;
    }

    @BindView(R.id.userAvatar)
    ImageView userAvatar;

    @BindView(R.id.userNameTv)
    TextView userNameTv;

    @BindView(R.id.userTitleTv)
    TextView userTitleTv;

    @BindView(R.id.userDescTv)
    TextView userDescTv;

    @BindView(R.id.userFollowerTv)
    TextView userFollowersTv;

    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.questionEt)
    EditText questionEt;

    @BindView(R.id.questFeeTv)
    TextView questFeeTv;

    @BindView(android.R.id.content)
    View rootView;

    @BindView(R.id.action)
    View shareAction;

    private DatabaseReference userTable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        ButterKnife.bind(this);

        shareAction.setVisibility(View.VISIBLE);
        userTable = FirebaseDatabase.getInstance().getReference(TableConstants.TABLE_USERS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        userTable.child(getIntent().getStringExtra(U_ID)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                updateBasicInfo(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateBasicInfo(User user) {
        title.setText(getString(R.string.user_detail_title, user.username));

        userAvatar.setImageResource(R.drawable.ic_account_circle_black_36dp);
        if (user.photoUrl != null) {
            ImageLoader.getInstance().displayImage(user.photoUrl, userAvatar);
        }
        userNameTv.setText(user.username);
        userTitleTv.setText(user.title);
        userDescTv.setText(user.desc);
        userFollowersTv.setText(getString(R.string.user_followers_count, user.followers));
        questFeeTv.setText(getString(R.string.user_quest_fee, user.questFee));
    }

    @OnClick(R.id.userFollowingAction)
    void doFollowing(View view) {
        FollowUtil.doFollowing(view);
    }

    @OnClick(R.id.questSubmit)
    void doSubmit(final View view) {
        view.setEnabled(false);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final String questionKey = databaseReference.child(TableConstants.TABLE_QUESTIONS).push().getKey();
        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Question question = new Question(questionEt.getText().toString().trim(), Float.valueOf(questFeeTv.getText().toString()), currentUserId, getIntent().getStringExtra(U_ID));
        QuestionUtil.updateQuestion(questionKey, question, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Snackbar.make(rootView, databaseError != null && databaseError.getMessage() != null ? databaseError.getMessage() : "Question was sent out.", Snackbar.LENGTH_LONG).show();
                view.setEnabled(true);
            }
        });
    }

    @OnClick(R.id.action)
    void doShare(View view) {

    }
}
