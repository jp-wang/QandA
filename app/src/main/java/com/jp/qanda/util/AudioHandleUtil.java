package com.jp.qanda.util;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.LruCache;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author jpwang
 * @since 6/5/16
 */
public final class AudioHandleUtil {
    public final static String AUDIO_CACHE_DIRECTORY = "audios";
    private final static String AUDIO_SUFFIX = ".3gp";
    private static AudioLruCache cache;
    private static File audioDirectory;

    public static void init(int cacheSize, Context context) {
        cache = new AudioLruCache(cacheSize);
        Observable.just(context.getFilesDir())
                .observeOn(Schedulers.io())
                .flatMap(new Func1<File, Observable<File>>() {
                    @Override
                    public Observable<File> call(File file) {
                        audioDirectory = new File(file, AUDIO_CACHE_DIRECTORY);
                        if (audioDirectory.exists()) {
                            return Observable.from(audioDirectory.listFiles());
                        } else {
                            audioDirectory.mkdir();
                            return null;
                        }
                    }
                })
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        cache.put(file.getName(), file);
                    }
                });
    }

    public static String getAudioOutput(String questionKey) {
        checkCacheInit();
        return new File(audioDirectory, questionKey + AUDIO_SUFFIX).getAbsolutePath();
    }

    public static void getAnswerAudio(final String questionKey, final OnSuccessListener<File> callback) {
        checkCacheInit();
        File local = cache.get(questionKey);
        if (local != null) {
            callback.onSuccess(local);
        } else {
            final String localFileName = questionKey + AUDIO_SUFFIX;
            try {
                local = File.createTempFile(questionKey, AUDIO_SUFFIX);
                final File finalLocal = local;
                FirebaseStorage.getInstance().getReference(AUDIO_CACHE_DIRECTORY)
                        .child(localFileName).getFile(finalLocal).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        cache.put(questionKey, finalLocal);
                        callback.onSuccess(finalLocal);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        finalLocal.delete();
                        callback.onSuccess(null);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                callback.onSuccess(null);
            }

        }
    }

    private static void checkCacheInit() {
        if (cache == null) {
            throw new IllegalAccessError("Before call this method, the AudioHandleUtil should be initialized by init() method");
        }
    }

    public static void saveAnswerAudio(String questionKey, final OnSuccessListener<Uri> callback) throws FileNotFoundException {
        checkCacheInit();
        final String localFileName = questionKey + AUDIO_SUFFIX;
        final File local = new File(audioDirectory, localFileName);
        if (!local.exists()) {
            throw new FileNotFoundException("The audio file " + localFileName + " can not be found!");
        } else {
            cache.put(questionKey, local);
            FirebaseStorage.getInstance().getReference(AUDIO_CACHE_DIRECTORY)
                    .child(localFileName).putFile(Uri.fromFile(local))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            callback.onSuccess(taskSnapshot.getDownloadUrl());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onSuccess(null);
                }
            });
        }
    }

    static class AudioLruCache extends LruCache<String, File> {

        /**
         * @param maxSize for caches that do not override {@link #sizeOf}, this is
         *                the maximum number of entries in the cache. For all other caches,
         *                this is the maximum sum of the sizes of the entries in this cache.
         */
        public AudioLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, File oldValue, File newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            if (oldValue == newValue) {
                return;
            }
            Observable.just(oldValue)
                    .observeOn(Schedulers.io())
                    .subscribe(new Action1<File>() {
                        @Override
                        public void call(File s) {
                            if (s != null && s.exists()) {
                                s.delete();
                            }
                        }
                    });
        }
    }
}
