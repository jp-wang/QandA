package com.jp.qanda.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jp.qanda.R;
import com.jp.qanda.TableConstants;
import com.jp.qanda.follow.UserDetailActivity;
import com.jp.qanda.util.FollowUtil;
import com.jp.qanda.vo.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author jpwang
 * @since 6/2/16
 */
public class UserListFragment extends Fragment {

    @BindView(R.id.userList)
    RecyclerView userList;

    private DatabaseReference userTable;
    private FirebaseRecyclerAdapter<User, UserViewHolder> userListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);

        ButterKnife.bind(this, rootView);

        userTable = FirebaseDatabase.getInstance().getReference(TableConstants.TABLE_USERS);

        userListAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(User.class, R.layout.user_list_item, UserViewHolder.class, userTable.limitToFirst(100)) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, User model, int position) {
                final DatabaseReference ref = getRef(position);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(UserDetailActivity.createIntent(getActivity(), ref.getKey()));
                    }
                });
                viewHolder.bindData(model, null, ref.getKey());
            }

        };

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayoutManager manager = new LinearLayoutManager(this.getActivity());
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        userList.setLayoutManager(manager);
        userList.setAdapter(userListAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (userListAdapter != null) {
            userListAdapter.cleanup();
        }
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.userAvatar)
        ImageView userAvatar;

        @BindView(R.id.userNameTv)
        TextView userName;

        @BindView(R.id.userTitleTv)
        TextView userTitle;

        @BindView(R.id.userAnswers)
        TextView userAnswers;

        @BindView(R.id.userFollowerTv)
        TextView userFollowers;

        @BindView(R.id.userFollowingAction)
        TextView userFollowingAction;

        public UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(User user, View.OnClickListener listener, final String uid) {

//            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
//            TextDrawable.IBuilder builder = TextDrawable.builder()
//                    .beginConfig()
//                    .withBorder(4)
//                    .endConfig()
//                    .rect();
//
//            userAvatar.setImageDrawable(builder.build(user.username.substring(0, 1), generator.getRandomColor()));
            userAvatar.setImageResource(R.drawable.ic_account_circle_black_36dp);
            if (user.photoUrl != null && !user.photoUrl.isEmpty()) {
                ImageLoader.getInstance().displayImage(user.photoUrl, userAvatar);
            }

            userName.setText(user.username);
            userTitle.setText(user.title);

            userAnswers.setText(itemView.getContext().getString(R.string.user_answer_desc, user.answers));
            userFollowers.setText(String.valueOf(itemView.getContext().getString(R.string.user_followers_count, user.followers)));

            userFollowingAction.setTag(uid);
            final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference(TableConstants.TABLE_USER_FOLLOWERS)
                    .child(uid).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userFollowingAction.setSelected(dataSnapshot.getValue() != null);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        @OnClick(R.id.userFollowingAction)
        void doFollowing(final View view) {
            FollowUtil.doFollowing(view);
        }
    }
}
