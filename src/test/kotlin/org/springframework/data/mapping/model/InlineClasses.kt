/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mapping.model

import org.springframework.data.annotation.PersistenceCreator

/**
 * @author Mark Paluch
 */
@JvmInline
value class MyValueClass(val id: String)

/**
 * Simple class. Value class is flattened into `String`. However, `copy` requires a boxed value when called via reflection.
 */
data class WithMyValueClass(val id: MyValueClass) {

	// ByteCode explanation

	// ---------
	// default constructor, detected by Discoverers.KOTLIN
	// private WithMyValueClass(java.lang.String arg0) {}
	// ---------

	// ---------
	// synthetic constructor that we actually want to use
	// public synthetic WithMyValueClass(java.lang.String arg0, kotlin.jvm.internal.DefaultConstructorMarker arg1) {}
	// ---------

	// ---------
	// public static WithMyValueClass copy-R7yrDNU$default(WithMyValueClass var0, String var1, int var2, Object var3) {
	// ---------
}

@JvmInline
value class MyNullableValueClass(val id: String? = "id")

@JvmInline
value class MyNestedNullableValueClass(val id: MyNullableValueClass)

@JvmInline
value class MyGenericValue<T>(val id: T)

@JvmInline
value class MyGenericBoundValue<T : CharSequence>(val id: T)

data class WithGenericValue(
	val string: MyGenericBoundValue<String>,
	val charseq: MyGenericBoundValue<CharSequence>,
	val recursive: MyGenericValue<MyGenericValue<String>>
)

data class WithGenericNullableValue(val recursive: MyGenericValue<MyGenericValue<String>>?)

class WithNestedMyNullableValueClass(
	var id: MyNestedNullableValueClass? = MyNestedNullableValueClass(
		MyNullableValueClass("foo")
	), var baz: MyNullableValueClass? = MyNullableValueClass("id")

	// ByteCode explanation

	// ---------
	// private WithNestedMyNullableValueClass(MyNestedNullableValueClass id, MyNullableValueClass baz) {}
	// ---------

	// ---------
	// default constructor, detected by Discoverers.KOTLIN
	// note that these constructors use boxed variants ("MyNestedNullableValueClass")

	// public synthetic WithNestedMyNullableValueClass(MyNestedNullableValueClass id, MyNullableValueClass baz, DefaultConstructorMarker $constructor_marker) {}
	// ---------

	// ---------
	// translated by KotlinInstantiationDelegate.resolveKotlinJvmConstructor as we require a constructor that we can use to
	// provide the defaulting mask. This constructor only gets generated when parameters are nullable, otherwise
	// Kotlin doesn't create this constructor

	// public synthetic WithNestedMyNullableValueClass(MyNestedNullableValueClass var1, MyNullableValueClass var2, int var3, DefaultConstructorMarker var4) {}
	// ---------
)

/**
 * Nullability on the domain class means that public and static copy methods uses both the boxed type.
 */
data class DataClassWithNullableValueClass(

	val fullyNullable: MyNullableValueClass? = MyNullableValueClass("id"),
	val innerNullable: MyNullableValueClass = MyNullableValueClass("id")

	// ByteCode

	// private final MyNullableValueClass fullyNullable;
	// private final String innerNullable;

	// ---------
	// private DataClassWithNullableValueClass(MyNullableValueClass fullyNullable, String innerNullable)
	// ---------

	// ---------
	// public DataClassWithNullableValueClass(MyNullableValueClass var1, MyNullableValueClass var2, int var3, DefaultConstructorMarker var4) {
	// ---------

	// ---------
	// public final DataClassWithNullableValueClass copy-bwh045w (@Nullable MyNullableValueClass fullyNullable, @NotNull String innerNullable) {
	// ---------

	// ---------
	// public static DataClassWithNullableValueClass copy-bwh045w$default(DataClassWithNullableValueClass var0, MyNullableValueClass var1, MyNullableValueClass var2, int var3, Object var4) {}
	// ---------
)

/**
 * Nullability on a nested level (domain class property isn't nullable, the inner value in the value class is) means that public copy method uses the value component type while the static copy method uses the boxed type.
 *
 * This creates a skew in getter vs. setter. The setter would be required to set the boxed value while the getter returns plain String. Sigh.
 */
data class DataClassWithNestedNullableValueClass(
	val nullableNestedNullable: MyNestedNullableValueClass?,
	val nestedNullable: MyNestedNullableValueClass

	// ByteCode

	// ---------
	// public DataClassWithNestedNullableValueClass(MyNestedNullableValueClass nullableNestedNullable, String nestedNullable, DefaultConstructorMarker $constructor_marker) {
	// ---------

	// ---------
	// public final DataClassWithNestedNullableValueClass copy-W2GYjxM(@Nullable MyNestedNullableValueClass nullableNestedNullable, @NotNull String nestedNullable) { … }
	// ---------

	// ---------
	// public static DataClassWithNestedNullableValueClass copy-W2GYjxM$default(DataClassWithNestedNullableValueClass var0, MyNestedNullableValueClass var1, MyNestedNullableValueClass var2, int var3, Object var4) {
	// ---------
)

class WithValueClassPreferredConstructor(
	val id: MyNestedNullableValueClass? = MyNestedNullableValueClass(
		MyNullableValueClass("foo")
	), val baz: MyNullableValueClass? = MyNullableValueClass("id")
) {

	@PersistenceCreator
	constructor(
		a: String, id: MyNestedNullableValueClass? = MyNestedNullableValueClass(
			MyNullableValueClass("foo")
		)
	) : this(id, MyNullableValueClass(a + "-pref")) {

	}

	// ByteCode explanation

	// ---------
	// private WithPreferredConstructor(MyNestedNullableValueClass id, MyNullableValueClass baz) {}
	// ---------

	// ---------
	//    public WithPreferredConstructor(MyNestedNullableValueClass var1, MyNullableValueClass var2, int var3, DefaultConstructorMarker var4) {}
	// ---------

	// ---------
	// private WithPreferredConstructor(String a, MyNestedNullableValueClass id) {}
	// ---------

	// ---------
	// this is the one we need to invoke to pass on the defaulting mask
	// public synthetic WithPreferredConstructor(String var1, MyNestedNullableValueClass var2, int var3, DefaultConstructorMarker var4) {}
	// ---------

	// ---------
	// public synthetic WithPreferredConstructor(MyNestedNullableValueClass id, MyNullableValueClass baz, DefaultConstructorMarker $constructor_marker) {
	// ---------

	// ---------
	// annotated constructor, detected by Discoverers.KOTLIN
	// @PersistenceCreator
	// public WithPreferredConstructor(String a, MyNestedNullableValueClass id, DefaultConstructorMarker $constructor_marker) {
	// ---------

}

