package com.example.luchunyang.rxjava;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * RXJava要素:
 * Observable (可观察者，即被观察者)
 * Observer (观察者)
 * subscribe (订阅)、事件。
 * Observable 和 Observer 通过 subscribe() 方法实现订阅关系，从而 Observable 可以在需要的时候发出事件来通知 Observer。
 * <p>
 * onCompleted(): 事件队列完结。RxJava 不仅把每个事件单独处理，还会把它们看做一个队列。RxJava 规定，当不会再有新的 onNext() 发出时，需要触发 onCompleted() 方法作为标志。
 * onError(): 事件队列异常。在事件处理过程中出异常时，onError() 会被触发，同时队列自动终止，不允许再有事件发出。
 * 在一个正确运行的事件序列中, onCompleted() 和 onError() 有且只有一个，并且是事件序列中的最后一个。需要注意的是，onCompleted() 和 onError() 二者也是互斥的，即在队列中调用了其中一个，就不应该再调用另一个。
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv = (ImageView) findViewById(R.id.iv);
    }

    Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
        @Override
        public void call(Subscriber<? super String> subscriber) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            subscriber.onNext("hello rxjava");
            subscriber.onCompleted();
            subscriber.onNext("onCompleted之后,能否再有事件发出?不能");
        }
    }).subscribeOn(Schedulers.newThread());

    Observable<String> threadObservable = Observable.create(new Observable.OnSubscribe<String>() {
        @Override
        public void call(final Subscriber<? super String> subscriber) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    subscriber.onNext("这个是Thread里的消息");
                }
            }).start();
        }
    });


    Observable<String> errorObservable = Observable.create(new Observable.OnSubscribe<String>() {
        @Override
        public void call(Subscriber<? super String> subscriber) {
            subscriber.onNext("this is errorObserable");
            subscriber.onError(new Throwable("自定义异常"));
            subscriber.onNext("异常之后,能否再有事件发出?不能");
        }
    });


    public void observer(View view) {
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "onCompleted: ");
            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "onNext: " + s);
            }
        };

        Subscription subscription = observable.subscribe(observer);
        if(!subscription.isUnsubscribed())
            subscription.unsubscribe();//取消订阅
    }

    /**
     * 除了 Observer 接口之外，RxJava 还内置了一个实现了 Observer 的抽象类：Subscriber。 Subscriber 对 Observer 接口进行了一些扩展，但他们的基本使用方式是完全一样的
     * <p>
     * 1.onStart(): 这是 Subscriber 增加的方法。它会在 subscribe 刚开始，而事件还未发送之前被调用，可以用于做一些准备工作，例如数据的清零或重置.默认情况下它的实现为空.
     * 它总是在 subscribe 所发生的线程被调用.而不能指定线程。要在指定的线程来做准备工作，可以使用 doOnSubscribe() 方法
     * <p>
     * 2.unsubscribe(): 这是 Subscriber 所实现的另一个接口 Subscription 的方法，用于取消订阅
     * 在这个方法被调用后，Subscriber 将不再接收事件。一般在这个方法调用前，可以使用 isUnsubscribed() 先判断一下状态。
     * unsubscribe() 这个方法很重要，因为在 subscribe() 之后， Observable 会持有 Subscriber 的引用，这个引用如果不能及时被释放，将有内存泄露的风险。
     * 所以最好保持一个原则：要在不再使用的时候尽快在合适的地方（例如 onPause() onStop() 等方法中）调用 unsubscribe() 来解除引用关系，以避免内存泄露的发生。
     */
    public void subscriber(View view) {
        Subscriber<String> subscriber = new Subscriber<String>() {

            @Override
            public void onStart() {
                Log.i(TAG, "onStart: ");
            }


            @Override
            public void onCompleted() {
                Log.i(TAG, "onCompleted: ");
            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "onNext: " + s);
            }
        };

        //Observable 并不是在创建的时候就立即开始发送事件，而是在它被订阅的时候，即当 subscribe() 方法执行的时候。
        observable.subscribe(subscriber);
    }


    public void observableInThread(View view) {

        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "onNext: " + " thread name=" + Thread.currentThread().getName() + "  " + s);
            }
        };

        threadObservable.subscribe(subscriber);
    }

    public void action(View view) {
        //如果我们其实并不关心OnCompleted 和 OnError,我们只需要OnNext
        Action1<String> onNext = new Action1<String>() {
            @Override
            public void call(String s) {
                Log.i(TAG, "call: " + s);
            }
        };

        Action1<Throwable> onError = new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.i(TAG, "call: " + throwable.getMessage());
            }
        };
        errorObservable.subscribe(onNext, onError);
    }

    //create() 方法是 RxJava 最基本的创造事件序列的方法。基于这个方法， RxJava 还提供了一些方法用来快捷创建事件队列，例如：just(T...): 将传入的参数依次发送出来。
    public void just(View view) {
        Observable<String> observable = Observable.just("参数1", "参数2", "参数3", "参数4", "参数5");
        //将会依次调用onNext("参数1")-->onNext("参数2")-->onNext("参数3")-->onNext("参数4")-->onNext("参数5")-->onCompleted()

        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "onCompleted: ");
            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "onError: ");
            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "onNext: " + s);
            }
        };
        observable.subscribe(subscriber);
    }

    //from(T[]) / from(Iterable<? extends T>) : 将传入的数组或 Iterable 拆分成具体对象后，依次发送出来。
    public void from(View view) {
        String[] words = {"hello", "hi", "seek"};
        Observable<String> observable = Observable.from(words);
        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "onCompleted: ");
            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "onError: ");
            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "onNext: " + s);
            }
        };
        observable.subscribe(subscriber);
    }

    public void printString(View view) {
        String[] words = {"hello", "seek", "i", "am", "tom"};
        Observable.from(words).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.i(TAG, "call: " + s);
            }
        });
    }

    final private String src = "http://img5.imgtn.bdimg.com/it/u=2865644899,3976786574&fm=21&gp=0.jpg";

    public void downloadImage(View view) {
        final Observable observable = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(final Subscriber<? super Bitmap> subscriber) {
                Log.i(TAG, "call: "+Thread.currentThread().getName());
                Bitmap bitmap = null;
                try {
                    bitmap = getBitmapFromUrl(src);
                } catch (IOException e) {
                    subscriber.onError(e);
                }
                subscriber.onNext(bitmap);
                subscriber.onCompleted();
            }
        });

        final Subscriber<Bitmap> subscriber = new Subscriber<Bitmap>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(final Throwable e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onNext(final Bitmap bitmap) {
                Log.i(TAG, "onNext: "+Thread.currentThread().getName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bitmap != null)
                            iv.setImageBitmap(bitmap);
                    }
                });
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                //在不指定线程的情况下， RxJava 遵循的是线程不变的原则，即：在哪个线程调用 subscribe()，就在哪个线程生产事件；在哪个线程生产事件，就在哪个线程消费事件。如果需要切换线程，就需要用到 Scheduler （调度器）。
                observable.subscribe(subscriber);
            }
        }).start();
    }

    private Bitmap getBitmapFromUrl(String src) throws IOException {
        URL url = new URL(src);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(4000);
        Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
        return bitmap;
    }


    public void scheduler(View view) {
        /**
         * Schedulers.newThread(): 总是启用新线程，并在新线程执行操作。
         * Schedulers.immediate(): 直接在当前线程运行，相当于不指定线程。这是默认的 Scheduler
         * Schedulers.io(): I/O 操作（读写文件、读写数据库、网络信息交互等）行为模式和 newThread() 差不多，区别在于 io() 的内部实现是是用一个无数量上限的线程池，可以重用空闲的线程，因此多数情况下 io() 比 newThread() 更有效率
         * Schedulers.computation(): 计算所使用的 Scheduler。这个计算指的是 CPU 密集型计算，即不会被 I/O 等操作限制性能的操作，例如图形的计算。这个 Scheduler 使用的固定的线程池，大小为 CPU 核数。不要把 I/O 操作放在 computation() 中，否则 I/O 操作的等待时间会浪费 CPU。
         * RxAndroid 还有一个专用的 AndroidSchedulers.mainThread()
         *
         * subscribeOn(): 指定 subscribe() 所发生的线程，即 Observable.OnSubscribe 被激活时所处的线程。或者叫做事件产生的线程。
         * observeOn(): 指定 Subscriber 所运行在的线程。或者叫做事件消费的线程。
         */
        Observable<Bitmap> observable = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Log.i(TAG, "call: "+Thread.currentThread().getName());
                Bitmap bitmap = null;
                try {
                    bitmap = getBitmapFromUrl(src);
                } catch (IOException e) {
                    subscriber.onError(e);
                }
                subscriber.onNext(bitmap);
                subscriber.onCompleted();

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.immediate());

        Subscriber<Bitmap> subscriber = new Subscriber<Bitmap>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(Bitmap bitmap) {
                Log.i(TAG, "onNext: "+Thread.currentThread().getName());
                iv.setImageBitmap(bitmap);
            }
        };

        observable.subscribe(subscriber);
    }


    //所谓变换，就是将事件序列中的对象或整个序列进行加工处理，转换成不同的事件或事件序列
    //map() 方法将参数中的 String 对象转换成一个 Bitmap 对象后返回，而在经过 map() 方法后，事件的参数类型也由 String 转为了 Bitmap。这种直接变换对象并返回的，是最常见的也最容易理解的变换
    public void map(View view) {
        Observable<String> observable = Observable.just(src);
        observable.map(new Func1<String, Bitmap>() {
            @Override
            public Bitmap call(String s) {
                try {
                    return getBitmapFromUrl(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Action1<Bitmap>() {
            @Override
            public void call(final Bitmap bitmap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        iv.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }
}
