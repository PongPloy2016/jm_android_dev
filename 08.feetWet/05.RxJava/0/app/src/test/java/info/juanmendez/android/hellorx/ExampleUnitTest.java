package info.juanmendez.android.hellorx;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */

@RunWith(RobolectricTestRunner.class)
@Config( constants = BuildConfig.class, manifest="app/src/main/AndroidManifest.xml", sdk = 21)
public class ExampleUnitTest {

    static{
        ShadowLog.stream = System.out;
    }
    private static final String TAG = "RxAndroidSamples";
    BackgroundThread backgroundThread;
    Handler backgroundHandler;

    @Before
    public void beforeChaos(){
        backgroundThread = new BackgroundThread();
    }

    //@Test
    public void observeInMainUI() throws Exception
    {
        Observable.just("one", "two", "three", "four", "five")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(/* an Observer */);
    }

    //@Test
    public void observeInArbitraryThreads(){

        new Thread(()->{
            final Handler handler = new Handler();

            Observable.just("1", "2", "3", "4", "5" ).subscribeOn(Schedulers.newThread()).
                    observeOn(HandlerScheduler.from(handler)).subscribe(
                    s -> {
                        Log.e("retrofix", "handle it!");
                    });

        }, "custom-thread-1").start();
    }

    @Test
    public void testBackgroundThread(){

        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        final Handler mainThread = new Handler(Looper.getMainLooper() );
        onRunSchedulerExampleButtonClicked();
    }


    void onRunSchedulerExampleButtonClicked() {
        sampleObservable()
                .subscribeOn(HandlerScheduler.from(backgroundHandler)) //run on bg thread
                        // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted()");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override
                    public void onNext(String string) {
                        Log.d(TAG, "onNext(" + string + ")");
                    }
                });
    }

    static Observable<String> sampleObservable() {
        return Observable.defer(() -> {
            try {
                // Do some long running operation
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                throw OnErrorThrowable.from(e);
            }
            return Observable.just("one", "two", "three", "four", "five");
        });
    }

    static class BackgroundThread extends HandlerThread {

        BackgroundThread(){
            super("ScheduleSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND );
        }
    }


}