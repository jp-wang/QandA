package com.jp.qanda.question;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.jp.qanda.ConfigConstants;
import com.jp.qanda.R;
import com.jp.qanda.TableConstants;
import com.jp.qanda.util.AudioHandleUtil;
import com.jp.qanda.util.QuestionUtil;
import com.jp.qanda.vo.Answer;
import com.jp.qanda.vo.Question;
import com.jp.qanda.vo.User;
import com.jp.recorderandplayer.playerview.PlayerView;
import com.jp.recorderandplayer.recorderview.RecorderView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * @author jpwang
 * @since 6/2/16
 */
@RuntimePermissions
public class QuestionAnswerDetailActivity extends AppCompatActivity implements RecorderView.RecorderViewListener, PlayerView.PlayerViewListener {
    private final static String Q_KEY = "q_key";

    public static Intent createIntent(Context context, String questionKey) {
        Intent intent = new Intent();
        intent.setClass(context, QuestionAnswerDetailActivity.class);
        intent.putExtra(Q_KEY, questionKey);
        return intent;
    }

    @BindView(R.id.userAvatar)
    ImageView userAvatar;

    @BindView(R.id.userNameTv)
    TextView userNameTv;

    @BindView(R.id.questFeeTv)
    TextView questFeeTv;

    @BindView(R.id.questionTv)
    TextView questionTv;

    @BindView(R.id.answerContainer)
    View answerContainer;

    @BindView(R.id.answerContentLength)
    TextView answerContentLength;

    @BindView(R.id.answerUserAvatar)
    ImageView answerUserAvatar;

    @BindView(R.id.questionTimeTv)
    TextView questionTimeTv;

    @BindView(R.id.answerListenersTv)
    TextView answerListenersTv;

    @BindView(R.id.answerUserAvatar2)
    ImageView answerUserAvatar2;

    @BindView(R.id.answerUserNameTv)
    TextView answerUserNameTv;

    @BindView(R.id.answerUserTitleTv)
    TextView answerUserTitleTv;

    @BindView(R.id.answerUserFollowersTv)
    TextView answerUserFollowersTv;

    @BindView(R.id.answerRecorderView)
    RecorderView answerRecorderView;

    @BindView(R.id.answerPlayerView)
    PlayerView answerPlayerView;

    @BindView(R.id.title)
    TextView titleTv;

    @BindView(R.id.answerContent)
    TextView answerContent;

    private DatabaseReference database;

