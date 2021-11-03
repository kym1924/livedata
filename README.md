<div>
<img src="https://img.shields.io/badge/Android-3DDC84?style=flat&logo=Android&logoColor=white" />
<img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=Kotlin&logoColor=white" />
<img src="https://img.shields.io/badge/writer-kym1924-yellow?&style=flat&logo=Android"/>
</div>

# LiveData
Translator using *LiveData* [(and Kakao API)](https://developers.kakao.com/product/translation)
<br><br>
<img width=360 height=760 src="https://user-images.githubusercontent.com/63637706/140039107-0b06e4d2-a244-411b-95a1-4711771ebd28.gif"/>

#### 1. Observer
```java
public interface Observer<T> {
    void onChanged(T t);
    // Called when the data is changed.
}
```
- `Observer` is A simple callback that can receive from *LiveData*.
- `Observer` listens notifications from *the LiveData* in the active state of *the LifecycleOwner*.
  - Active state means `Lifecycle.State.STARTED` or `Lifecycle.State.RESUMED`.
  - Observer in `Lifecycle.State.DESTROYED` can not listen.
<br>

#### 2. LiveData
- `LiveData` is data holder class can *be observed*.
- `LiveData` is aware of *the lifecycle* of Activity and Fragment etc.
- `LiveData` is paired with *an Observer class*.
- `LiveData` notifies its update only to Observer in the active state.
<br>

#### 2-1. The advantages of using LiveData
- `Match the state of the data`
  - When LiveData is updated, it notifies the Observer.
  - When receive notifications from LiveData, can update UI in Observer.
- `No memory leaks`
  - Observer connected with Lifecycle is destroyed when Lifecycle is destroyed.
- `No crashes due to stopped activites`
  - No receive notifications from LiveData in the inactive state of the LifecycleOwner.
- `No more lifecycle handling`
  - Because LiveData is aware of the lifecycle, don't have to manage it.
- `Always up-to-date data`
  - When the Lifecycle becomes active again from inactive state, can observe the most recent LiveData.
- `Proper configuration changes`
  - When the Activity or Fragment is recreated because of rotating screen, can observe the most recent LiveData.
- `Sharing resources`
  - Can wrap system services by extending a LiveData object that uses the singleton pattern so that apps can share system services.
<br>

#### 2-2. The methods of LiveData
- `constructor` : Creates a LiveData initialized with the given value or no value.
```java
private volatile Object mData;
private int mVersion;
  
@SuppressWarnings("WeakerAccess") /* synthetic access */
static final Object NOT_SET = new Object();
static final int START_VERSION = -1;
  
public LiveData(T value) {
    mData = value;
    mVersion = START_VERSION + 1;
}
  
public LiveData() {
    mData = NOT_SET;
    mVersion = START_VERSION;
}
```
<br>

- `getValue()` : Return value of LiveData. if LiveData initialized with no value, return null.
```java
@SuppressWarnings("unchecked")
@Nullable
public T getValue() {
    Object data = mData;
    if (data != NOT_SET) {
        return (T) data;
    }
    return null;
}
```
<br>

- `setValue()` : Sets the value.
```java
@MainThread
protected void setValue(T value) {
    assertMainThread("setValue");
    mVersion++;
    mData = value;
    dispatchingValue(null);
}
```
<br>

- `postValue()` : Posts a task to a main thread to set the given value.
```java
private final Runnable mPostValueRunnable = new Runnable() {
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
      Object newValue;
      synchronized (mDataLock) {
          newValue = mPendingData;
          mPendingData = NOT_SET;
      }
      setValue((T) newValue);
    }
};
  
protected void postValue(T value) {
    boolean postTask;
    synchronized (mDataLock) {
        postTask = mPendingData == NOT_SET;
        mPendingData = value;
    }
    if (!postTask) {
        return;
    }
    ArchTaskExecutor.getInstance().postToMainThread(mPostValueRunnable);
}
```
<br>

- `hasActiveObservers()` : If this LiveData has active observers.
```java
@SuppressWarnings("WeakerAccess") /* synthetic access */
int mActiveCount = 0;
  
@SuppressWarnings("WeakerAccess")
public boolean hasActiveObservers() {
    return mActiveCount > 0;
}
```
<br>

- `hasObservers()` : If this LiveData has observers.
```java
@SuppressWarnings("WeakerAccess") /* synthetic access */
public boolean hasObservers() {
    return mObservers.size() > 0;
}
```
<br>

- `observe(LifecycleOwnver ownver, Observer<? super T> observer)` : The given observer is added to mObservers.
```java
private SafeIterableMap<Observer<? super T>, ObserverWrapper> mObservers = new SafeIterableMap<>();
  
@MainThread
public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
    assertMainThread("observe");
    if (owner.getLifecycle().getCurrentState() == DESTROYED) {
        // ignore
        return;
    }
    LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer);
    ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
    if (existing != null && !existing.isAttachedTo(owner)) {
        throw new IllegalArgumentException("Cannot add the same observer" + " with different lifecycles");
    }
    if (existing != null) {
        return;
    }
    owner.getLifecycle().addObserver(wrapper);
}
```
<br>

- `observeForever(Observer<? super T> observer)` : The given observer is added to mObservers.
  - This Observer is considered to be always active state. So, should call `removeObserver(Observer)` method.
```java
@MainThread
public void observeForever(@NonNull Observer<? super T> observer) {
    assertMainThread("observeForever");
    AlwaysActiveObserver wrapper = new AlwaysActiveObserver(observer);
    ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
    if (existing instanceof LiveData.LifecycleBoundObserver) {
        throw new IllegalArgumentException("Cannot add the same observer" + " with different lifecycles");
    }
    if (existing != null) {
        return;
    }
    wrapper.activeStateChanged(true);
}
```
<br>

- `removeObserver(Observer<? super T> observer)` : The given observer is removed from mObservers.
```java
@MainThread
public void removeObserver(@NonNull final Observer<? super T> observer) {
    assertMainThread("removeObserver");
    ObserverWrapper removed = mObservers.remove(observer);
    if (removed == null) {
        return;
    }
    removed.detachObserver();
    removed.activeStateChanged(false);
}
```
<br>

- `removeObservers(LifecycleOwner owner)` : Remove all observers related to the given LifecycleOwner.
```java
@SuppressWarnings("WeakerAccess")
@MainThread
public void removeObservers(@NonNull final LifecycleOwner owner) {
    assertMainThread("removeObservers");
    for (Map.Entry<Observer<? super T>, ObserverWrapper> entry : mObservers) {
        if (entry.getValue().isAttachedTo(owner)) {
            removeObserver(entry.getKey());
        }
    }
}
```
<br>

#### 2-3. The Usage of LiveData
- Create an `LiveData` instance.
  - setValue() method of LiveData is protected. So, to update value use *MutableLiveData*.
  - *Using encapsulation*, Prevent other classes from changing the value.
```kotlin
class MainViewModel : ViewModel() {
    ..
    
    private val _snackBar = MutableLiveData<String>()
    val snackBar: LiveData<String> = _snackBar
    
    ..
    
    fun setSnackBar(msg: String) {
        _snackBar.value = msg
    }
    
    ..
}
```
<br>

- Create an `Observer` instance with an onChanged() method defined.
```kotlin
class MainActivity : AppCompatActivity() {
    ..
    
    val snackBarObserver = Observer<String> { msg ->
		Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()  
    }
    
    ..
      
    binding.btnCopyText.setOnClickListener {
        viewModel.setSnackBar(getString(R.string.make_copy))
    }
    ..
}
```
<br>

- Using observe() method, LiveData instance and Observer instance are connected.
  - @param owner : The LifecycleOwner which controls the observer.
  - @param observer : The observer that will receive the events.
```kotlin
class MainActivity : AppCompatActivity() {
    ..
    
    // viewModel.snackBar.observe(this@MainActivity, snackBarObserver)
    viewModel.snackBar.observe(this@MainActivity) { msg ->
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
    }
    
    ..
}
```
<br>

#### 3. The transformations of LiveData
LiveData changes its own value based on *other LiveData's value* Before sending a notification to the Observer.
<br>

#### 3-1. Transformations.map
It takes as parameters another LiveData to base on and a function to change its value.
```java
@MainThread
@NonNull
public static <X, Y> LiveData<Y> map(
        @NonNull LiveData<X> source,
        @NonNull final Function<X, Y> mapFunction) {
    final MediatorLiveData<Y> result = new MediatorLiveData<>();
    result.addSource(source, new Observer<X>() {
        @Override
        public void onChanged(@Nullable X x) {
            // The function passed to map() must return a value.
            result.setValue(mapFunction.apply(x));
        }
    });
    return result;
}
```

- *example*
```kotlin
val countryList = Transformations.map(isSrcBottomSheet) { value ->
    val removeCountry = when (value) {
        true -> requireNotNull(_targetLang.value)
        false -> requireNotNull(_srcLang.value)
    }
 Country.values().toMutableList().apply {
        remove(removeCountry)
    }
}
 ```
<br>

#### 3-2. Transformation.switchMap
The function passed to switchMap() must return a LiveData.
```java
@MainThread
@NonNull
public static <X, Y> LiveData<Y> switchMap(
        @NonNull LiveData<X> source,
        @NonNull final Function<X, LiveData<Y>> switchMapFunction) {
    final MediatorLiveData<Y> result = new MediatorLiveData<>();
    result.addSource(source, new Observer<X>() {
        LiveData<Y> mSource;

        @Override
        public void onChanged(@Nullable X x) {
            // The function passed to switchMap() must return a LiveData.
            LiveData<Y> newLiveData = switchMapFunction.apply(x);
            if (mSource == newLiveData) {
                return;
            }
            if (mSource != null) {
                result.removeSource(mSource);
            }
            mSource = newLiveData;
            if (mSource != null) {
                result.addSource(mSource, new Observer<Y>() {
                    @Override
                    public void onChanged(@Nullable Y y) {
                        result.setValue(y);
                    }
                });
            }
        }
    });
    return result;
}
```

- *example*
```kotlin
private fun getCountryList(value: Boolean): LiveData<MutableList<Country>> {
    return
}
  
val countryList = Transformations.map(isSrcBottomSheet) { value ->
    getCountryList(value)
}
```
<br>

#### 3-3. MediatorLiveData
It can be passed multiple LiveData sources with addSource() method.

```java
@MainThread
public <S> void addSource(@NonNull LiveData<S> source, @NonNull Observer<? super S> onChanged) {
    Source<S> e = new Source<>(source, onChanged);
    Source<?> existing = mSources.putIfAbsent(source, e);
    if (existing != null && existing.mObserver != onChanged) {
        throw new IllegalArgumentException("This source was already added with the different observer");
    }
    if (existing != null) {
        return;
    }
    if (hasActiveObservers()) {
        e.plug();
    }
}
```

- *example*
```kotlin
val translated = MediatorLiveData<String>()
translated.addSource(Query) { query -> 
    // TODO
}
trnaslated.addSource(_targetLang) { country ->
    // TODO
}
/*
 * translated.addSource(query, object : Observer<String> {
 *   override fun onChanged(t: String) {
 *       // TODO
 *   }
 * })
 */
```
<br>

#### 4. LiveData with SingleLiveEvent
Consider [2-3 case](#2-3-the-usage-of-livedata).
<br>
1. The value of the _snackBar is changed. And the Snackbar is shown.
2. As the screen rotates, MainActivity is recreated.
3. Observer become active again, can observe the most recent LiveData.
4. So, Snackbar shown again. Because Observer observes the latest value of _snackBar.
5. Snackbar only needs to be seen once. So How can solve this situation?
- Let's wrap LiveData with `Event` and use it.

```kotlin
/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}
```
<br>

- Update 2-3 Case.
```kotlin
class MainViewModel : ViewModel() {
    ..
    
    private val _snackBar = MutableLiveData<Event<String>>()
    val snackBar: LiveData<Event<String>> = _snackBar
    
    ..
    
    fun setSnackBar(msg: String) {
        _snackBar.value = Event(msg)
    }
    
    ..
}

class MainActivity : AppCompatActivity() {
    ..
    
    /* 
	 * viewModel.snackBar.observe(this@MainActivity) { it -> // it: Event<String>!
	 *	it.getContentIfNotHandled()?.let { msg ->
     *    	showSnackBar(msg)
     *    }
     * }
     */
    
    viewModel.snackBar.observe(this@MainActivity, EventObserver { msg ->
        binding.root.showSnackBar(msg)
    })
    ..
}
```
<br>

- EventObserver
```kotlin
class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getContentIfNotHandled()?.let { value ->
            onEventUnhandledContent(value)
        }
    }
}
```
<br>

##### Reference

- https://developer.android.com/topic/libraries/architecture/livedata
- https://developer.android.com/reference/androidx/lifecycle/LiveData
- https://developer.android.com/reference/androidx/lifecycle/Observer
- https://developer.android.com/reference/androidx/lifecycle/Transformations
- https://developer.android.com/reference/androidx/lifecycle/MediatorLiveData
- https://developer.android.com/codelabs/kotlin-android-training-live-data
- https://developer.android.com/codelabs/kotlin-android-training-live-data-transformations
- https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
- https://gist.github.com/JoseAlcerreca/e0bba240d9b3cffa258777f12e5c0ae9
- https://developers.kakao.com/product/translation
