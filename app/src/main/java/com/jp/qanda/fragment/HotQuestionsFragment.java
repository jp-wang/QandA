package com.jp.qanda.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.jp.qanda.TableConstants;

/**
 * @author jpwang
 * @since 6/3/16
 */
public class HotQuestionsFragment extends QuestionListFragment {
    @Override
    protected Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child(TableConstants.TABLE_QUESTIONS)
                .orderByChild("timestamp");

    }
}
