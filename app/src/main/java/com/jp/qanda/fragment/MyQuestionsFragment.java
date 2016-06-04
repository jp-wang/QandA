package com.jp.qanda.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * @author jpwang
 * @since 6/3/16
 */
public class MyQuestionsFragment extends QuestionListFragment {
    @Override
    protected Query getQuery(DatabaseReference databaseReference) {
        return null;
    }
}
