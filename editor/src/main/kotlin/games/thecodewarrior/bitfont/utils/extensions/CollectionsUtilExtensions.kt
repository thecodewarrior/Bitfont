package com.teamwizardry.mirror.utils

import java.util.Collections
import java.util.NavigableMap
import java.util.NavigableSet
import java.util.SortedMap
import java.util.SortedSet

fun <T> Collection<T>.unmodifiable() = Collections.unmodifiableCollection(this)
fun <T> Set<T>.unmodifiable() = Collections.unmodifiableSet(this)
fun <T> SortedSet<T>.unmodifiable() = Collections.unmodifiableSortedSet(this)
fun <T> NavigableSet<T>.unmodifiable() = Collections.unmodifiableNavigableSet(this)
fun <T> List<T>.unmodifiable() = Collections.unmodifiableList(this)
fun <K, V> Map<K, V>.unmodifiable() = Collections.unmodifiableMap(this)
fun <K, V> SortedMap<K, V>.unmodifiable() = Collections.unmodifiableSortedMap(this)
fun <K, V> NavigableMap<K, V>.unmodifiable() = Collections.unmodifiableNavigableMap(this)
fun <T> Collection<T>.synchronized() = Collections.synchronizedCollection(this)
fun <T> Set<T>.synchronized() = Collections.synchronizedSet(this)
fun <T> SortedSet<T>.synchronized() = Collections.synchronizedSortedSet(this)
fun <T> NavigableSet<T>.synchronized() = Collections.synchronizedNavigableSet(this)
fun <T> List<T>.synchronized() = Collections.synchronizedList(this)
fun <K, V> Map<K, V>.synchronized() = Collections.synchronizedMap(this)
fun <K, V> SortedMap<K, V>.synchronized() = Collections.synchronizedSortedMap(this)
fun <K, V> NavigableMap<K, V>.synchronized() = Collections.synchronizedNavigableMap(this)

fun <T> Collection<T>.unmodifiableCopy() = Collections.unmodifiableCollection(this.toList())
fun <T> Set<T>.unmodifiableCopy() = Collections.unmodifiableSet(this.toSet())
fun <T> List<T>.unmodifiableCopy() = Collections.unmodifiableList(this.toList())
fun <K, V> Map<K, V>.unmodifiableCopy() = Collections.unmodifiableMap(this.toMap())
