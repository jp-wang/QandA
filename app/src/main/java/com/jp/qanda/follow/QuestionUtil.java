package com.jp.qanda.follow;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jp.qanda.TableConstants;
import com.jp.qanda.vo.Question;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author jpwang
 * @since 6/3/16
 */
public final class QuestionUtil {
    public static final Map<String, Long> times = new LinkedHashMap<>();

    static {
        times.put("year", TimeUnit.DAYS.toMillis(365));
        times.put("month", TimeUnit.DAYS.toMillis(30));
        times.put("week", TimeUnit.DAYS.toMillis(7));
        times.put("day", TimeUnit.DAYS.toMillis(1));
        times.put("hour", TimeUnit.HOURS.toMillis(1));
        times.put("minute", TimeUnit.MINUTES.toMillis(1));
        times.put("second", TimeUnit.SECONDS.toMillis(1));
    }


    public static void updateQuestion(String questionKey, Question question, DatabaseReference.CompletionListener listener) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> childUpdates = new HashMap<>();
        final Map<String, Object> questionMap = question.toMap();
        childUpdates.put("/" + TableConstants.TABLE_QUESTIONS + "/" + questionKey, questionMap);
        childUpdates.put("/" + TableConstants.TABLE_USER_QUESTIONS + "/" + question.from + "/" + questionKey, questionMap);
        childUpdates.put("/" + TableConstants.TABLE_USER_ANSWERS + "/" + question.to + "/" + questionKey, questionMap);
        databaseReference.updateChildren(childUpdates, listener);
    }

    @NonNull
    public static String getDisplayTime(long duration) {
        StringBuilder res = new StringBuilder();
        for (Map.Entry<String, Long> time : times.entrySet()) {
            long timeDelta = duration / time.getValue();
            if (timeDelta > 0) {
                res.append(timeDelta)
                        .append(" ")
                        .append(time.getKey())
                        .append(timeDelta > 1 ? "s" : "")
                        .append(" ago");
                break;
            }
        }
        if ("".equals(res.toString())) {
            return "0 seconds ago";
        } else {
            return res.toString();
        }
    }

    @NonNull
    public static String getDisplayTime(Date start, Date end) {
        assert start.after(end);
        return getDisplayTime(end.getTime() - start.getTime());
    }
}