    private MediaRecorder mediaRecorder;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_answer_detail);
        ButterKnife.bind(this);

        titleTv.setText(R.string.question_title);
        answerContent.setText(getString(R.string.question_answer_one_dollar_listen,
                FirebaseRemoteConfig.getInstance().getDouble(ConfigConstants.CONFIG_SECRET_LISTEN_FEE)));

        database = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder.stop();
            mediaRecorder = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        database.child(TableConstants.TABLE_QUESTIONS)
                .child(getIntent().getStringExtra(Q_KEY))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Question question = dataSnapshot.getValue(Question.class);
                        updateBasicUI(question);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void updateBasicUI(Question question) {
        updateQuestionUserInfo(question);
        updateAnswerUserInfo(question);
        updateQuestionDetail(question);
        updateAnswerDetail(question);
    }

    private void updateAnswerDetail(Question question) {
        final Answer answer = question.answer;
        if (answer == null) {
            if (question.to.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                answerRecorderView.setVisibility(View.VISIBLE);
            } else {
                answerRecorderView.setVisibility(View.GONE);
            }
            answerListenersTv.setVisibility(View.GONE);
            answerContainer.setVisibility(View.GONE);
            return;
        }
        answerRecorderView.setVisibility(View.GONE);
        answerListenersTv.setVisibility(View.VISIBLE);
        answerContainer.setVisibility(View.VISIBLE);

        questionTimeTv.setText(QuestionUtil.getDisplayTime(System.currentTimeMillis() - answer.timestamp));
        answerListenersTv.setText(getString(R.string.user_secret_listening_count, answer.secretListeners));
        answerContentLength.setText(String.valueOf((int) Math.ceil(answer.contentLength / 1000L)));
    }

    private void updateQuestionDetail(Question question) {
        questFeeTv.setText(getString(R.string.user_quest_fee, question.questFee));
        questionTv.setText(question.content);
        questionTimeTv.setText(QuestionUtil.getDisplayTime(System.currentTimeMillis() - question.timestamp));

        answerListenersTv.setVisibility(View.GONE);
        answerContainer.setVisibility(View.GONE);
    }

    private void updateQuestionUserInfo(Question question) {
        database.child(TableConstants.TABLE_USERS).child(question.from).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User questionFrom = dataSnapshot.getValue(User.class);
                userNameTv.setText(questionFrom.username);
                if (questionFrom.photoUrl != null && !questionFrom.photoUrl.isEmpty()) {
                    ImageLoader.getInstance().displayImage(questionFrom.photoUrl, userAvatar);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateAnswerUserInfo(Question question) {
        database.child(TableConstants.TABLE_USERS).child(question.to).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User answerUser = dataSnapshot.getValue(User.class);
                answerUserNameTv.setText(answerUser.username);
                if (answerUser.photoUrl != null && !answerUser.photoUrl.isEmpty()) {
                    ImageLoader.getInstance().displayImage(answerUser.photoUrl, answerUserAvatar);
                    ImageLoader.getInstance().displayImage(answerUser.photoUrl, answerUserAvatar2);
                }
                answerUserTitleTv.setText(answerUser.title);
                answerUserFollowersTv.setText(getString(R.string.user_followers_count, answerUser.followers));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @OnClick(R.id.thumbDownIv)
    void doThumbDown(View v) {
        database.child(TableConstants.TABLE_QUESTIONS).child(getIntent().getStringExtra(Q_KEY))
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Question question = mutableData.getValue(Question.class);
                        if (question.answer != null) {
                            question.answer.rating += 1;
                        }
                        mutableData.setValue(question);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });
    }

    private long lastPlayingAnswerTime = 0;

    @OnClick({R.id.answerContent, R.id.answerPlayerView})
    void playOrStopQuestionContent(View v) {
        if (System.currentTimeMillis() - lastPlayingAnswerTime < 1000) {
            return;
        }
        lastPlayingAnswerTime = System.currentTimeMillis();
        if (answerPlayerView.isAnimationStarted()) {
            answerPlayerView.stopAnimation();
        } else {
            playAnswer();
        }
    }

    private long lastClickingRecordTime = 0;

    @OnClick(R.id.answerRecorderView)
    void doAnswerOrCancel(View v) {
        // protect multiple clicks in short time
        if (System.currentTimeMillis() - lastClickingRecordTime < 1000) {
            return;
        }
        lastClickingRecordTime = System.currentTimeMillis();
        if (answerRecorderView.isAnimationStarted()) {
            answerRecorderView.stopAnimation();
        } else {
            QuestionAnswerDetailActivityPermissionsDispatcher.recordAnswerWithCheck(this);
        }
    }

    private long recordStartTime = 0;

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    void recordAnswer() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        String fileOutput = AudioHandleUtil.getAudioOutput(getIntent().getStringExtra(Q_KEY));
        try {
            new File(fileOutput).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.setOutputFile(AudioHandleUtil.getAudioOutput(getIntent().getStringExtra(Q_KEY)));
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            recordStartTime = System.currentTimeMillis();
            answerRecorderView.startAnimation((int) FirebaseRemoteConfig.getInstance().getLong(ConfigConstants.CONFIG_AUDIO_MAX_TIME), this);
        } catch (IOException e) {
            e.printStackTrace();
            mediaRecorder = null;
        }
    }

    @OnShowRationale(Manifest.permission.RECORD_AUDIO)
    void showRationalForRecordAnswer(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(R.string.question_permission_audio_rationale)
                .show();
    }

    void playAnswer() {

        AudioHandleUtil.getAnswerAudio(getIntent().getStringExtra(Q_KEY), new OnSuccessListener<File>() {
            @Override
            public void onSuccess(File file) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(file.getAbsolutePath());
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mediaPlayer = null;
                            answerPlayerView.stopAnimation();
                        }
                    });
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    answerPlayerView.startAnimation(Integer.parseInt(answerContentLength.getText().toString()), QuestionAnswerDetailActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRecorderFinished() {
        if (mediaRecorder == null) {
            return;
        }
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        final long recordTotalTime = System.currentTimeMillis() - recordStartTime;
        try {
            AudioHandleUtil.saveAnswerAudio(getIntent().getStringExtra(Q_KEY), new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(final Uri file) {
                    if (file != null) {
                        database.child(TableConstants.TABLE_QUESTIONS).child(getIntent().getStringExtra(Q_KEY)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Question question = dataSnapshot.getValue(Question.class);
                                question.answer = new Answer(file.toString());
                                question.answer.contentLength = recordTotalTime;
                                updateBasicUI(question);
                                QuestionUtil.updateQuestion(getIntent().getStringExtra(Q_KEY), question, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        // TODO: 6/5/16 set the retry button invisible
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } else {
                        // TODO: 6/5/16 set the retry button visible
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // TODO: 6/5/16  show user the error message and let him record audio again
        }
    }

    @Override
    public void onPlayerFinished() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        QuestionAnswerDetailActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

}
