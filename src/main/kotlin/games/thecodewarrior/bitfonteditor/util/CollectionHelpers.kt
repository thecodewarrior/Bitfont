package games.thecodewarrior.bitfonteditor.util

import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.collections.ObservableSet
import java.util.Collections
import java.util.NavigableMap
import java.util.NavigableSet
import java.util.Queue
import java.util.SortedMap
import java.util.SortedSet

/**
 * Creates an observable view of this list. The [Observable]s returned from [elementObservers] will be used to detect
 * mutations of the objects within this list.
 */
fun <T> MutableList<T>.observable(elementObservers: (T) -> List<Observable> = { emptyList() })
    : ObservableList<T> = FXCollections.observableList(this) { elementObservers(it).toTypedArray() }

/**
 * Creates an observable list containing the provided values. The [Observable]s returned from [elementObservers] will
 * be used to detect mutations of the objects within the created list.
 */
fun <T> observableListOf(vararg values: T, elementObservers: (T) -> List<Observable> = { emptyList() })
    : ObservableList<T> = mutableListOf(*values).observable()

/**
 * Creates an observable view of this map.
 */
fun <K, V> MutableMap<K, V>.observable()
    : ObservableMap<K, V> = FXCollections.observableMap(this)

/**
 * Creates an observable map containing the provided values.
 */
fun <K, V> observableMapOf(vararg elements: Pair<K, V>)
    : ObservableMap<K, V> = mutableMapOf(*elements).observable()

/**
 * Creates an observable view of this set.
 */
fun <T> MutableSet<T>.observable()
    : ObservableSet<T> = FXCollections.observableSet(this)

/**
 * Creates an observable map containing the provided values.
 */
fun <T> observableSetOf(vararg elements: T)
    : ObservableSet<T> = mutableSetOf(*elements).observable()


fun <T> Collection<T>.unmodifiable(): Collection<T> = Collections.unmodifiableCollection(this)
fun <T> Set<T>.unmodifiable(): Set<T> = Collections.unmodifiableSet(this)
fun <T> SortedSet<T>.unmodifiable(): SortedSet<T> = Collections.unmodifiableSortedSet(this)
fun <T> NavigableSet<T>.unmodifiable(): NavigableSet<T> = Collections.unmodifiableNavigableSet(this)
fun <T> List<T>.unmodifiable(): List<T> = Collections.unmodifiableList(this)

fun <K, V> Map<K, V>.unmodifiable(): Map<K, V> = Collections.unmodifiableMap(this)
fun <K, V> SortedMap<K, V>.unmodifiable(): SortedMap<K, V> = Collections.unmodifiableSortedMap(this)
fun <K, V> NavigableMap<K, V>.unmodifiable(): NavigableMap<K, V> = Collections.unmodifiableNavigableMap(this)

fun <T> Collection<T>.synchronized(): Collection<T> = Collections.synchronizedCollection(this)
fun <T> MutableSet<T>.synchronized(): MutableSet<T> = Collections.synchronizedSet(this)
fun <T> SortedSet<T>.synchronized(): SortedSet<T> = Collections.synchronizedSortedSet(this)
fun <T> NavigableSet<T>.synchronized(): NavigableSet<T> = Collections.synchronizedNavigableSet(this)
fun <T> MutableList<T>.synchronized(): MutableList<T> = Collections.synchronizedList(this)

fun <K, V> MutableMap<K, V>.synchronized(): MutableMap<K, V> = Collections.synchronizedMap(this)
fun <K, V> SortedMap<K, V>.synchronized(): SortedMap<K, V> = Collections.synchronizedSortedMap(this)
fun <K, V> NavigableMap<K, V>.synchronized(): NavigableMap<K, V> = Collections.synchronizedNavigableMap(this)

inline fun <reified T> Collection<T>.checked(): Collection<T> = Collections.checkedCollection(this, T::class.java)
inline fun <reified T> Queue<T>.checked(): Queue<T> = Collections.checkedQueue(this, T::class.java)
inline fun <reified T> Set<T>.checked(): Set<T> = Collections.checkedSet(this, T::class.java)
inline fun <reified T> SortedSet<T>.checked(): SortedSet<T> = Collections.checkedSortedSet(this, T::class.java)
inline fun <reified T> NavigableSet<T>.checked(): NavigableSet<T> = Collections.checkedNavigableSet(this, T::class.java)
inline fun <reified T> List<T>.checked(): List<T> = Collections.checkedList(this, T::class.java)

inline fun <reified K, reified V> Map<K, V>.checked(): Map<K, V> = Collections.checkedMap(this, K::class.java, V::class.java)
inline fun <reified K, reified V> SortedMap<K, V>.checked(): SortedMap<K, V> = Collections.checkedSortedMap(this, K::class.java, V::class.java)
inline fun <reified K, reified V> NavigableMap<K, V>.checked(): NavigableMap<K, V> = Collections.checkedNavigableMap(this, K::class.java, V::class.java)

fun <K, V> ObservableMap<K, V>.unmodifiable(): ObservableMap<K, V> = FXCollections.unmodifiableObservableMap(this)
fun <T> ObservableList<T>.unmodifiable(): ObservableList<T> = FXCollections.unmodifiableObservableList(this)
fun <T> ObservableSet<T>.unmodifiable(): ObservableSet<T> = FXCollections.unmodifiableObservableSet(this)

fun <K, V> ObservableMap<K, V>.synchronized(): ObservableMap<K, V> = FXCollections.synchronizedObservableMap(this)
fun <T> ObservableList<T>.synchronized(): ObservableList<T> = FXCollections.synchronizedObservableList(this)
fun <T> ObservableSet<T>.synchronized(): ObservableSet<T> = FXCollections.synchronizedObservableSet(this)

inline fun <reified K, reified V> ObservableMap<K, V>.checked(): ObservableMap<K, V> = FXCollections.checkedObservableMap(this, K::class.java, V::class.java)
inline fun <reified T> ObservableList<T>.checked(): ObservableList<T> = FXCollections.checkedObservableList(this, T::class.java)
inline fun <reified T> ObservableSet<T>.checked(): ObservableSet<T> = FXCollections.checkedObservableSet(this, T::class.java)
