import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class Observable<T>() {
    constructor(initialValue: T) : this() {
        state.value = initialValue
    }

    private val state = MutableStateFlow<T?>(null)
    var value: T
        get() = state.value!!
        set(value) { state.value = value }

    private val observers = Collections.synchronizedList(mutableListOf<suspend (T) -> Unit>())
    fun observe(observer: suspend  (T) -> Unit) {
        val suspendNullable: suspend (T?) -> Unit = { it?.let { it1 -> observer(it1) } } //don't want to expose that we use nulls internally
        observers.add(suspendNullable)
        scope.launch {
            state.collect(suspendNullable)
        }
    }

    fun removeObserver(observer: suspend (T) -> Unit) {
        observers.remove(observer)
    }
}