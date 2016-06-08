package com.jp.qanda.fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.jp.qanda.TableConstants;

/**
 * @author jpwang
 * @since 6/3/16
 */
public class MyAnswersFragment extends QuestionListFragment {
    @Override
    protected Query getQuery(DatabaseReference databaseReference) {
        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return databaseReference.child(TableConstants.TABLE_USER_ANSWERS)
                .child(currentUserId).orderByChild("timestamp");
    }
}
