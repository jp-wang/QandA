package com.jp.qanda.follow;

import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.jp.qanda.TableConstants;
import com.jp.qanda.vo.User;

import java.util.HashMap;

/**
 * @author jpwang
 * @since 6/2/16
 */
public final class FollowUtil {
    public static void doFollowing(final View view) {
        final String uid = (String) view.getTag();
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference(TableConstants.TABLE_USER_SUPERIORS)
                .child(currentUser).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    HashMap<String, Object> updateChilds = new HashMap<>();
                    updateChilds.put("/" + TableConstants.TABLE_USER_SUPERIORS + "/" + currentUser + "/" + uid, true);
                    updateChilds.put("/" + TableConstants.TABLE_USER_FOLLOWERS + "/" + uid + "/" + currentUser, true);
                    FirebaseDatabase.getInstance().getReference()
                            .updateChildren(updateChilds);
                    view.setSelected(true);
                } else {
                    FirebaseDatabase.getInstance().getReference(TableConstants.TABLE_USER_SUPERIORS)
                            .child(currentUser).child(uid).removeValue();
                    FirebaseDatabase.getInstance().getReference(TableConstants.TABLE_USER_FOLLOWERS)
                            .child(uid).child(currentUser).removeValue();
                    view.setSelected(false);
                }

                FirebaseDatabase.getInstance().getReference(TableConstants.TABLE_USERS)
                        .child(uid).runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        User user = mutableData.getValue(User.class);
                        if (user == null) {
                            return Transaction.success(mutableData);
                        }
                        user.followers = view.isSelected() ? user.followers + 1 : user.followers - 1;
                        if (user.followers < 0) {
                            user.followers = 0;
                        }
                        mutableData.setValue(user);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });

                FirebaseDatabase.getInstance().getReference(TableConstants.TABLE_USERS)
                        .child(currentUser).runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        User user = mutableData.getValue(User.class);
                        if (user == null) {
                            return Transaction.success(mutableData);
                        }
                        user.superiors = view.isSelected() ? user.superiors + 1 : user.superiors - 1;
                        if (user.superiors < 0) {
                            user.superiors = 0;
                        }
                        mutableData.setValue(user);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
